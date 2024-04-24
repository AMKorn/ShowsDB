package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(
        name = "episode",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"season", "episodeNumber"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties("show")
public class Episode implements Comparable<Episode> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "season")
    @JsonIgnoreProperties(value = {"episodes"})
    private Season season;
    @Column(name = "episode_number")
    private Integer number;
    private String name;
    @Column(name = "rel_date")
    private Date releaseDate;

    public Show getShow() {
        return season.getShow();
    }

    public EpisodeOutputDto getInfoDto() {
        return EpisodeOutputDto.builder()
                .showId(season.getShow().getId())
                .seasonNumber(season.getNumber())
                .episodeNumber(number)
                .name(name)
                .releaseDate(releaseDate)
                .build();
    }

    @Override
    public int compareTo(Episode e) {
        int compareSeasons = season.compareTo(e.getSeason());
        return compareSeasons == 0 ? number.compareTo(e.getNumber()) : compareSeasons;
    }
}
