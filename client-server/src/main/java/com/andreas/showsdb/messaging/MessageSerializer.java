package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MessageSerializer implements Serializer<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageSerializer.class);

    @Override
    public byte[] serialize(String s, Message message) {
        try {
            Map<String, Object> map = message.toMap();
            map.put("class", message.getClass().getSimpleName());
            return new ObjectMapper().writeValueAsBytes(map);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw new SerializationException();
        }
    }
}
