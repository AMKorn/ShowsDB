package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeOutputDto implements Serializable {
    @NotNull(message = "Show ID must not be null")
    Long showId;
    @NotNull(message = "Season number must not be null")
    @Positive(message = "Season number must be higher than 0")
    Integer seasonNumber;
    @NotNull(message = "Episode number must not be null")
    @Positive(message = "Episode number must be higher than 0")
    Integer episodeNumber;
    @Size(max = 255, message = "Episode name must not be longer than 255 characters")
    String name;
    LocalDate releaseDate;
}
