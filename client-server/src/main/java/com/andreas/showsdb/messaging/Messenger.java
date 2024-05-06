package com.andreas.showsdb.messaging;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.service.ShowsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class Messenger {

    private final KafkaTemplate<String, EpisodeMessage> kafkaTemplate;
    private final ShowsService showsService;

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    public Messenger(KafkaTemplate<String, EpisodeMessage> kafkaTemplate, ShowsService showsService) {
        this.kafkaTemplate = kafkaTemplate;
        this.showsService = showsService;
    }

    public void sendMessage(String topic, EpisodeMessage message) {
        CompletableFuture<SendResult<String, EpisodeMessage>> future = kafkaTemplate.send(topic, message);
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
        sendMessage("new_episodes", message);
//        ShowOutputDto show = showsService.findById(episode.getShowId());
//        sendMessage("new_episodes", "New episode released: %s S%02dE%02d - %s"
//                .formatted(show.getName(),
//                        episode.getSeasonNumber(),
//                        episode.getEpisodeNumber(),
//                        episode.getName()));
    }

    public void newSeason(SeasonOutputDto season) throws NotFoundException {
//        ShowOutputDto show = showsService.findById(season.getShowId());
//        sendMessage("new_seasons", "New season announced: %s S%02d"
//                .formatted(show.getName(), season.getSeasonNumber()));
    }

    public void newShow(ShowOutputDto show) {
//        sendMessage("new_shows", "New show added: %s"
//                .formatted(show.getName()));
    }

    @KafkaListener(topics = "new_episodes", groupId = "showsDB", containerFactory = "kafkaListenerContainerFactory")
    public void newEpisodeListener(EpisodeMessage message) {
        String messageText = "[%s] %s: %s S%02dE%02d - %s"
                .formatted(message.getReleaseDate(),
                        message.getMessage(),
                        message.getShow(),
                        message.getSeasonNumber(),
                        message.getEpisodeNumber(),
                        message.getName());
        logger.info("Received Message in group showsDB: {}", messageText);
    }
}
