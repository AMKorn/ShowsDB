package com.andreas.showsdb.messaging.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class EpisodeMessage {
    private String message;
    String show;
    Integer seasonNumber;
    Integer episodeNumber;
    String name;
    Date releaseDate;
}
