package es.scafftrix.conciliacionbatch.model;

public class ConciliacionResult {

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONCILIADA = "CONCILIADA";
    public static final String ESTADO_IMPORTE_NO_COINCIDE = "IMPORTE_NO_COINCIDE";

    private String numeroFactura;
    private String estado;

    public ConciliacionResult() {
    }

    public ConciliacionResult(String numeroFactura, String estado) {
        this.numeroFactura = numeroFactura;
        this.estado = estado;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "ConciliacionResult{" + "numeroFactura='" + numeroFactura + '\'' + ", estado='" + estado + '\'' + '}';
    }
}
