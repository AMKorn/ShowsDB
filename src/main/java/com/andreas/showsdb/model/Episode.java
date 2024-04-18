package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.EpisodeInfo;
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
    private Integer episodeNumber;
    private String name;
    @Column(name = "rel_date")
    private Date releaseDate;

    public Show getShow() {
        return season.getShow();
    }

    public EpisodeInfo dto() {
        return EpisodeInfo.builder()
                .showId(season.getShow().getId())
                .seasonNumber(season.getSeasonNumber())
                .episodeNumber(episodeNumber)
                .releaseDate(releaseDate)
                .build();
    }

    @Override
    public int compareTo(Episode e) {
        int compareSeasons = season.compareTo(e.getSeason());
        return compareSeasons == 0 ? episodeNumber.compareTo(e.getEpisodeNumber()) : compareSeasons;
    }
}
