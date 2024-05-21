package com.andreas.showsdb.batch;

import com.andreas.showsdb.messaging.messages.BatchOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(groupId = "showsDB", topics = "batch-order")
public class BatchOrderListener {
    public static final String SHOWS = "shows";
    public static final String EPISODES = "episodes";

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
            case SHOWS -> showJob;
            case EPISODES -> episodeJob;
            default -> throw new IllegalStateException("Unexpected value: " + message.getText());
        };
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("filepath", message.getFilepath())
                .toJobParameters();
        try {
            jobLauncher.run(jobToDo, jobParameters);
        } catch (JobExecutionException e) {
            logger.error("Error while importing: {}", e.getMessage());
        }

    }

    @KafkaHandler(isDefault = true)
    public void unknownListener(Object message) {
        logger.info("{} {}", RECEIVED_MESSAGE, message);
    }
}
