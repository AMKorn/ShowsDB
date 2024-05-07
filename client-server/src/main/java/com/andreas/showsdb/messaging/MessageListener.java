package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(groupId = "showsDB", topics = "novelties")
public class MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private static final String RECEIVED_MESSAGE = "Received Message in group showsDB:";

    @KafkaHandler
    public void newEpisodeListener(EpisodeMessage message) {
        String messageText = "[%s] %s: %s S%02dE%02d - %s"
                .formatted(message.getReleaseDate(),
                        message.getMessage(),
                        message.getShow(),
                        message.getSeasonNumber(),
                        message.getEpisodeNumber(),
                        message.getName());
        logger.info("{} {}", RECEIVED_MESSAGE, messageText);
    }

    @KafkaHandler
    public void newShowListener(ShowMessage message) {
        logger.info("{} {}: {}", RECEIVED_MESSAGE,
                message.getMessage(),
                message.getName());
    }

    @KafkaHandler(isDefault = true)
    public void unknownListener(Object message) {
        logger.info("{} {}", RECEIVED_MESSAGE, message);
    }
}
