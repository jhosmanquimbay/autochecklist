package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demostracion.dto.CompetidorPrecioDTO;
import com.example.demostracion.model.Vehiculo;
import com.fasterxml.jackson.databind.ObjectMapper;

class MercadoCompetenciaServiceTest {

    private final MercadoCompetenciaService mercadoCompetenciaService = new MercadoCompetenciaService(new ObjectMapper());

    @Test
    void deberiaExtraerAvisosAutocosmos() {
        Vehiculo vehiculo = vehiculo("Toyota", "Corolla", 2020);
        String html = """
                <html><body>
                <article class="card listing-card" itemscope itemtype="http://schema.org/Car">
                    <a itemprop="url" href="/auto/usado/toyota/corolla/18l-se-g-aut/123">
                        <span class="listing-card__brand">Toyota</span>
                        <span class="listing-card__model">Corolla</span>
                        <span class="listing-card__version">1.8L SE-G Aut</span>
                    </a>
                    <span class="listing-card__year">2020</span>
                    <span class="listing-card__km">86500 km</span>
                    <div class="listing-card__location">Bogotá | Bogotá</div>
                    <span class="listing-card__price-value" itemprop="price" content="89000000">$89.000.000</span>
                </article>
                </body></html>
                """;

        List<CompetidorPrecioDTO> comparables = mercadoCompetenciaService.extraerAutocosmos(html, vehiculo);

        assertThat(comparables).hasSize(1);
        assertThat(comparables.get(0).getFuente()).isEqualTo("Autocosmos");
        assertThat(comparables.get(0).getPrecio()).isEqualTo(89_000_000d);
        assertThat(comparables.get(0).getAnio()).isEqualTo(2020);
    }

    @Test
    void deberiaExtraerTuCarroYExcluirVersionCrossSiElModeloEsCorolla() {
        Vehiculo vehiculo = vehiculo("Toyota", "Corolla", 2020);
        String html = """
                <html><body><script>
                {"results":[
                  {"id":"POLYCARD","polycard":{"metadata":{"url":"articulo.tucarro.com.co/MCO-1-toyota-corolla-18-se-g-_JM"},"components":[
                    {"type":"title","title":{"text":"Toyota Corolla 1.8 Se-g"}},
                    {"type":"price","price":{"current_price":{"value":96000000,"currency":"COP"}}},
                    {"type":"attributes_list","attributes_list":{"texts":["2020","47.000 Km"]}},
                    {"type":"location","location":{"text":"Usaquén - Bogotá D.C."}}
                  ]}},
                  {"id":"POLYCARD","polycard":{"metadata":{"url":"articulo.tucarro.com.co/MCO-2-toyota-corolla-cross-18-_JM"},"components":[
                    {"type":"title","title":{"text":"Toyota Corolla Cross 1.8 Hev"}},
                    {"type":"price","price":{"current_price":{"value":139800000,"currency":"COP"}}},
                    {"type":"attributes_list","attributes_list":{"texts":["2020","33.500 Km"]}},
                    {"type":"location","location":{"text":"Suba - Bogotá D.C."}}
                  ]}}
                ],"paging":{}}</script></body></html>
                """;

        List<CompetidorPrecioDTO> comparables = mercadoCompetenciaService.extraerTuCarro(html, vehiculo);

        assertThat(comparables).hasSize(1);
        assertThat(comparables.get(0).getFuente()).isEqualTo("TuCarro");
        assertThat(comparables.get(0).getTitulo()).contains("Toyota Corolla 1.8 Se-g");
        assertThat(comparables.get(0).getPrecio()).isEqualTo(96_000_000d);
    }

    private Vehiculo vehiculo(String marca, String modelo, Integer anio) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAnio(anio);
        return vehiculo;
    }
}