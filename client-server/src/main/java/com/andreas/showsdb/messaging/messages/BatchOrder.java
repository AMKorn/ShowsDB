package com.andreas.showsdb.messaging.messages;

import com.andreas.showsdb.util.Utils;
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
public class BatchOrder extends Message {
    String filePath;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("filePath", filePath);
        return map;
    }

    public static BatchOrder fromJson(JsonNode json) {
        return builder()
                .text(json.path("text").asText())
                .filePath(json.path("filePath").asText())
                .build();
    }
}
