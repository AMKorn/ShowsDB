package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MainCastDto {
    @NotNull(message = "Actor id may not be null")
    Long actorId;
    @NotNull(message = "Show id may not be null")
    Long showId;
    @NotNull(message = "Character may not be null")
    String character;
}
