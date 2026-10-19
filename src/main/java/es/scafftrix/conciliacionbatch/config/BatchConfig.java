package es.scafftrix.conciliacionbatch.config;

import es.scafftrix.conciliacionbatch.listener.JobCompletionNotificationListener;
import es.scafftrix.conciliacionbatch.listener.RegistrosOmitidosListener;
import es.scafftrix.conciliacionbatch.model.ConciliacionResult;
import es.scafftrix.conciliacionbatch.model.Factura;
import es.scafftrix.conciliacionbatch.model.Pago;
import es.scafftrix.conciliacionbatch.processor.FacturaProcessor;
import es.scafftrix.conciliacionbatch.processor.PagoConciliadorProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<Factura> readerFacturas() {
        return new FlatFileItemReaderBuilder<Factura>()
                .name("facturaItemReader")
                .resource(new ClassPathResource("facturas.csv"))
                .delimited()
                .names("numeroFactura", "fechaEmision", "importe", "nifCliente")
                .targetType(Factura.class)
                .build();
    }

    @Bean
    public FacturaProcessor processorFacturas() {
        return new FacturaProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Factura> writerFacturas(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Factura>()
                .sql("INSERT INTO facturas (numero_factura, fecha_emision, importe, nif_cliente, estado) " +
                     "VALUES (:numeroFactura, :fechaEmision, :importe, :nifCliente, 'PENDIENTE')")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public FlatFileItemReader<Pago> readerPagos() {
        return new FlatFileItemReaderBuilder<Pago>()
                .name("pagoItemReader")
                .resource(new ClassPathResource("pagos.csv"))
                .delimited()
                .names("numeroFactura", "importe", "fechaPago")
                .targetType(Pago.class)
                .build();
    }

    @Bean
    public PagoConciliadorProcessor processorPagos(JdbcTemplate jdbcTemplate) {
        return new PagoConciliadorProcessor(jdbcTemplate);
    }

    @Bean
    public JdbcBatchItemWriter<ConciliacionResult> writerPagos(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<ConciliacionResult>()
                .sql("UPDATE facturas SET estado = :estado WHERE numero_factura = :numeroFactura")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step stepFacturas(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             FlatFileItemReader<Factura> readerFacturas,
                             FacturaProcessor processorFacturas,
                             JdbcBatchItemWriter<Factura> writerFacturas,
                             RegistrosOmitidosListener registrosOmitidos) {
        return new StepBuilder("stepFacturas", jobRepository)
                .<Factura, Factura>chunk(3)
                .reader(readerFacturas)
                .processor(processorFacturas)
                .writer(writerFacturas)
                .transactionManager(transactionManager)
                .listener(registrosOmitidos)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(5)
                .build();
    }

    @Bean
    public Step stepPagos(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          FlatFileItemReader<Pago> readerPagos,
                          PagoConciliadorProcessor processorPagos,
                          JdbcBatchItemWriter<ConciliacionResult> writerPagos,
                          RegistrosOmitidosListener registrosOmitidos) {
        return new StepBuilder("stepPagos", jobRepository)
                .<Pago, ConciliacionResult>chunk(3)
                .reader(readerPagos)
                .processor(processorPagos)
                .writer(writerPagos)
                .transactionManager(transactionManager)
                .listener(registrosOmitidos)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(5)
                .build();
    }

    @Bean
    public Job conciliacionJob(JobRepository jobRepository,
                               Step stepFacturas,
                               Step stepPagos,
                               JobCompletionNotificationListener listener) {
        return new JobBuilder("conciliacionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(stepFacturas)
                .next(stepPagos)
                .build();
    }
}
