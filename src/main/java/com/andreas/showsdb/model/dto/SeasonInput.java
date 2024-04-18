package com.andreas.showsdb.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = SeasonInput.SeasonInputBuilder.class)
public class SeasonInput {
    @Positive
    @JsonProperty("seasonNumber")
    Integer seasonNumber;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SeasonInputBuilder{

    }
}
