package com.andreas.showsdb.batch;

import com.andreas.showsdb.model.Show;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportShowCompletionNotificationListener implements JobExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ImportShowCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("!!! JOB FINISHED! Time to verify the results");

            String query = "SELECT `name`, `country` FROM `show`";
            jdbcTemplate.query(query, (rs, row) -> Show.builder()
                            .name(rs.getString(1))
                            .country(rs.getString(2))
                            .build())
                    .forEach(show -> logger.info("Found < {} > in the database.", show));
        }
    }
}
