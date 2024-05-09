package com.andreas.showsdb.controller;

import com.andreas.showsdb.batch.BatchProcessingException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImportController {
    private final JobLauncher jobLauncher;
    private final Job showJob;
    private final Job episodeJob;

    public ImportController(JobLauncher jobLauncher,
                            @Qualifier("importShowJob") Job showJob,
                            @Qualifier("importEpisodeJob") Job episodeJob) {
        this.jobLauncher = jobLauncher;
        this.showJob = showJob;
        this.episodeJob = episodeJob;
    }

    @PostMapping("/imports/shows")
    public void importAll() throws BatchProcessingException {
        try {
            jobLauncher.run(showJob, new JobParameters());
        } catch (JobParametersInvalidException e) {
            throw new BatchProcessingException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException e) {
            throw new BatchProcessingException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/imports/episodes")
    public void importEpisodes() throws BatchProcessingException {
        try {
            jobLauncher.run(episodeJob, new JobParameters());
        } catch (JobParametersInvalidException e) {
            throw new BatchProcessingException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException e) {
            throw new BatchProcessingException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
