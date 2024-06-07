package com.andreas.showsdb.model.dto;

import com.andreas.showsdb.model.Show;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowOutputDto implements Serializable {
    @NotNull(message = "Show ID must not be null")
    Long id;
    @NotNull(message = "Show name must not be null")
    @Size(max = 255, message = "Show name may not be longer than 255 characters")
    String name;
    @Size(max = 255, message = "Country name may not be longer than 255 characters")
    String country;
    @NotNull(message = "Number of seasons must not be null")
    @PositiveOrZero(message = "Number of seasons must be higher than or equal to 0")
    Integer numberOfSeasons;
    @NotNull(message = "Number of episodes must not be null")
    @PositiveOrZero(message = "Number of episodes must be higher than or equal to 0")
    Integer numberOfEpisodes;
    @NotNull(message = "Show state must not be null")
    Show.State state;
}
