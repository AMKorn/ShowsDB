package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Show;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShowsControllerTest {
    @Autowired
    private TestRestTemplate client;

    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testSearchAll() {
        ResponseEntity<Show[]> response =
                client.getForEntity(createUri("/api/shows"), Show[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Show> shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, shows.size());
        assertEquals(1L, shows.get(0).getId());
        assertEquals(2L, shows.get(1).getId());
        assertEquals("What We Do in the Shadows", shows.get(0).getName());
        assertEquals("The Good Place", shows.get(1).getName());
        assertEquals(2019, shows.get(0).getRelease());
        assertEquals(2017, shows.get(1).getRelease());

    }

    @Test
    @Order(2)
    void testGetShowExists() {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/1"), Show.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Show show = response.getBody();
        assertNotNull(show);
        assertEquals(1L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals(2019, show.getRelease());
    }

    @Test
    @Order(3)
    void testGetShowDoesNotExist() {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/10"), Show.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(4)
    void testAddShow() {
        Show show = new Show("Bojack Horseman", 60);

        ResponseEntity<Show> response = client.postForEntity(createUri("/api/shows"), show, Show.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Show show1 = response.getBody();
        assertNotNull(show1);
        assertEquals(3, show1.getId());
        assertEquals("Bojack Horseman", show1.getName());
        assertEquals(60, show1.getRelease());
    }

    @Test
    @Order(5)
    void testModifyShow() {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/2"), Show.class);
        Show show = response.getBody();

        assertNotNull(show);
        show.setRelease(50);

        client.put(createUri("/api/shows"), show);

        response = client.getForEntity(createUri("/api/shows/2"), Show.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        show = response.getBody();
        assertNotNull(show);
        assertEquals(50, show.getRelease());
    }

    @Test
    @Order(6)
    void testDeleteShow() {
        ResponseEntity<Show[]> response = client.getForEntity(createUri("/api/shows"), Show[].class);
        List<Show> shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(3, shows.size());

        client.delete(createUri("/api/shows/3"));

        response = client.getForEntity(createUri("/api/shows"), Show[].class);
        shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, shows.size());
        Optional<Show> show = shows.stream().filter(s -> s.getId() == 3).findFirst();
        assertTrue(show.isEmpty());
    }

    private String createUri(String uri) {
        return "http://localhost:" + port + uri;
    }

}