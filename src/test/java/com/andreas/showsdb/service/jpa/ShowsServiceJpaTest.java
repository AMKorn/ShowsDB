package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.ShowsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShowsServiceJpaTest {

    @Autowired
    ShowsRepository showsRepository;

    @Test
    void testFindAll() {
        List<Show> shows = showsRepository.findAll();

        assertNotNull(shows);
        assertFalse(shows.isEmpty());
    }

    @Test
    void testInsert() {
        Show show = new Show("What We Do in the Shadows", 10);

        Show savedShow = showsRepository.save(show);

        assertEquals(show, savedShow);

    }


}