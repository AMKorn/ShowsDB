package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ShowInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {
    @Test
    void testCreate() {
        Show show = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();

        assertNotNull(show);
        assertEquals(0L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals("United States", show.getCountry());
    }

    @Test
    void testEqual() {
        Show show1 = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();
        Show show2 = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();
        Show show3 = Show.builder()
                .id(1L)
                .name("The Good Place")
                .country("United States")
                .build();
        Show show4 = Show.builder()
                .name("What We Do in the Shadows")
                .country("United States")
                .build();


        assertEquals(show1, show2);
        assertNotEquals(show1, show3);
        assertNotEquals(show1, show4);
    }

    @Test
    void testDto() {
        Show show = Show.builder()
                .name("What We Do in the Shadows")
                .country("United States")
                .build();
        Season season1 = Season.builder()
                .seasonNumber(1)
                .build();
        Season season2 = Season.builder()
                .seasonNumber(2)
                .build();
        Episode episode1 = Episode.builder()
                .episodeNumber(1)
                .name("Pilot")
                .build();
        Episode episode2 = Episode.builder()
                .episodeNumber(2)
                .name("City Council")
                .build();
        Episode episode3 = Episode.builder()
                .episodeNumber(1)
                .name("Season 2 premiere")
                .build();
        List<Episode> firstSeasonEpisodes = List.of(episode1, episode2);
        season1.setEpisodes(firstSeasonEpisodes);

        List<Episode> secondSeasonEpisodes = List.of(episode3);
        season2.setEpisodes(secondSeasonEpisodes);

        List<Season> seasons = List.of(season1, season2);

        show.setSeasons(seasons);

        ShowInfo showInfo = show.getInfoDto();

        assertEquals("What We Do in the Shadows", showInfo.getName());
        assertEquals("United States", showInfo.getCountry());
        assertEquals(2, showInfo.getNumberOfSeasons());
        assertEquals(3, showInfo.getNumberOfEpisodes());

    }
}