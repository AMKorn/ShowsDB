package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.SeasonDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(
        name = "season",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"show", "seasonNumber"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season implements Comparable<Season> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "show")
    @JsonIgnoreProperties(value = {"seasons", "mainCast"})
    private Show show;
    @Column(name = "season_number")
    private Integer seasonNumber;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"season"})
    private List<Episode> episodes;

    public SeasonDto dto() {
        return SeasonDto.builder()
                .showId(show.getId())
                .seasonNumber(seasonNumber)
                .build();
    }

    @Override
    public int compareTo(Season s) {
        if (s.show != show) return 0;
        return seasonNumber.compareTo(s.getSeasonNumber());
    }
}
