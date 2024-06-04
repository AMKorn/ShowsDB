package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(groupId = "showsDB", topics = "novelties")
public class MessageListener {
    private static final String RECEIVED_MESSAGE = "Received Message in group showsDB:";

    @KafkaHandler
    public void newEpisodeListener(EpisodeMessage message) {
        String messageText = "[%s] %s: %s S%02dE%02d - %s".formatted(
                message.getReleaseDate(),
                message.getText(),
                message.getShow(),
                message.getSeasonNumber(),
                message.getEpisodeNumber(),
                message.getName());
        log.info("{} {}", RECEIVED_MESSAGE, messageText);
    }

    @KafkaHandler
    public void newShowListener(ShowMessage message) {
        log.info("{} {}: {}", RECEIVED_MESSAGE, message.getText(), message.getName());
    }

    @KafkaHandler(isDefault = true)
    public void unknownListener(Object message) {
        log.info("{} {}", RECEIVED_MESSAGE, message);
    }
}
