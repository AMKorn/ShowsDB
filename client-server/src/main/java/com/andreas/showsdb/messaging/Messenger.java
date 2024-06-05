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
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class Messenger {

    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final ShowsService showsService;

    public void newEpisode(EpisodeOutputDto episode) throws NotFoundException {
        LocalDate releaseDate = episode.getReleaseDate();
        Date date = null;
        if(releaseDate != null) {
            Instant instant = releaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            date = Date.from(instant);
        }
        EpisodeMessage message = EpisodeMessage.builder()
                .text("New episode released")
                .show(showsService.findById(episode.getShowId()).getName())
                .seasonNumber(episode.getSeasonNumber())
                .episodeNumber(episode.getEpisodeNumber())
                .name(episode.getName())
                .releaseDate(date)
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

    public void sendBatchOrder(String importJob, String file) {
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
                log.info("Sent message=[%s] with offset=[%d]"
                        .formatted(message, result.getRecordMetadata().offset()));
            } else {
                log.info("Unable to send message=[%s] due to : %s"
                        .formatted(message, ex.getMessage()));
            }
        });
    }
}
