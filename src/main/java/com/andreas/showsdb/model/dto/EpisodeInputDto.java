package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class EpisodeInputDto {
    @Positive(message = "Episode number must be higher than 0")
    Integer episodeNumber;
    @Size(max = 255, message = "Episode name must not be longer than 255 characters")
    String name;
    Date releaseDate;
}
