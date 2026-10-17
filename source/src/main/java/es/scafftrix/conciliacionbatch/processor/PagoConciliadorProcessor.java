package es.scafftrix.conciliacionbatch.processor;

import es.scafftrix.conciliacionbatch.model.ConciliacionResult;
import es.scafftrix.conciliacionbatch.model.Pago;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

public class PagoConciliadorProcessor implements ItemProcessor<Pago, ConciliacionResult> {

    private static final Logger log = LoggerFactory.getLogger(PagoConciliadorProcessor.class);

    private final JdbcTemplate jdbcTemplate;

    public PagoConciliadorProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ConciliacionResult process(Pago pago) {
        if (pago.getImporte() == null) {
            log.warn("Pago descartado por importe ausente: {}", pago);
            return null;
        }
        try {
            LocalDate.parse(pago.getFechaPago());
        } catch (DateTimeParseException | NullPointerException e) {
            log.warn("Pago descartado por fecha de pago inválida: {}", pago);
            return null;
        }

        List<BigDecimal> importes = jdbcTemplate.query(
                "SELECT importe FROM facturas WHERE numero_factura = ?",
                (rs, rowNum) -> rs.getBigDecimal(1),
                pago.getNumeroFactura());

        if (importes.isEmpty()) {
            log.warn("Pago ignorado: no existe la factura {} (importe {})", pago.getNumeroFactura(), pago.getImporte());
            return null;
        }

        BigDecimal importeFactura = importes.get(0);
        String estado = pago.getImporte().compareTo(importeFactura) == 0
                ? ConciliacionResult.ESTADO_CONCILIADA
                : ConciliacionResult.ESTADO_IMPORTE_NO_COINCIDE;

        log.info("Factura {}: pago {} vs factura {} -> {}",
                pago.getNumeroFactura(), pago.getImporte(), importeFactura, estado);

        return new ConciliacionResult(pago.getNumeroFactura(), estado);
    }
}
