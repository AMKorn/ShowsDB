package com.andreas.showsdb.messaging.messages;

import com.andreas.showsdb.util.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class EpisodeMessage extends Message {
    private String show;
    private Integer seasonNumber;
    private Integer episodeNumber;
    private String name;
    private Date releaseDate;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("show", show);
        map.put("seasonNumber", seasonNumber);
        map.put("episodeNumber", episodeNumber);
        map.put("name", name);
        map.put("releaseDate", Utils.dateToString(releaseDate));
        return map;
    }

    public static EpisodeMessage fromJson(JsonNode json) {
        return builder()
                .text(json.path("text").asText())
                .show(json.path("show").asText())
                .seasonNumber(json.path("seasonNumber").asInt())
                .episodeNumber(json.path("episodeNumber").asInt())
                .name(json.path("name").asText())
                .releaseDate(Utils.parseDate(json.path("releaseDate").asText()))
                .build();
    }
}


