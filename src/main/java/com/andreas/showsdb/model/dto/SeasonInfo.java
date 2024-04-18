package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeasonInfo {
    @NotNull
    Long showId;
    @NotNull
    @Positive
    Integer seasonNumber;
    @NotNull
    @PositiveOrZero
    Integer numberOfEpisodes;
}
