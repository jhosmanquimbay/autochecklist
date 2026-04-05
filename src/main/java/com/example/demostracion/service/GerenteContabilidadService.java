package com.example.demostracion.service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demostracion.dto.DetalleContableVentaDTO;
import com.example.demostracion.dto.ResumenContableDTO;
import com.example.demostracion.model.ContabilidadVenta;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Pedido;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.ContabilidadVentaRepository;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.PedidoRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class GerenteContabilidadService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final Set<String> ESTADOS_CIERRE = Set.of("vendido", "entregado");

    private final ContabilidadVentaRepository contabilidadVentaRepository;
    private final PedidoRepository pedidoRepository;
    private final InventarioRepository inventarioRepository;

    public GerenteContabilidadService(ContabilidadVentaRepository contabilidadVentaRepository,
                                      PedidoRepository pedidoRepository,
                                      InventarioRepository inventarioRepository) {
        this.contabilidadVentaRepository = contabilidadVentaRepository;
        this.pedidoRepository = pedidoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    public List<DetalleContableVentaDTO> listarDetalle(String periodo, String estadoFiltro) {
        String filtro = normalizarEstadoFiltro(estadoFiltro);
        String periodoNormalizado = normalizarPeriodo(periodo);

        Map<Long, ContabilidadVenta> contabilidadPorPedido = contabilidadVentaRepository.findAll().stream()
                .filter(registro -> registro.getPedido() != null && registro.getPedido().getIdPedido() != null)
                .collect(Collectors.toMap(registro -> registro.getPedido().getIdPedido(), registro -> registro, (primero, segundo) -> primero));

        List<DetalleContableVentaDTO> detalles = pedidoRepository.findAll().stream()
                .filter(this::esPedidoContable)
                .filter(pedido -> coincidePeriodo(pedido, periodoNormalizado))
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(pedido -> construirDetalle(pedido, contabilidadPorPedido.get(pedido.getIdPedido())))
                .toList();

        if ("pendientes_liquidacion".equals(filtro)) {
            return detalles.stream().filter(detalle -> !detalle.isConfigurado()).toList();
        }

        if (ESTADOS_CIERRE.contains(filtro)) {
            return detalles.stream()
                    .filter(detalle -> filtro.equals(normalizarEstado(detalle.getEstado())))
                    .toList();
        }

        return detalles;
    }

    public ResumenContableDTO construirResumen(List<DetalleContableVentaDTO> detalles) {
        ResumenContableDTO resumen = new ResumenContableDTO();
        resumen.setTotalOperaciones(detalles.size());
        resumen.setOperacionesLiquidadas((int) detalles.stream().filter(DetalleContableVentaDTO::isConfigurado).count());
        resumen.setPendientesLiquidacion(detalles.stream().filter(detalle -> !detalle.isConfigurado()).count());

        List<DetalleContableVentaDTO> liquidadas = detalles.stream()
                .filter(DetalleContableVentaDTO::isConfigurado)
                .toList();

        resumen.setIngresosLiquidados(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getPrecioVentaFinal())).sum());
        resumen.setCostosLiquidados(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getCostoTotal())).sum());
        resumen.setUtilidadBruta(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getUtilidadBruta())).sum());
        resumen.setComisiones(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getComision())).sum());
        resumen.setUtilidadNeta(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getUtilidadNeta())).sum());
        resumen.setReinversionSugerida(liquidadas.stream().mapToDouble(detalle -> valor(detalle.getReinversion())).sum());
        resumen.setMargenPromedio(liquidadas.isEmpty() ? 0.0 : liquidadas.stream().mapToDouble(detalle -> valor(detalle.getMargen())).average().orElse(0.0));
        resumen.setTicketPromedio(liquidadas.isEmpty() ? 0.0 : liquidadas.stream().mapToDouble(detalle -> valor(detalle.getPrecioVentaFinal())).average().orElse(0.0));
        return resumen;
    }

    public ContabilidadVenta prepararRegistro(Long pedidoId) {
        Pedido pedido = obtenerPedido(pedidoId);
        ContabilidadVenta registro = contabilidadVentaRepository.findByPedidoIdPedido(pedidoId)
                .orElseGet(ContabilidadVenta::new);
        double precioPublicadoActual = pedido.getVehiculo() != null && pedido.getVehiculo().getPrecio() != null
            ? pedido.getVehiculo().getPrecio()
            : 0.0;

        registro.setPedido(pedido);
        registro.setVehiculo(pedido.getVehiculo());
        registro.setInventario(resolverInventario(pedido.getVehiculo()));

        if (registro.getPrecioPublicadoSnapshot() == null) {
            registro.setPrecioPublicadoSnapshot(normalizarMonto(precioPublicadoActual));
        }
        if (registro.getPrecioVentaFinal() == null) {
            registro.setPrecioVentaFinal(registro.getPrecioPublicadoSnapshot());
        }
        if (registro.getPorcentajeComision() == null) {
            registro.setPorcentajeComision(8.0);
        }
        if (registro.getPorcentajeReinversion() == null) {
            registro.setPorcentajeReinversion(60.0);
        }

        return registro;
    }

    @Transactional
    public ContabilidadVenta guardarRegistro(Long pedidoId, ContabilidadVenta formulario) {
        Pedido pedido = obtenerPedido(pedidoId);
        ContabilidadVenta registro = contabilidadVentaRepository.findByPedidoIdPedido(pedidoId)
                .orElseGet(ContabilidadVenta::new);
        double precioPublicadoActual = pedido.getVehiculo() != null && pedido.getVehiculo().getPrecio() != null
            ? pedido.getVehiculo().getPrecio()
            : 0.0;

        registro.setPedido(pedido);
        registro.setVehiculo(pedido.getVehiculo());
        registro.setInventario(resolverInventario(pedido.getVehiculo()));
        registro.setPrecioPublicadoSnapshot(validarMontoConDefault(formulario.getPrecioPublicadoSnapshot(),
            precioPublicadoActual,
                "El precio publicado no puede ser negativo."));
        registro.setPrecioVentaFinal(validarMontoObligatorio(formulario.getPrecioVentaFinal(), "El precio final de venta es obligatorio."));
        registro.setCostoBase(normalizarMonto(formulario.getCostoBase()));
        registro.setCostoAcondicionamiento(normalizarMonto(formulario.getCostoAcondicionamiento()));
        registro.setCostoTraslado(normalizarMonto(formulario.getCostoTraslado()));
        registro.setCostoAdministrativo(normalizarMonto(formulario.getCostoAdministrativo()));
        registro.setGastoPublicacion(normalizarMonto(formulario.getGastoPublicacion()));
        registro.setGastosCierre(normalizarMonto(formulario.getGastosCierre()));
        registro.setPorcentajeComision(validarPorcentaje(formulario.getPorcentajeComision(), 8.0, "El porcentaje de comisión debe estar entre 0 y 100."));
        registro.setPorcentajeReinversion(validarPorcentaje(formulario.getPorcentajeReinversion(), 60.0, "El porcentaje de reinversión debe estar entre 0 y 100."));
        registro.setNotas(normalizarTexto(formulario.getNotas()));

        return contabilidadVentaRepository.save(registro);
    }

    public byte[] generarReportePdf(String periodo, String estadoFiltro) {
        List<DetalleContableVentaDTO> detalles = listarDetalle(periodo, estadoFiltro);
        ResumenContableDTO resumen = construirResumen(detalles);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            try (Document document = new Document(pdf, PageSize.A4.rotate())) {
                DeviceRgb colorMarca = new DeviceRgb(10, 29, 55);
                DeviceRgb colorAcento = new DeviceRgb(245, 166, 35);

                document.add(new Paragraph("REPORTE DE CONTABILIDAD COMERCIAL")
                        .setBold()
                        .setFontSize(18)
                        .setFontColor(colorMarca)
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("Filtro: " + describirFiltro(periodo, estadoFiltro))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setMarginBottom(14));

                Table resumenTabla = new Table(UnitValue.createPercentArray(new float[] {1, 1, 1, 1, 1})).useAllAvailableWidth();
                resumenTabla.addCell(celdaResumen("Operaciones", String.valueOf(resumen.getTotalOperaciones()), colorAcento));
                resumenTabla.addCell(celdaResumen("Liquidadas", String.valueOf(resumen.getOperacionesLiquidadas()), colorAcento));
                resumenTabla.addCell(celdaResumen("Utilidad neta", formatearMoneda(resumen.getUtilidadNeta()), colorAcento));
                resumenTabla.addCell(celdaResumen("Comisiones", formatearMoneda(resumen.getComisiones()), colorAcento));
                resumenTabla.addCell(celdaResumen("Reinversión", formatearMoneda(resumen.getReinversionSugerida()), colorAcento));
                document.add(resumenTabla);

                document.add(new Paragraph("\nDetalle por operación")
                        .setBold()
                        .setFontSize(13)
                        .setFontColor(colorMarca));

                Table detalleTabla = new Table(UnitValue.createPercentArray(new float[] {1.1f, 1.9f, 1.8f, 1.0f, 1.15f, 1.15f, 1.15f, 1.15f})).useAllAvailableWidth();
                detalleTabla.addHeaderCell(celdaHeader("Fecha", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Cliente", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Vehículo", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Estado", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Venta", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Costos", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Utilidad neta", colorMarca));
                detalleTabla.addHeaderCell(celdaHeader("Reinversión", colorMarca));

                for (DetalleContableVentaDTO detalle : detalles) {
                    detalleTabla.addCell(celdaDato(detalle.getFechaOperacion() != null ? detalle.getFechaOperacion().toLocalDate().toString() : "-"));
                    detalleTabla.addCell(celdaDato(detalle.getCliente()));
                    detalleTabla.addCell(celdaDato(detalle.getVehiculo()));
                    detalleTabla.addCell(celdaDato(detalle.getEstadoEtiqueta()));
                    detalleTabla.addCell(celdaDato(detalle.isConfigurado() ? formatearMoneda(valor(detalle.getPrecioVentaFinal())) : "Pendiente"));
                    detalleTabla.addCell(celdaDato(detalle.isConfigurado() ? formatearMoneda(valor(detalle.getCostoTotal())) : "Pendiente"));
                    detalleTabla.addCell(celdaDato(detalle.isConfigurado() ? formatearMoneda(valor(detalle.getUtilidadNeta())) : "Pendiente"));
                    detalleTabla.addCell(celdaDato(detalle.isConfigurado() ? formatearMoneda(valor(detalle.getReinversion())) : "Pendiente"));
                }

                if (detalles.isEmpty()) {
                    detalleTabla.addCell(new Cell(1, 8)
                            .add(new Paragraph("No hay operaciones para el filtro actual."))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(12));
                }

                document.add(detalleTabla);
            }

            return baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF contable.", e);
        }
    }

    public String normalizarEstadoFiltro(String estadoFiltro) {
        if (estadoFiltro == null || estadoFiltro.isBlank()) {
            return "cerradas";
        }

        String valor = estadoFiltro.trim().toLowerCase(LOCALE_ES)
                .replace('á', 'a')
                .replace('é', 'e')
                .replace('í', 'i')
                .replace('ó', 'o')
                .replace('ú', 'u');

        return switch (valor) {
            case "vendido", "entregado", "pendientes_liquidacion", "cerradas" -> valor;
            default -> "cerradas";
        };
    }

    public String describirFiltro(String periodo, String estadoFiltro) {
        String periodoNormalizado = normalizarPeriodo(periodo);
        String estadoNormalizado = normalizarEstadoFiltro(estadoFiltro);
        String etiquetaEstado = switch (estadoNormalizado) {
            case "vendido" -> "Solo vendidos";
            case "entregado" -> "Solo entregados";
            case "pendientes_liquidacion" -> "Pendientes de liquidación";
            default -> "Ventas cerradas";
        };
        if (periodoNormalizado == null) {
            return etiquetaEstado + " · Todo el histórico";
        }
        return etiquetaEstado + " · " + periodoNormalizado;
    }

    private DetalleContableVentaDTO construirDetalle(Pedido pedido, ContabilidadVenta registro) {
        DetalleContableVentaDTO detalle = new DetalleContableVentaDTO();
        detalle.setPedidoId(pedido.getIdPedido());
        detalle.setFechaOperacion(pedido.getFechaCreacion());
        detalle.setEstado(pedido.getEstado());
        detalle.setEstadoEtiqueta(etiquetaEstado(pedido.getEstado()));
        detalle.setCliente(extraerCliente(pedido));
        detalle.setVendedor(pedido.getConductor() != null && pedido.getConductor().getNombre() != null && !pedido.getConductor().getNombre().isBlank()
                ? pedido.getConductor().getNombre()
                : "Sin vendedor");
        detalle.setVehiculo(extraerVehiculo(pedido));
        detalle.setChasis(pedido.getVehiculo() != null ? pedido.getVehiculo().getChasis() : "Sin chasis");
        detalle.setPrecioPublicado(pedido.getVehiculo() != null ? pedido.getVehiculo().getPrecio() : null);

        if (registro == null || registro.getIdContabilidadVenta() == null) {
            detalle.setConfigurado(false);
            detalle.setCostoTotal(0.0);
            detalle.setUtilidadBruta(0.0);
            detalle.setComision(0.0);
            detalle.setUtilidadNeta(0.0);
            detalle.setReinversion(0.0);
            detalle.setMargen(0.0);
            return detalle;
        }

        detalle.setIdContabilidadVenta(registro.getIdContabilidadVenta());
        detalle.setConfigurado(true);
        detalle.setPrecioPublicado(registro.getPrecioPublicadoSnapshot());
        detalle.setPrecioVentaFinal(registro.getPrecioVentaFinal());
        detalle.setCostoTotal(registro.getCostoTotalCalculado());
        detalle.setUtilidadBruta(registro.getUtilidadBrutaCalculada());
        detalle.setComision(registro.getValorComisionCalculado());
        detalle.setUtilidadNeta(registro.getUtilidadNetaCalculada());
        detalle.setReinversion(registro.getValorReinversionCalculado());
        detalle.setMargen(registro.getMargenCalculado());
        return detalle;
    }

    private Pedido obtenerPedido(Long pedidoId) {
        Long pedidoSeguro = java.util.Objects.requireNonNull(pedidoId, "El pedido es obligatorio.");
        return pedidoRepository.findById(pedidoSeguro)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
    }

    private Inventario resolverInventario(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getChasis() == null || vehiculo.getChasis().isBlank()) {
            return null;
        }
        return inventarioRepository.findByChasis(vehiculo.getChasis()).orElse(null);
    }

    private boolean esPedidoContable(Pedido pedido) {
        return ESTADOS_CIERRE.contains(normalizarEstado(pedido.getEstado()));
    }

    private boolean coincidePeriodo(Pedido pedido, String periodoNormalizado) {
        if (periodoNormalizado == null) {
            return true;
        }
        if (pedido.getFechaCreacion() == null) {
            return false;
        }
        return periodoNormalizado.equals(String.format(LOCALE_ES, "%04d-%02d", pedido.getFechaCreacion().getYear(), pedido.getFechaCreacion().getMonthValue()));
    }

    private String normalizarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return null;
        }
        String valor = periodo.trim();
        if (valor.matches("\\d{4}-\\d{2}")) {
            return valor;
        }
        return null;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "";
        }

        return estado.trim().toLowerCase(LOCALE_ES)
                .replace('á', 'a')
                .replace('é', 'e')
                .replace('í', 'i')
                .replace('ó', 'o')
                .replace('ú', 'u');
    }

    private String etiquetaEstado(String estado) {
        return switch (normalizarEstado(estado)) {
            case "vendido" -> "Vendido";
            case "entregado" -> "Entregado";
            default -> "En gestión";
        };
    }

    private String extraerCliente(Pedido pedido) {
        if (pedido.getDescripcion() == null || pedido.getDescripcion().isBlank()) {
            return "Cliente sin nombre";
        }
        String[] partes = pedido.getDescripcion().split("\\|");
        if (partes.length == 0 || partes[0].isBlank()) {
            return "Cliente sin nombre";
        }
        return partes[0].trim();
    }

    private String extraerVehiculo(Pedido pedido) {
        if (pedido.getVehiculo() == null) {
            return "Sin vehículo";
        }
        String marca = pedido.getVehiculo().getMarca() != null ? pedido.getVehiculo().getMarca() : "Sin marca";
        String modelo = pedido.getVehiculo().getModelo() != null ? pedido.getVehiculo().getModelo() : "Sin modelo";
        return (marca + " " + modelo).trim();
    }

    private Double normalizarMonto(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        if (valor < 0) {
            throw new IllegalArgumentException("Los valores monetarios no pueden ser negativos.");
        }
        return valor;
    }

    private Double validarMontoObligatorio(Double valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        if (valor < 0) {
            throw new IllegalArgumentException("Los valores monetarios no pueden ser negativos.");
        }
        return valor;
    }

    private Double validarMontoConDefault(Double valor, Double valorPorDefecto, String mensaje) {
        if (valor == null) {
            return normalizarMonto(valorPorDefecto);
        }
        if (valor < 0) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private Double validarPorcentaje(Double valor, double porDefecto, String mensaje) {
        if (valor == null) {
            return porDefecto;
        }
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isBlank() ? null : texto;
    }

    private String formatearMoneda(double valor) {
        return java.text.NumberFormat.getCurrencyInstance(LOCALE_ES).format(valor);
    }

    private double valor(Double numero) {
        return numero == null ? 0.0 : numero;
    }

    private Cell celdaResumen(String titulo, String valor, DeviceRgb colorAcento) {
        Cell celda = new Cell();
        celda.setPadding(12);
        celda.setBackgroundColor(new DeviceRgb(248, 250, 252));
        celda.setBorderTop(new com.itextpdf.layout.borders.SolidBorder(colorAcento, 3));
        celda.add(new Paragraph(titulo).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));
        celda.add(new Paragraph(valor).setBold().setFontSize(13).setFontColor(new DeviceRgb(10, 29, 55)));
        return celda;
    }

    private Cell celdaHeader(String texto, DeviceRgb colorMarca) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE).setFontSize(9))
                .setBackgroundColor(colorMarca)
                .setPadding(8);
    }

    private Cell celdaDato(String texto) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "-").setFontSize(8.5f))
                .setPadding(7);
    }
}