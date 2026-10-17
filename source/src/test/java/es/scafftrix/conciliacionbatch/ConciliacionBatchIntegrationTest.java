package es.scafftrix.conciliacionbatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.scafftrix.conciliacionbatch.model.ConciliacionResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:conciliacion-test;DB_CLOSE_DELAY=-1"
})
class ConciliacionBatchIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job conciliacionJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void conciliaElCsvDeMuestra() throws Exception {
        JobExecution execution = jobLauncher.run(
                conciliacionJob,
                new JobParametersBuilder().addLong("run.id", 1L).toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM facturas", Long.class);
        assertEquals(5L, total);

        // ResultSetExtractor: el lambda devuelve el map, así que JdbcTemplate lo
        // resuelve a ResultSetExtractor y NO a RowCallbackHandler. Iteramos nosotros.
        Map<String, Long> porEstado = jdbcTemplate.query(
                "SELECT estado, COUNT(*) AS total FROM facturas GROUP BY estado",
                (ResultSetExtractor<Map<String, Long>>) rs -> {
                    Map<String, Long> mapa = new HashMap<>();
                    while (rs.next()) {
                        mapa.put(rs.getString("estado"), rs.getLong("total"));
                    }
                    return mapa;
                });

        assertEquals(3L, porEstado.get(ConciliacionResult.ESTADO_CONCILIADA));
        assertEquals(1L, porEstado.get(ConciliacionResult.ESTADO_IMPORTE_NO_COINCIDE));
        assertEquals(1L, porEstado.get(ConciliacionResult.ESTADO_PENDIENTE));
        assertEquals(5L, porEstado.values().stream().mapToLong(Long::longValue).sum());
    }
}
