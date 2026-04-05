package com.example.demostracion.service;

import com.example.demostracion.dto.ReporteInventarioDTO;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PDFGeneratorService {

    private static final DeviceRgb COLOR_HEADER = new DeviceRgb(255, 214, 0);
    private static final DeviceRgb COLOR_ACCENT = new DeviceRgb(33, 150, 243);

    public byte[] generarReportePDF(ReporteInventarioDTO reporte) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        agregarTitulo(document, "REPORTE DE INVENTARIO DE VEHÍCULOS");
        agregarInformacionReporte(document, reporte);
        agregarTotalesGenerales(document, reporte);
        
        if (!reporte.getVehiculosPorMarca().isEmpty()) {
            document.add(new Paragraph("\n"));
            agregarSubtitulo(document, "Distribución por Marca");
            byte[] chartMarca = generarGraficoTorta(reporte.getVehiculosPorMarca(), "Vehículos por Marca");
            agregarImagen(document, chartMarca);
        }
        
        if (!reporte.getVehiculosPorEstadoLogistico().isEmpty()) {
            document.add(new Paragraph("\n"));
            agregarSubtitulo(document, "Vehículos por Estado Logístico");
            byte[] chartEstado = generarGraficoBarras(reporte.getVehiculosPorEstadoLogistico(), 
                "Estado Logístico", "Cantidad de Vehículos");
            agregarImagen(document, chartEstado);
        }
        
        if (!reporte.getVehiculosPorColor().isEmpty()) {
            document.add(new Paragraph("\n"));
            agregarSubtitulo(document, "Distribución por Color");
            agregarTablaEstadisticas(document, reporte.getVehiculosPorColor(), "Color", "Cantidad");
        }
        
        if (!reporte.getVehiculosPorAnio().isEmpty()) {
            document.add(new Paragraph("\n"));
            agregarSubtitulo(document, "Vehículos por Año de Fabricación");
            byte[] chartAnio = generarGraficoBarrasAnio(reporte.getVehiculosPorAnio(), 
                "Año", "Cantidad de Vehículos");
            agregarImagen(document, chartAnio);
        }
        
        if (!reporte.getVehiculosPorUbicacion().isEmpty()) {
            document.add(new Paragraph("\n"));
            agregarSubtitulo(document, "Distribución por Ubicación");
            agregarTablaEstadisticas(document, reporte.getVehiculosPorUbicacion(), "Ubicación", "Cantidad");
        }
        
        agregarPiePagina(document);
        
        document.close();
        return baos.toByteArray();
    }

    private void agregarTitulo(Document document, String texto) {
        Paragraph titulo = new Paragraph(texto)
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(COLOR_HEADER)
            .setPadding(10);
        document.add(titulo);
    }

    private void agregarSubtitulo(Document document, String texto) {
        Paragraph subtitulo = new Paragraph(texto)
            .setFontSize(14)
            .setBold()
            .setFontColor(COLOR_ACCENT)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(subtitulo);
    }

    private void agregarInformacionReporte(Document document, ReporteInventarioDTO reporte) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));
        
        table.addCell(crearCeldaInfo("Fecha de Generación:"));
        table.addCell(crearCeldaValor(reporte.getFechaGeneracion().format(formatter)));
        
        if (reporte.getGeneradoPor() != null) {
            table.addCell(crearCeldaInfo("Generado por:"));
            table.addCell(crearCeldaValor(reporte.getGeneradoPor()));
        }
        
        table.addCell(crearCeldaInfo("Filtros Aplicados:"));
        table.addCell(crearCeldaValor(reporte.getFiltrosAplicados()));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarTotalesGenerales(Document document, ReporteInventarioDTO reporte) {
        agregarSubtitulo(document, "Totales Generales");
        
        Table table = new Table(3);
        table.setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(crearCeldaEncabezado("Total Vehículos"));
        table.addHeaderCell(crearCeldaEncabezado("Activos"));
        table.addHeaderCell(crearCeldaEncabezado("Inactivos"));
        
        table.addCell(crearCeldaTotal(String.valueOf(reporte.getTotalVehiculos())));
        table.addCell(crearCeldaTotal(String.valueOf(reporte.getVehiculosActivos())));
        table.addCell(crearCeldaTotal(String.valueOf(reporte.getVehiculosInactivos())));
        
        document.add(table);
    }

    private void agregarTablaEstadisticas(Document document, Map<?, Long> datos, 
                                         String columna1, String columna2) {
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(crearCeldaEncabezado(columna1));
        table.addHeaderCell(crearCeldaEncabezado(columna2));
        
        datos.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .forEach(entry -> {
                table.addCell(crearCeldaInfo(String.valueOf(entry.getKey())));
                table.addCell(crearCeldaInfo(String.valueOf(entry.getValue())));
            });
        
        document.add(table);
    }

    private Cell crearCeldaEncabezado(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setBold())
            .setBackgroundColor(COLOR_ACCENT)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(5);
    }

    private Cell crearCeldaTotal(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setBold().setFontSize(14))
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(10)
            .setBackgroundColor(new DeviceRgb(240, 240, 240));
    }

    private Cell crearCeldaInfo(String texto) {
        return new Cell()
            .add(new Paragraph(texto))
            .setPadding(5);
    }

    private Cell crearCeldaValor(String texto) {
        return new Cell()
            .add(new Paragraph(texto))
            .setPadding(5);
    }

    private byte[] generarGraficoTorta(Map<String, Long> datos, String titulo) throws Exception {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        datos.forEach((key, value) -> dataset.setValue(key, value));

        JFreeChart chart = ChartFactory.createPieChart(
            titulo,
            dataset,
            true,
            true,
            false
        );

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        
        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, 500, 400);
        
        byte[] imageBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
        tempFile.delete();
        
        return imageBytes;
    }

    private byte[] generarGraficoBarras(Map<String, Long> datos, String ejeX, String ejeY) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.forEach((key, value) -> dataset.addValue(value, "Cantidad", key));

        JFreeChart chart = ChartFactory.createBarChart(
            "",
            ejeX,
            ejeY,
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, 500, 400);
        
        byte[] imageBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
        tempFile.delete();
        
        return imageBytes;
    }

    private byte[] generarGraficoBarrasAnio(Map<Integer, Long> datos, String ejeX, String ejeY) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> dataset.addValue(entry.getValue(), "Cantidad", String.valueOf(entry.getKey())));

        JFreeChart chart = ChartFactory.createBarChart(
            "",
            ejeX,
            ejeY,
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, 500, 400);
        
        byte[] imageBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
        tempFile.delete();
        
        return imageBytes;
    }

    private void agregarImagen(Document document, byte[] imageBytes) throws Exception {
        Image image = new Image(ImageDataFactory.create(imageBytes));
        image.setWidth(UnitValue.createPercentValue(80));
        image.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        document.add(image);
    }

    private void agregarPiePagina(Document document) {
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Este reporte fue generado automáticamente por el Sistema de Gestión de Inventario")
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY);
        document.add(footer);
    }
}
