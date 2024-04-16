package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
public class Season implements Comparable<Season> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "show")
    @JsonIgnoreProperties(value = {"seasons"})
    private Show show;
    @Column(name = "season_number")
    private Integer seasonNumber;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"season"})
    private List<Episode> episodes;

    public Season(Show show, Integer seasonNumber) {
        this.show = show;
        this.seasonNumber = seasonNumber;
    }

    @Override
    public int compareTo(Season s) {
        if (s.show != show) return 0;
        return seasonNumber.compareTo(s.getSeasonNumber());
    }
}
