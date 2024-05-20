package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.BatchOrder;
import com.andreas.showsdb.messaging.messages.EpisodeMessage;
import com.andreas.showsdb.messaging.messages.Message;
import com.andreas.showsdb.messaging.messages.ShowMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageDeserializer implements Deserializer<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

    @Override
    public Message deserialize(String topic, byte[] bytes) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(bytes);
            String messageClass = jsonNode.get("class").asText();
            Message message;
            switch (messageClass) {
                case "ShowMessage" -> message = ShowMessage.fromJson(jsonNode);
                case "EpisodeMessage" -> message = EpisodeMessage.fromJson(jsonNode);
                case "BatchOrder" -> message = BatchOrder.fromJson(jsonNode);
                default -> message = Message.fromJson(jsonNode);
            }
            return message;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SerializationException();
        }
    }
}
