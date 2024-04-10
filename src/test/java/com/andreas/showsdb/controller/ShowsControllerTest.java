package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
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
        assertEquals("United States", shows.get(0).getCountry());
        assertEquals("United States", shows.get(1).getCountry());

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
        assertEquals("United States", show.getCountry());
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
        Show show = new Show("Bojack Horseman", "United States");

        ResponseEntity<Show> response = client.postForEntity(createUri("/api/shows"), show, Show.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Show show1 = response.getBody();
        assertNotNull(show1);
        assertEquals(3, show1.getId());
        assertEquals("Bojack Horseman", show1.getName());
        assertEquals("United States", show1.getCountry());
    }

    @Test
    @Order(5)
    void testModifyShow() {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/2"), Show.class);
        Show show = response.getBody();

        assertNotNull(show);
        show.setCountry("Canada");

        client.put(createUri("/api/shows"), show);

        response = client.getForEntity(createUri("/api/shows/2"), Show.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        show = response.getBody();
        assertNotNull(show);
        assertEquals("Canada", show.getCountry());
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

    @Test
    @Order(7)
    void testAddSeasonToShow() {
        Season season = new Season();
        season.setSeasonNumber(1);
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(1, seasonResponse.getSeasonNumber());
    }

    @Test
    @Order(8)
    void testAddSecondSeasonToShow() {
        Season season = new Season();
        season.setSeasonNumber(2);
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(2, seasonResponse.getSeasonNumber());
    }

    @Test
    @Order(9)
    void testAddUnnumberedSeasonToShow() {
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), new Season(), Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(3, seasonResponse.getSeasonNumber());

    }

    @Test
    @Order(10)
    void testAddSeasonToShowAlreadyExists() throws JsonProcessingException {
        Season season = new Season();
        season.setSeasonNumber(1);
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show 'What We Do in the Shadows' already has a Season 1",
                json.path("message").asText());

    }

    @Test
    @Order(11)
    void testAddSeasonToShowThatDoesNotExist() {
        Season season = new Season();
    }


    @Test
    @Order(12)
    void testGetShowSeasons() {
        ResponseEntity<Season[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Season> seasons = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(3, seasons.size());
    }

    @Test
    @Order(13)
    void testGetShowSeasonByNumber() {
        ResponseEntity<Season> response = client.getForEntity(createUri("/api/shows/1/seasons/1"), Season.class);
    }

    private String createUri(String uri) {
        return "http://localhost:" + port + uri;
    }

}