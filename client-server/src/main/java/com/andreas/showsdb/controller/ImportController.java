package com.andreas.showsdb.controller;

import com.andreas.showsdb.batch.BatchProcessingException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ImportController {
    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    private final JobLauncher jobLauncher;
    private final Job showJob;
    private final Job episodeJob;

    @Value("${showsdb.files}")
    private String filePath;

    public ImportController(JobLauncher jobLauncher,
                            @Qualifier("importShowJob") Job showJob,
                            @Qualifier("importEpisodeJob") Job episodeJob) {
        this.jobLauncher = jobLauncher;
        this.showJob = showJob;
        this.episodeJob = episodeJob;
    }

    @PostMapping("/imports/shows")
    public void importShows(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        logger.info("Correctly entered to import shows");
        String savedFile = Utils.saveFile(file, filePath);
        logger.info("created file: {}", savedFile);
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
