package com.andreas.showsdb.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {
    @Test
    void testCreate() {
        Show show = new Show(0L, "What We Do in the Shadows", 10);

        assertNotNull(show);
        assertEquals(0L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals(10, show.getEpisodes());
    }

    @Test
    void testCreate2() {
        Show show = new Show("What We Do in the Shadows", 10);

        assertNotNull(show);
        assertNull(show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals(10, show.getEpisodes());
    }

    @Test
    void testEqual() {
        Show show1 = new Show(0L, "What We Do in the Shadows", 10);
        Show show2 = new Show(0L, "What We Do in the Shadows", 10);
        Show show3 = new Show(1L, "The Good Place", 40);

        assertEquals(show1, show2);
        assertNotEquals(show1, show3);
    }
}