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
public class EpisodeMessage {
    private String message;
    private String show;
    private Integer seasonNumber;
    private Integer episodeNumber;
    private String name;
    private Date releaseDate;
}


