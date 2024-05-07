package com.andreas.showsdb.messaging;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.service.ShowsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class Messenger {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShowsService showsService;

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    public Messenger(KafkaTemplate<String, Object> kafkaTemplate, ShowsService showsService) {
        this.kafkaTemplate = kafkaTemplate;
        this.showsService = showsService;
    }

    public void sendMessage(String topic, Object message) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);
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

    public void newEpisode(EpisodeOutputDto episode) throws NotFoundException {
        EpisodeMessage message = EpisodeMessage.builder()
                .message("New episode released")
                .show(showsService.findById(episode.getShowId()).getName())
                .seasonNumber(episode.getSeasonNumber())
                .episodeNumber(episode.getEpisodeNumber())
                .name(episode.getName())
                .releaseDate(episode.getReleaseDate())
                .build();
        sendMessage("novelties", message);
    }

    public void newShow(ShowOutputDto show) {
        ShowMessage message = ShowMessage.builder()
                .message("New show released")
                .name(show.getName())
                .build();
        sendMessage("novelties", message);
    }
}
