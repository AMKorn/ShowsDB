package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.BatchOrder;
import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.Message;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class MessageDeserializer implements Deserializer<Message> {

    @Override
    public Message deserialize(String topic, byte[] bytes) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(bytes);
            String messageClass = jsonNode.get("class").asText();
            return switch (messageClass) {
                case "ShowMessage" -> ShowMessage.fromJson(jsonNode);
                case "EpisodeMessage" -> EpisodeMessage.fromJson(jsonNode);
                case "BatchOrder" -> BatchOrder.fromJson(jsonNode);
                default -> Message.fromJson(jsonNode);
            };
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SerializationException();
        }
    }
}
