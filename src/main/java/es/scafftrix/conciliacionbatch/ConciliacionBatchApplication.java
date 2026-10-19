package es.scafftrix.conciliacionbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConciliacionBatchApplication {

    private static final Logger logger = LoggerFactory.getLogger(ConciliacionBatchApplication.class);

    public static void main(String[] args) {
        logger.info("conciliacion-batch arrancando; el job se ejecuta al arranque");
        SpringApplication.run(ConciliacionBatchApplication.class, args);
        logger.info("conciliacion-batch: ejecución terminada");
    }
}
