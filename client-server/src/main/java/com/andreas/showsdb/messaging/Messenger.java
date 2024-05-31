package com.andreas.showsdb.messaging;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.messaging.messages.BatchOrder;
import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.Message;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.service.ShowsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class Messenger {

    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final ShowsService showsService;

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    public void newEpisode(EpisodeOutputDto episode) throws NotFoundException {
        EpisodeMessage message = EpisodeMessage.builder()
                .text("New episode released")
                .show(showsService.findById(episode.getShowId()).getName())
                .seasonNumber(episode.getSeasonNumber())
                .episodeNumber(episode.getEpisodeNumber())
                .name(episode.getName())
                .releaseDate(Date.valueOf(episode.getReleaseDate()))
                .build();
        sendMessage("novelties", message);
    }

    public void newShow(ShowOutputDto show) {
        ShowMessage message = ShowMessage.builder()
                .text("New show released")
                .name(show.getName())
                .build();
        sendMessage("novelties", message);
    }

    public void sendBatchOrder(String importJob, String file){
        BatchOrder batchOrder = BatchOrder.builder()
                .text(importJob)
                .filepath(file)
                .build();
        sendMessage("batch-order", batchOrder);
    }

    private void sendMessage(String topic, Message message) {
        CompletableFuture<SendResult<String, Message>> future = kafkaTemplate.send(topic, message);
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
}
