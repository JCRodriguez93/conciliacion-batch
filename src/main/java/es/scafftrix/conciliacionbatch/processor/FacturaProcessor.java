package es.scafftrix.conciliacionbatch.processor;

import es.scafftrix.conciliacionbatch.model.Factura;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class FacturaProcessor implements ItemProcessor<Factura, Factura> {

    private static final Logger log = LoggerFactory.getLogger(FacturaProcessor.class);

    @Override
    public Factura process(Factura factura) {
        if (factura.getImporte() == null || factura.getImporte().signum() <= 0) {
            log.warn("Factura descartada por importe inválido: {}", factura);
            return null;
        }
        try {
            LocalDate.parse(factura.getFechaEmision());
        } catch (DateTimeParseException | NullPointerException e) {
            log.warn("Factura descartada por fecha de emisión inválida: {}", factura);
            return null;
        }
        return factura;
    }
}
