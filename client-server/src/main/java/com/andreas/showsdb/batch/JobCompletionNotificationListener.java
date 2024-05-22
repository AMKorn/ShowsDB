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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("!!! JOB FINISHED! Time to verify the results");

            String jobName = jobExecution.getJobInstance().getJobName();
            switch (jobName) {
                case "importShowJob" -> showImportedShows();
                case "importEpisodeJob" -> showImportedEpisodes();
                default -> logger.info("Nothing to verify");
            }
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            logger.error("FAILURE STATE! Job could not be completed.");
        }
        String filepath = jobExecution.getJobParameters().getString("filepath");
        if (filepath != null) {
            logger.info("Removing file from file system");
            File file = new File(filepath);
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                logger.error("Could not delete file {}, please make sure to remove it manually.", filepath);
            }
        }
    }

    private void showImportedEpisodes() {
        String query = "SELECT `name` FROM `episode`";
        jdbcTemplate.query(query, (rs, rowNum) -> rs.getString(1))
                .forEach(episode -> logger.info("Found < {} > in the database.", episode));
    }

    private void showImportedShows() {
        String query = "SELECT `id`, `name`, `country` FROM `show`";
        jdbcTemplate.query(query, (rs, row) -> Show.builder()
                        .id(rs.getLong(1))
                        .name(rs.getString(2))
                        .country(rs.getString(3))
                        .build())
                .forEach(show -> logger.info("Found < {} > in the database.", show));
    }
}
