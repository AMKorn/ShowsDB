package com.andreas.showsdb.batch;

import com.andreas.showsdb.controller.ImportController;
import com.andreas.showsdb.messaging.messages.BatchOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(groupId = "showsDB", topics = "batch-order")
public class BatchOrderListener {
    private static final Logger logger = LoggerFactory.getLogger(BatchOrderListener.class);
    private static final String RECEIVED_MESSAGE = "Received new batch order:";

    private final JobLauncher jobLauncher;
    private final Job showJob;
    private final Job episodeJob;


    public BatchOrderListener(JobLauncher jobLauncher,
                            @Qualifier("importShowJob") Job showJob,
                            @Qualifier("importEpisodeJob") Job episodeJob) {
        this.jobLauncher = jobLauncher;
        this.showJob = showJob;
        this.episodeJob = episodeJob;
    }


    @KafkaHandler
    public void batchOrderListener(BatchOrder message) {
        logger.info("{} {}", RECEIVED_MESSAGE, message);
        Job jobToDo = switch (message.getText()) {
            case "shows" -> showJob;
            case "episodes" -> episodeJob;
            default -> throw new IllegalStateException("Unexpected value: " + message.getText());
        };
        try {
            jobLauncher.run(jobToDo, new JobParameters());
        } catch (JobExecutionException e) {
            logger.error("Error while importing: {}", e.getMessage());
        }

    }

    @KafkaHandler(isDefault = true)
    public void unknownListener(Object message) {
        logger.info("{} {}", RECEIVED_MESSAGE, message);
    }
}
