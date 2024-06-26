package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "show")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    @Enumerated(EnumType.STRING)
    private State state;
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL) //, orphanRemoval = true)
    @JsonIgnoreProperties({"show"})
    private List<Season> seasons;
    @OneToMany(mappedBy = "show")
    @JsonIgnoreProperties({"show"})
    private Set<MainCast> mainCast;

    public static Show translateFromDto(@Valid ShowInputDto dto) {
        return Show.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .build();
    }

    public static Show translateFromDto(@Valid ShowOutputDto dto) {
        return Show.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .state(dto.getState())
                .build();
    }

    public ShowOutputDto getInfoDto() {
        ShowOutputDto.ShowOutputDtoBuilder infoBuilder = ShowOutputDto.builder()
                .id(id)
                .name(name)
                .country(country)
                .state(state);
        int numberOfSeasons;
        int numberOfEpisodes;
        if (seasons != null) {
            numberOfSeasons = seasons.size();
            numberOfEpisodes = seasons.stream()
                    .flatMap(season -> season.getEpisodes().stream())
                    .toList()
                    .size();
        } else {
            numberOfSeasons = 0;
            numberOfEpisodes = 0;
        }
        return infoBuilder.numberOfSeasons(numberOfSeasons)
                .numberOfEpisodes(numberOfEpisodes)
                .build();
    }

    public enum State {
        UNRELEASED, AIRING, CANCELLED, FINISHED
    }
}
