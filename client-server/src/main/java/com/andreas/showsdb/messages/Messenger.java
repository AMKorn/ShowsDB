package com.andreas.showsdb.messages;

import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.service.ShowsService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class Messenger {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ShowsService showsService;

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    public Messenger(KafkaTemplate<String, String> kafkaTemplate, ShowsService showsService) {
        this.kafkaTemplate = kafkaTemplate;
        this.showsService = showsService;
    }

    public void sendMessage(String topic, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[%s] with offset=[%d]"
                        .formatted(message, result.getRecordMetadata().offset()));
            } else {
                logger.info("Unable to send message=[%s] due to : %s"
                        .formatted(message, ex.getMessage()));
            }
        });
    }

    @SneakyThrows
    public void newEpisode(EpisodeOutputDto savedEpisode) {
        ShowOutputDto show = showsService.findById(savedEpisode.getShowId());
        sendMessage("new_episodes", "New episode released: %s S%02dE%02d - %s"
                .formatted(show.getName(),
                        savedEpisode.getSeasonNumber(),
                        savedEpisode.getEpisodeNumber(),
                        savedEpisode.getName()));
    }
}
