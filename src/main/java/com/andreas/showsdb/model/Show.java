package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ShowInfo;
import com.andreas.showsdb.model.dto.ShowInput;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"show"})
    private List<Season> seasons;

    @OneToMany(mappedBy = "show")
    @JsonIgnoreProperties({"show"})
    Set<MainCast> mainCast;

//    public ShowInput dto(){
//        return ShowInput.builder()
//                .name(name)
//                .country(country)
//                .build();
//    }

    public static Show translateFromDto(ShowInput dto) {
        return Show.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .build();
    }

    public static Show translateFromDto(ShowInfo dto) {
        return Show.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .build();
    }

    public ShowInfo dto() {
        return ShowInfo.builder()
                .id(id)
                .name(name)
                .country(country)
                .numberOfSeasons(seasons.size())
                .numberOfEpisodes(seasons.stream()
                        .flatMap(season -> season.getEpisodes().stream())
                        .toList()
                        .size())
                .build();
    }
}
