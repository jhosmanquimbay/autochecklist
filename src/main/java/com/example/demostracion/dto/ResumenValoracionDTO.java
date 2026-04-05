package com.example.demostracion.dto;

public class ResumenValoracionDTO {

    private int totalVehiculos;
    private int listosParaPublicar;
    private int riesgoDocumental;
    private int subirPrecio;
    private int bajarPrecio;
    private int sinPrecio;

    public int getTotalVehiculos() {
        return totalVehiculos;
    }

    public void setTotalVehiculos(int totalVehiculos) {
        this.totalVehiculos = totalVehiculos;
    }

    public int getListosParaPublicar() {
        return listosParaPublicar;
    }

    public void setListosParaPublicar(int listosParaPublicar) {
        this.listosParaPublicar = listosParaPublicar;
    }

    public int getRiesgoDocumental() {
        return riesgoDocumental;
    }

    public void setRiesgoDocumental(int riesgoDocumental) {
        this.riesgoDocumental = riesgoDocumental;
    }

    public int getSubirPrecio() {
        return subirPrecio;
    }

    public void setSubirPrecio(int subirPrecio) {
        this.subirPrecio = subirPrecio;
    }

    public int getBajarPrecio() {
        return bajarPrecio;
    }

    public void setBajarPrecio(int bajarPrecio) {
        this.bajarPrecio = bajarPrecio;
    }

    public int getSinPrecio() {
        return sinPrecio;
    }

    public void setSinPrecio(int sinPrecio) {
        this.sinPrecio = sinPrecio;
    }
}