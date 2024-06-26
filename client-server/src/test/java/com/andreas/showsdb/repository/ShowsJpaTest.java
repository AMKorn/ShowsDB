package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Show;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Disabled
class ShowsJpaTest {

    @Autowired
    ShowsRepository showsRepository;

    @Test
    void testFindAllShows() {
        List<Show> shows = showsRepository.findAll();

        assertNotNull(shows);
        assertFalse(shows.isEmpty());
        assertEquals(2, shows.size());
    }

    @Test
    void testFindShowById() {
        Optional<Show> show = showsRepository.findById(1L);
        assertFalse(show.isEmpty());
        assertEquals("What We Do in the Shadows", show.orElseThrow().getName());
    }

    @Test
    void testInsertShow() {
        Show show = Show.builder()
                .name("Bojack Horseman")
                .country("United States")
                .build();

        Show savedShow = showsRepository.save(show);

        assertEquals(show, savedShow);

        List<Show> all = showsRepository.findAll();
        assertEquals(3, all.size());
        assertTrue(all.contains(savedShow));
    }

    @Test
    void testUpdateShow() {
        Show show = showsRepository.findById(1L).orElseThrow();
        show.setCountry("Canada");
        showsRepository.save(show);

        show = showsRepository.findById(1L).orElseThrow();
        assertEquals("Canada", show.getCountry());
    }

    @Test
    void testDeleteShow() {
        List<Show> shows = showsRepository.findAll();
        assertEquals(2, shows.size());

        showsRepository.deleteById(1L);

        shows = showsRepository.findAll();
        assertEquals(1, shows.size());
    }
}