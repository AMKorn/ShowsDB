package com.andreas.showsdb.messaging;

import com.andreas.showsdb.messaging.messages.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class MessageSerializer implements Serializer<Message> {

    @Override
    public byte[] serialize(String s, Message message) {
        try {
            Map<String, Object> map = message.toMap();
            map.put("class", message.getClass().getSimpleName());
            return new ObjectMapper().writeValueAsBytes(map);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new SerializationException();
        }
    }
}
