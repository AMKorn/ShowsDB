package com.andreas.showsdb.batch;

import com.andreas.showsdb.model.Show;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    private static void accept(String show) {
        log.info("Found < {} > in the database.", show);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            String jobName = jobExecution.getJobInstance().getJobName();
            switch (jobName) {
                case "importShowJob" -> acceptImportedShows();
                case "importEpisodeJob" -> acceptImportedEpisodes();
                default -> log.info("Nothing to verify");
            }
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("FAILURE STATE! Job could not be completed.");
        }
        String filepath = jobExecution.getJobParameters().getString("filepath");
        if (filepath != null) {
            log.info("Removing file from file system");
            File file = new File(filepath);
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("Could not delete file {}, please make sure to remove it manually.", filepath);
            }
        }
    }

    private void acceptImportedEpisodes() {
        String query = "SELECT `name` FROM `episode`";
        jdbcTemplate.query(query, (rs, rowNum) -> rs.getString(1))
                .forEach(JobCompletionNotificationListener::accept);
    }

    private void acceptImportedShows() {
        String query = "SELECT `id`, `name`, `country` FROM `show`";
        jdbcTemplate.query(query, (rs, row) -> Show.builder()
                        .id(rs.getLong(1))
                        .name(rs.getString(2))
                        .country(rs.getString(3))
                        .build())
                .stream().map(Show::getName)
                .forEach(JobCompletionNotificationListener::accept);
    }
}
