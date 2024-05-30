package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonOutputDto implements Serializable {
    @NotNull(message = "Show ID must not be null")
    Long showId;
    @NotNull(message = "Season number must not be null")
    @Positive(message = "Season number must be higher than 0")
    Integer seasonNumber;
    @NotNull(message = "Number of episodes must not be null")
    @PositiveOrZero(message = "Number of episodes must be higher than or equal to 0")
    Integer numberOfEpisodes;
}
