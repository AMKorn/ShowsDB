package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EpisodesControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(20)
    void testAddEpisode() throws URISyntaxException {
        // Setup. Can't use @BeforeAll because client is not static
        Season season = new Season();
        season.setSeasonNumber(1);
        client.postForEntity(createUri("/api/shows/1/seasons"), season, Void.class);

        Episode episode = Episode.builder()
                .episodeNumber(1)
                .name("Pilot")
                .releaseDate(Utils.parseDate("28/03/2019"))
                .build();
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
        Episode episode = Episode.builder()
                .episodeNumber(1)
                .name("Nonexistent episode")
                .build();

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
    @Order(23)
    void testGetAllSeasonEpisodes() throws URISyntaxException {
        ResponseEntity<Episode[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Episode> episodes = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, episodes.size());
    }

    @Test
    @Order(24)
    void testGetEpisode() throws URISyntaxException {
        ResponseEntity<Episode> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/1"),
                Episode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Episode episode = response.getBody();

        assertNotNull(episode);
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals("Pilot", episode.getName());
        assertEquals(1, episode.getSeason().getSeasonNumber());
        assertEquals("What We Do in the Shadows", episode.getShow().getName());
    }

    @Test
    @Order(25)
    void testGetNonexistentEpisode() throws URISyntaxException {
        ResponseEntity<Episode> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/99"),
                Episode.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(26)
    void testModifyEpisode() throws URISyntaxException {
        ResponseEntity<Episode> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/2"),
                Episode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Episode episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());

        episode.setName("City Council");
        episode.setReleaseDate(Utils.parseDate("04/04/2019"));

        RequestEntity<Episode> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        response = client.exchange(request, Episode.class);

        episode = response.getBody();

        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertEquals("City Council", episode.getName());
        assertEquals(Utils.parseDate("04/04/2019"), episode.getReleaseDate());
        assertEquals(1, episode.getSeason().getSeasonNumber());
        assertEquals("What We Do in the Shadows", episode.getShow().getName());
    }

    @Test
    @Order(27)
    void testModifyEpisodeDoesNotExist() throws URISyntaxException, JsonProcessingException {
        Episode episode = Episode.builder()
                .episodeNumber(3)
                .name("Werewolf Feud")
                .build();

        RequestEntity<Episode> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode does not exist, or trying to modify episode number.",
                json.path("message").asText());
    }

    @Test
    @Order(28)
    void testModifyEpisodeNumber() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<Episode> getResponse = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/2"),
                Episode.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, getResponse.getHeaders().getContentType());

        Episode episode = getResponse.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        episode.setEpisodeNumber(1);

        RequestEntity<Episode> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        ResponseEntity<String> putResponse = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, getResponse.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(putResponse.getBody());
        assertEquals("It's not possible to modify episode number.",
                json.path("message").asText());

    }

    @Test
    @Order(29)
    void testDeleteEpisode() throws URISyntaxException {
        ResponseEntity<Episode[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Episode[] body = response.getBody();
        assertNotNull(body);
        assertEquals(2, response.getBody().length);

        client.delete(createUri("/api/shows/1/seasons/1/episodes/1"));


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
    }

    @Test
    @Order(30)
    void testDeleteEpisodeDoesNotExist() throws URISyntaxException, JsonProcessingException {

        RequestEntity<Episode> request = new RequestEntity<>(HttpMethod.DELETE,
                createUri("/api/shows/1/seasons/1/episodes/1"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode does not exist",
                json.path("message").asText());
    }

    @Test
    @Order(31)
    void testDeleteAllEpisodes() throws URISyntaxException {
        ResponseEntity<Episode[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Episode[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, response.getBody().length);

        client.delete(createUri("/api/shows/1/seasons/1/episodes"));


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                Episode[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.length);
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
