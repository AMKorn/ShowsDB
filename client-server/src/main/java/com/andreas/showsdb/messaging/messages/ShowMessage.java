package com.andreas.showsdb.messaging.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ShowMessage extends Message {
    private String name;

    public static ShowMessage fromJson(JsonNode json) {
        return builder()
                .text(json.path("text").asText())
                .name(json.path("name").asText())
                .build();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("name", name);
        return map;
    }
}
