package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.SeasonOutputDto;
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
                @UniqueConstraint(columnNames = {"show", "season_number"})
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
    private Show show;
    @Column(name = "season_number")
    private Integer number;

    @OneToMany(mappedBy = "season", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Episode> episodes;

    public SeasonOutputDto getInfoDto() {
        SeasonOutputDto.SeasonOutputDtoBuilder seasonInfoBuilder = SeasonOutputDto.builder()
                .showId(show.getId())
                .seasonNumber(number);
        int numberOfEpisodes;
        try {
            numberOfEpisodes = episodes.size();
        } catch (NullPointerException e) {
            numberOfEpisodes = 0;
        }
        return seasonInfoBuilder.numberOfEpisodes(numberOfEpisodes)
                .build();
    }

    @Override
    public int compareTo(Season s) {
        if (s.show != show) return 0;
        return number.compareTo(s.getNumber());
    }
}
