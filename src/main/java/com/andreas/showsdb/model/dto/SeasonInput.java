package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeasonInput {
    @Positive
    Integer seasonNumber;
}
