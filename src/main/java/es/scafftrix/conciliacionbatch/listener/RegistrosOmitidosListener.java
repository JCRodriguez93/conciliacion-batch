package es.scafftrix.conciliacionbatch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

/**
 * Registra cada registro que Spring Batch omite por la tolerancia a fallos
 * configurada en BatchConfig (skip de FlatFileParseException). Sin esto,
 * un fichero mal formado se salta en silencio salvo por el aviso interno
 * de Spring Batch.
 */
@Component
public class RegistrosOmitidosListener implements SkipListener<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(RegistrosOmitidosListener.class);

    @Override
    public void onSkipInRead(Throwable throwable) {
        log.warn("Registro omitido en lectura: {}", throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable throwable) {
        log.warn("Registro omitido en procesamiento: {} ({})", item, throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable throwable) {
        log.warn("Registro omitido en escritura: {} ({})", item, throwable.getMessage());
    }
}
