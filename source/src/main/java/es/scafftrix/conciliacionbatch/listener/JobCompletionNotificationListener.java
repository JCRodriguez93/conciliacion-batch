package es.scafftrix.conciliacionbatch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final JdbcTemplate jdbcTemplate;

    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            return;
        }

        log.info("!!! CONCILIACION FINALIZADA. Resumen por estado:");
        jdbcTemplate.query(
                "SELECT estado, COUNT(*) AS total FROM facturas GROUP BY estado ORDER BY estado",
                (ResultSetExtractor<Void>) rs -> {
                    while (rs.next()) {
                        log.info("  {}: {} factura(s)", rs.getString("estado"), rs.getLong("total"));
                    }
                    return null;
                });

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM facturas", Long.class);
        log.info("  TOTAL: {} factura(s) en la base", total);
    }
}
