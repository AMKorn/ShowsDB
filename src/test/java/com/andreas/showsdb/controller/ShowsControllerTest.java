package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
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
    void testSearchAll() throws URISyntaxException {
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
    void testGetShowExists() throws URISyntaxException {
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
    void testGetShowDoesNotExist() throws URISyntaxException {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/10"), Show.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(4)
    void testAddShow() throws URISyntaxException {
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
    void testModifyShow() throws URISyntaxException {
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
    void testModifyShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        Show show = new Show("Nonexistent Show", "Nonexistent Country");
        show.setId(99L);
        RequestEntity<Show> request = new RequestEntity<>(show, HttpMethod.PUT, createUri("/api/shows"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show does not exist",
                json.path("message").asText());
    }

    @Test
    @Order(7)
    void testDeleteShow() throws URISyntaxException {
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
    @Order(8)
    void testDeleteNonexistentShow() throws URISyntaxException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/99"));
        ResponseEntity<Void> response = client.exchange(request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(9)
    void testAddSeasonToShow() throws URISyntaxException {
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
    @Order(10)
    void testAddSecondSeasonToShow() throws URISyntaxException {
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
    @Order(11)
    void testAddUnnumberedSeasonToShow() throws URISyntaxException {
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
    @Order(12)
    void testAddSeasonToShowAlreadyExists() throws JsonProcessingException, URISyntaxException {
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
    @Order(13)
    void testAddSeasonToShowThatDoesNotExist() throws JsonProcessingException, URISyntaxException {
        Season season = new Season();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/99/seasons"), season, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show does not exist",
                json.path("message").asText());
    }


    @Test
    @Order(14)
    void testGetShowSeasons() throws URISyntaxException {
        ResponseEntity<Season[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Season> seasons = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(3, seasons.size());
    }

    @Test
    @Order(15)
    void testGetShowSeasonByNumber() throws URISyntaxException {
        ResponseEntity<Season> response = client.getForEntity(createUri("/api/shows/1/seasons/1"), Season.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season season = response.getBody();
        assertNotNull(season);
        assertEquals(1, season.getShow().getId());
        assertEquals(1, season.getSeasonNumber());
    }

    @Test
    @Order(16)
    void testGetNonexistentSeason() throws URISyntaxException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/15"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(17)
    void testDeleteSeason() throws URISyntaxException {
        ResponseEntity<Season[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertNotNull(response.getBody());
        int numberOfSeasons = response.getBody().length;

        client.delete(createUri("/api/shows/1/seasons/2"));

        response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertNotNull(response.getBody());
        assertEquals(numberOfSeasons - 1, response.getBody().length);
    }

    @Test
    @Order(18)
    void testDeleteNonexistentSeason() throws URISyntaxException, JsonProcessingException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/1/seasons/99"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Season does not exist",
                json.path("message").asText());
    }

    @Test
    @Order(19)
    void testDeleteAllSeasons() throws URISyntaxException {
        ResponseEntity<Season[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertNotNull(response.getBody());
        int numberOfSeasons = response.getBody().length;
        assertNotEquals(0, numberOfSeasons);

        client.delete(createUri("/api/shows/1/seasons"));

        response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);

    }

    @Test
    @Order(20)
    void testAddEpisode() throws URISyntaxException {
        // Setup
        Season season = new Season();
        season.setSeasonNumber(1);
        client.postForEntity(createUri("/api/shows/1/seasons"), season, Void.class);

        Episode episode = new Episode(1, "Pilot", Utils.parseDate("28/03/2019"));
        ResponseEntity<Episode> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episode, Episode.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        episode = response.getBody();
        assertNotNull(episode);
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals("Pilot", episode.getName());
        assertEquals(1, episode.getSeason().getSeasonNumber());
        assertEquals("What We Do in the Shadows", episode.getShow().getName());
    }

    @Test
    @Order(21)
    void testAddEmptyEpisode() throws URISyntaxException {
        ResponseEntity<Episode> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), new Episode(), Episode.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Episode episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertNull(episode.getName());
        assertEquals(1, episode.getSeason().getSeasonNumber());
        assertEquals("What We Do in the Shadows", episode.getShow().getName());

    }

    @Test
    @Order(22)
    void testAddEpisodeShowOrSeasonDoesNotExist() throws URISyntaxException, JsonProcessingException {
        Episode episode = new Episode(1, "Nonexistent episode");
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/9/seasons/9/episodes"), episode, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show does not exist",
                json.path("message").asText());

        response = client.postForEntity(createUri("/api/shows/1/seasons/9/episodes"), episode, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        json = objectMapper.readTree(response.getBody());
        assertEquals("Season does not exist",
                json.path("message").asText());
    }

    @Test
    void testGetEpisode() {

    }

    @Test
    void testGetEpisodeDoesNotExist() {
    }

    @Test
    void testModifyEpisode() {

    }

    @Test
    void testModifyEpisodeDoesNotExist() {

    }

    @Test
    void testGetAllSeasonEpisodes() throws URISyntaxException {
        ResponseEntity<Episode[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Episode> episodes = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(1, episodes.size());

    }

    @Test
    void testDeleteEpisode() {

    }

    @Test
    void testDeleteEpisodeDoesNotExist() {

    }

    @Test
    void testDeleteAllEpisodes() {

    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }

}
