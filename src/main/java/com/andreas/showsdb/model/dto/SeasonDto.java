package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeasonDto {
    @NotNull
    Long showId;
    @NotNull
    @Positive
    Integer seasonNumber;
}
