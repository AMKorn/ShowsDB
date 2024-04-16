package com.andreas.showsdb.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MainCastDto {
    Long actorId;
    Long showId;
    String character;
}
