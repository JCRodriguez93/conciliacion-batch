package es.scafftrix.conciliacionbatch.model;

import java.math.BigDecimal;

public class Factura {

    private String numeroFactura;
    private String fechaEmision;
    private BigDecimal importe;
    private String nifCliente;

    public Factura() {
    }

    public Factura(String numeroFactura, String fechaEmision, BigDecimal importe, String nifCliente) {
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.importe = importe;
        this.nifCliente = nifCliente;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getNifCliente() {
        return nifCliente;
    }

    public void setNifCliente(String nifCliente) {
        this.nifCliente = nifCliente;
    }

    @Override
    public String toString() {
        return "Factura{" + "numeroFactura='" + numeroFactura + '\'' + ", fechaEmision='" + fechaEmision + '\'' + ", importe=" + importe + ", nifCliente='" + nifCliente + '\'' + '}';
    }
}
