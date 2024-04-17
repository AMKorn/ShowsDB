package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ShowDto;
import com.andreas.showsdb.model.dto.ShowDtoId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
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

    public ShowDto dto(){
        return ShowDto.builder()
                .name(name)
                .country(country)
                .build();
    }

    public ShowDtoId dtoId(){
        return ShowDtoId.builder()
                .id(id)
                .name(name)
                .country(country)
                .build();
    }
}
