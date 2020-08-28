package com.personal.webflux.app.model;

public class FacturaItem {

    private Integer cantidad;

    private Plato plato;

    public FacturaItem() {

    }

    public FacturaItem(Integer cantidad, Plato plato) {
        this.cantidad = cantidad;
        this.plato = plato;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }
}
