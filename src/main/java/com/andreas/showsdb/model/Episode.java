package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
@JsonIgnoreProperties("show")
public class Episode implements Comparable<Episode> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "season")
    @JsonIgnoreProperties(value = {"episodes"})
    private Season season;
    private Integer episodeNumber;
    private String name;
    @Column(name = "relDate")
    private Date releaseDate;

    public Episode(Season season, Integer episodeNumber, String name, Date releaseDate) {
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.name = name;
        this.releaseDate = releaseDate;
    }

    public Episode(Integer episodeNumber, String name, Date releaseDate) {
        this.episodeNumber = episodeNumber;
        this.name = name;
        this.releaseDate = releaseDate;
    }

    public Episode(Integer episodeNumber, String name) {
        this.episodeNumber = episodeNumber;
        this.name = name;
    }

    public Show getShow() {
        return getSeason().getShow();
    }

    @Override
    public int compareTo(Episode e) {
        int compareSeasons = season.compareTo(e.getSeason());
        return compareSeasons == 0 ? episodeNumber.compareTo(e.getEpisodeNumber()) : compareSeasons;
    }
}
