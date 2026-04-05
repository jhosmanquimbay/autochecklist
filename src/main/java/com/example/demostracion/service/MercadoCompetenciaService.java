package com.example.demostracion.service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.demostracion.dto.CompetidorPrecioDTO;
import com.example.demostracion.dto.MercadoCompetenciaDTO;
import com.example.demostracion.model.Vehiculo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MercadoCompetenciaService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final String AUTOCOSMOS_BASE_URL = "https://www.autocosmos.com.co";
    private static final String TUCARRO_BASE_URL = "https://carros.tucarro.com.co";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 8000;
    private static final int MAX_RESULTADOS_POR_FUENTE = 4;
    private static final int MAX_RESULTADOS_TOTAL = 8;
    private static final Pattern PATRON_ANIO = Pattern.compile("(19|20)\\d{2}");

    private final ObjectMapper objectMapper;

    public MercadoCompetenciaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MercadoCompetenciaDTO consultarMercado(Vehiculo vehiculo) {
        MercadoCompetenciaDTO mercado = new MercadoCompetenciaDTO();
        mercado.setDisponible(false);
        mercado.setMensaje("No hay suficientes datos para consultar el mercado externo.");

        if (vehiculo == null || textoSeguro(vehiculo.getMarca()).isBlank() || textoSeguro(vehiculo.getModelo()).isBlank()) {
            return mercado;
        }

        List<CompetidorPrecioDTO> comparables = new ArrayList<>();
        comparables.addAll(consultarAutocosmos(vehiculo));
        comparables.addAll(consultarTuCarro(vehiculo));

        List<CompetidorPrecioDTO> unicos = deduplicarComparables(comparables).stream()
                .limit(MAX_RESULTADOS_TOTAL)
                .toList();

        if (unicos.isEmpty()) {
            mercado.setMensaje("No se encontraron avisos externos comparables por modelo y año cercano.");
            return mercado;
        }

        DoubleSummary resumenPrecios = resumirPrecios(unicos);
        String fuentes = unicos.stream()
                .map(CompetidorPrecioDTO::getFuente)
                .filter(Objects::nonNull)
                .filter(valor -> !valor.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        mercado.setDisponible(true);
        mercado.setFuentes(fuentes);
        mercado.setTotalMuestras(unicos.size());
        mercado.setPrecioPromedio(resumenPrecios.promedio());
        mercado.setPrecioMinimo(resumenPrecios.minimo());
        mercado.setPrecioMaximo(resumenPrecios.maximo());
        mercado.setComparables(unicos);
        mercado.setMensaje("Se encontraron " + unicos.size()
                + " referencias externas de " + fuentes
                + " filtradas por modelo y año cercano al vehículo.");
        return mercado;
    }

    List<CompetidorPrecioDTO> extraerAutocosmos(String html, Vehiculo vehiculo) {
        if (html == null || html.isBlank()) {
            return List.of();
        }

        Document document = Jsoup.parse(html, AUTOCOSMOS_BASE_URL);
        Elements cards = document.select("article.listing-card");
        List<CompetidorPrecioDTO> comparables = new ArrayList<>();

        for (Element card : cards) {
            String titulo = limpiarTexto(String.join(" ",
                    card.select(".listing-card__brand").text(),
                    card.select(".listing-card__model").text(),
                    card.select(".listing-card__version").text()));
            String url = card.select("a[itemprop=url]").attr("abs:href");
            Integer anio = parseEntero(card.select(".listing-card__year").text());

            if (!esComparable(vehiculo, titulo, url, anio)) {
                continue;
            }

            Double precio = parsePrecio(
                    card.select("[itemprop=price]").attr("content"),
                    card.select(".listing-card__price-value").text());

            if (precio == null || precio <= 0.0) {
                continue;
            }

            CompetidorPrecioDTO comparable = new CompetidorPrecioDTO();
            comparable.setFuente("Autocosmos");
            comparable.setTitulo(titulo);
            comparable.setPrecio(precio);
            comparable.setAnio(anio);
            comparable.setKilometraje(limpiarTexto(card.select(".listing-card__km").text()));
            comparable.setUbicacion(limpiarTexto(card.select(".listing-card__location").text()));
            comparable.setUrl(url);
            comparables.add(comparable);

            if (comparables.size() >= MAX_RESULTADOS_POR_FUENTE) {
                break;
            }
        }

        return comparables;
    }

    List<CompetidorPrecioDTO> extraerTuCarro(String html, Vehiculo vehiculo) {
        if (html == null || html.isBlank()) {
            return List.of();
        }

        String resultadosJson = extraerArregloResultados(html);
        if (resultadosJson == null || resultadosJson.isBlank()) {
            return List.of();
        }

        try {
            JsonNode results = objectMapper.readTree(resultadosJson);
            List<CompetidorPrecioDTO> comparables = new ArrayList<>();

            for (JsonNode result : results) {
                if (!"POLYCARD".equals(result.path("id").asText())) {
                    continue;
                }

                JsonNode polycard = result.path("polycard");
                JsonNode metadata = polycard.path("metadata");
                JsonNode components = polycard.path("components");
                JsonNode tituloNode = buscarComponente(components, "title");
                JsonNode precioNode = buscarComponente(components, "price");
                JsonNode atributosNode = buscarComponente(components, "attributes_list");
                JsonNode ubicacionNode = buscarComponente(components, "location");

                String titulo = tituloNode.path("title").path("text").asText("");
                String url = normalizarUrlTuCarro(metadata.path("url").asText(""));
                Integer anio = parseEntero(atributosNode.path("attributes_list").path("texts").path(0).asText(""));

                if (!esComparable(vehiculo, titulo, url, anio)) {
                    continue;
                }

                Double precio = precioNode.path("price").path("current_price").path("value").isNumber()
                        ? precioNode.path("price").path("current_price").path("value").doubleValue()
                        : null;

                if (precio == null || precio <= 0.0) {
                    continue;
                }

                CompetidorPrecioDTO comparable = new CompetidorPrecioDTO();
                comparable.setFuente("TuCarro");
                comparable.setTitulo(limpiarTexto(titulo));
                comparable.setPrecio(precio);
                comparable.setAnio(anio);
                comparable.setKilometraje(limpiarTexto(atributosNode.path("attributes_list").path("texts").path(1).asText("")));
                comparable.setUbicacion(limpiarTexto(ubicacionNode.path("location").path("text").asText("")));
                comparable.setUrl(url);
                comparables.add(comparable);

                if (comparables.size() >= MAX_RESULTADOS_POR_FUENTE) {
                    break;
                }
            }

            return comparables;
        } catch (IOException ex) {
            log.warn("No fue posible parsear los resultados de TuCarro", ex);
            return List.of();
        }
    }

    private List<CompetidorPrecioDTO> consultarAutocosmos(Vehiculo vehiculo) {
        try {
            String url = AUTOCOSMOS_BASE_URL + "/auto/usado/" + slugify(vehiculo.getMarca()) + "/" + slugify(vehiculo.getModelo());
            return extraerAutocosmos(descargarHtml(url), vehiculo);
        } catch (IOException ex) {
            log.warn("No fue posible consultar Autocosmos para {} {}", vehiculo.getMarca(), vehiculo.getModelo(), ex);
            return List.of();
        }
    }

    private List<CompetidorPrecioDTO> consultarTuCarro(Vehiculo vehiculo) {
        try {
            String url = TUCARRO_BASE_URL + "/" + slugify(vehiculo.getMarca()) + "/" + slugify(vehiculo.getModelo());
            return extraerTuCarro(descargarHtml(url), vehiculo);
        } catch (IOException ex) {
            log.warn("No fue posible consultar TuCarro para {} {}", vehiculo.getMarca(), vehiculo.getModelo(), ex);
            return List.of();
        }
    }

    private String descargarHtml(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "es-CO,es;q=0.9,en;q=0.8")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .execute()
                .body();
    }

    private String extraerArregloResultados(String html) {
        String marcador = "\"results\":[";
        int inicioMarcador = html.indexOf(marcador);
        if (inicioMarcador < 0) {
            return null;
        }

        int inicioArray = html.indexOf('[', inicioMarcador);
        if (inicioArray < 0) {
            return null;
        }

        int finArray = encontrarFinDeArrayJson(html, inicioArray);
        if (finArray < 0) {
            return null;
        }

        return html.substring(inicioArray, finArray + 1);
    }

    private int encontrarFinDeArrayJson(String texto, int inicioArray) {
        int profundidad = 0;
        boolean enCadena = false;
        boolean escape = false;

        for (int indice = inicioArray; indice < texto.length(); indice++) {
            char caracter = texto.charAt(indice);

            if (escape) {
                escape = false;
                continue;
            }

            if (caracter == '\\' && enCadena) {
                escape = true;
                continue;
            }

            if (caracter == '"') {
                enCadena = !enCadena;
                continue;
            }

            if (enCadena) {
                continue;
            }

            if (caracter == '[') {
                profundidad++;
            } else if (caracter == ']') {
                profundidad--;
                if (profundidad == 0) {
                    return indice;
                }
            }
        }

        return -1;
    }

    private JsonNode buscarComponente(JsonNode components, String type) {
        if (components == null || !components.isArray()) {
            return objectMapper.createObjectNode();
        }
        for (JsonNode component : components) {
            if (type.equals(component.path("type").asText())) {
                return component;
            }
        }
        return objectMapper.createObjectNode();
    }

    private List<CompetidorPrecioDTO> deduplicarComparables(List<CompetidorPrecioDTO> comparables) {
        Map<String, CompetidorPrecioDTO> mapa = comparables.stream()
                .filter(item -> item.getPrecio() != null && item.getPrecio() > 0.0)
                .collect(Collectors.toMap(
                        item -> limpiarTexto(item.getFuente()) + "|" + limpiarTexto(item.getUrl()) + "|" + item.getPrecio().longValue(),
                        item -> item,
                        (a, b) -> a,
                        LinkedHashMap::new));

        return mapa.values().stream()
                .sorted(Comparator.comparing(CompetidorPrecioDTO::getPrecio))
                .toList();
    }

    private DoubleSummary resumirPrecios(List<CompetidorPrecioDTO> comparables) {
        double promedio = comparables.stream()
                .map(CompetidorPrecioDTO::getPrecio)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double minimo = comparables.stream()
                .map(CompetidorPrecioDTO::getPrecio)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);
        double maximo = comparables.stream()
                .map(CompetidorPrecioDTO::getPrecio)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
        return new DoubleSummary(promedio, minimo, maximo);
    }

    private boolean esComparable(Vehiculo vehiculo, String titulo, String url, Integer anio) {
        String modeloNormalizado = normalizarParaComparacion(vehiculo.getModelo());
        String tituloNormalizado = normalizarParaComparacion(titulo);
        String urlNormalizado = normalizarParaComparacion(url);

        if (modeloNormalizado.isBlank()) {
            return false;
        }

        boolean coincideModelo = contieneTodosLosTokens(tituloNormalizado, modeloNormalizado)
                || contieneTodosLosTokens(urlNormalizado, modeloNormalizado);

        if (!coincideModelo) {
            return false;
        }

        if (!modeloNormalizado.contains("cross")
                && (tituloNormalizado.contains(modeloNormalizado + " cross")
                || urlNormalizado.contains(modeloNormalizado + " cross"))) {
            return false;
        }

        if (vehiculo.getAnio() != null && anio != null && Math.abs(vehiculo.getAnio() - anio) > 1) {
            return false;
        }

        return true;
    }

    private boolean contieneTodosLosTokens(String texto, String modelo) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        for (String token : modelo.split(" ")) {
            if (!token.isBlank() && !texto.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private String normalizarUrlTuCarro(String url) {
        String limpio = limpiarTexto(url);
        if (limpio.isBlank()) {
            return limpio;
        }
        if (limpio.startsWith("http://") || limpio.startsWith("https://")) {
            return limpio;
        }
        return "https://" + limpio;
    }

    private Double parsePrecio(String prioridadContenido, String prioridadTexto) {
        String valor = textoSeguro(prioridadContenido);
        if (valor.matches("\\d+(\\.\\d+)?")) {
            return Double.parseDouble(valor);
        }

        String soloDigitos = textoSeguro(prioridadTexto).replaceAll("[^\\d]", "");
        if (soloDigitos.isBlank()) {
            return null;
        }
        return Double.parseDouble(soloDigitos);
    }

    private Integer parseEntero(String texto) {
        String limpio = textoSeguro(texto);
        Matcher matcher = PATRON_ANIO.matcher(limpio);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        String soloDigitos = limpio.replaceAll("[^\\d]", "");
        if (soloDigitos.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(soloDigitos);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String slugify(String texto) {
        String normalizado = Normalizer.normalize(textoSeguro(texto), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(LOCALE_ES);
        return normalizado.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String normalizarParaComparacion(String texto) {
        String normalizado = Normalizer.normalize(textoSeguro(texto), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(LOCALE_ES);
        return normalizado.replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private String limpiarTexto(String texto) {
        return textoSeguro(texto).replaceAll("\\s+", " ").trim();
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private record DoubleSummary(double promedio, double minimo, double maximo) {
    }
}