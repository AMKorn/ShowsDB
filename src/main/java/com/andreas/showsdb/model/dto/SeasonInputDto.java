package com.andreas.showsdb.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = SeasonInputDto.SeasonInputBuilder.class)
public class SeasonInputDto {
    @Positive(message = "Season number must be higher than 0")
    @JsonProperty("seasonNumber")
    Integer seasonNumber;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SeasonInputBuilder {

    }
}
