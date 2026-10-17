package es.scafftrix.conciliacionbatch.model;

import java.math.BigDecimal;

public class Pago {

    private String numeroFactura;
    private BigDecimal importe;
    private String fechaPago;

    public Pago() {
    }

    public Pago(String numeroFactura, BigDecimal importe, String fechaPago) {
        this.numeroFactura = numeroFactura;
        this.importe = importe;
        this.fechaPago = fechaPago;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    @Override
    public String toString() {
        return "Pago{" + "numeroFactura='" + numeroFactura + '\'' + ", importe=" + importe + ", fechaPago='" + fechaPago + '\'' + '}';
    }
}
