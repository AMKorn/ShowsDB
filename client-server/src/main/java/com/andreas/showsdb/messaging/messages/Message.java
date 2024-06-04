package com.andreas.showsdb.messaging.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Message {
    private String text;

    public static Message fromJson(JsonNode json) {
        return builder()
                .text(json.path("text").asText())
                .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        return map;
    }
}
