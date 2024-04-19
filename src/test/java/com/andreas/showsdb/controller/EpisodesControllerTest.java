package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.EpisodeInfo;
import com.andreas.showsdb.model.dto.EpisodeInput;
import com.andreas.showsdb.model.dto.SeasonInput;
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
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class EpisodesControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddEpisode() throws URISyntaxException {
        // Setup. Can't use @BeforeAll because client is not static
        SeasonInput season = SeasonInput.builder()
                .seasonNumber(1)
                .build();
        client.postForEntity(createUri("/api/shows/1/seasons"), season, Void.class);

        EpisodeInput episodeInput = EpisodeInput.builder()
                .episodeNumber(1)
                .name("Pilot")
                .releaseDate(Utils.parseDate("28/03/2019"))
                .build();
        ResponseEntity<EpisodeInfo> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInput, EpisodeInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo episodeInfo = response.getBody();
        assertNotNull(episodeInfo);
        assertEquals(1, episodeInfo.getEpisodeNumber());
        assertEquals("Pilot", episodeInfo.getName());
        assertEquals(1, episodeInfo.getSeasonNumber());
        assertEquals(1L, episodeInfo.getShowId());
    }

    @Test
    @Order(2)
    void testAddEmptyEpisode() throws URISyntaxException {
        EpisodeInput episodeInput = EpisodeInput.builder().build();
        ResponseEntity<EpisodeInfo> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInput, EpisodeInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertNull(episode.getName());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(3)
    void testAddEpisodeAlreadyExists() throws URISyntaxException, JsonProcessingException {
        EpisodeInput episodeInput = EpisodeInput.builder()
                .episodeNumber(1)
                .name("Another name")
                .releaseDate(new Date())
                .build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInput, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Season already has an episode 1",
                json.path("message").asText());
        assertEquals(1, json.path("episode").path("showId").asInt());
        assertEquals(1, json.path("episode").path("seasonNumber").asInt());
        assertEquals(1, json.path("episode").path("episodeNumber").asInt());
        assertEquals("Pilot", json.path("episode").path("name").asText());
        assertEquals("2019-03-27T23:00:00.000+00:00",
                json.path("episode").path("releaseDate").asText());
    }

    @Test
    @Order(4)
    void testAddEpisodeShowOrSeasonDoesNotExist() throws URISyntaxException, JsonProcessingException {
        EpisodeInput episode = EpisodeInput.builder()
                .episodeNumber(1)
                .name("Nonexistent episode")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/9/seasons/9/episodes"), episode, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show not found",
                json.path("message").asText());

        response = client.postForEntity(createUri("/api/shows/1/seasons/9/episodes"), episode, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        json = objectMapper.readTree(response.getBody());
        assertEquals("Season not found",
                json.path("message").asText());
    }

    @Test
    @Order(5)
    void testGetAllSeasonEpisodes() throws URISyntaxException {
        ResponseEntity<EpisodeInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<EpisodeInfo> episodes = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, episodes.size());
    }

    @Test
    @Order(6)
    void testGetEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeInfo> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/1"),
                EpisodeInfo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo episode = response.getBody();

        assertNotNull(episode);
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals("Pilot", episode.getName());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(7)
    void testGetNonexistentEpisode() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/99"),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode not found",
                json.path("message").asText());
    }

    @Test
    @Order(8)
    void testModifyEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeInfo> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/2"),
                EpisodeInfo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());

        EpisodeInput episodeInput = EpisodeInput.builder()
                .episodeNumber(episode.getEpisodeNumber())
                .name("City Council")
                .releaseDate(Utils.parseDate("04/04/2019"))
                .build();

        RequestEntity<EpisodeInput> request = new RequestEntity<>(episodeInput, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        response = client.exchange(request, EpisodeInfo.class);

        episode = response.getBody();

        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertEquals("City Council", episode.getName());
        assertEquals(Utils.parseDate("04/04/2019"), episode.getReleaseDate());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(9)
    void testModifyEpisodeDoesNotExist() throws URISyntaxException, JsonProcessingException {
        EpisodeInput episode = EpisodeInput.builder()
                .episodeNumber(3)
                .name("Werewolf Feud")
                .build();

        RequestEntity<EpisodeInput> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode not found or trying to modify episode number.",
                json.path("message").asText());
    }

    @Test
    @Order(10)
    void testDeleteEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo[] body = response.getBody();
        assertNotNull(body);
        assertEquals(2, response.getBody().length);

        client.delete(createUri("/api/shows/1/seasons/1/episodes/1"));


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
    }

    @Test
    @Order(11)
    void testDeleteEpisodeDoesNotExist() throws URISyntaxException, JsonProcessingException {

        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE,
                createUri("/api/shows/1/seasons/1/episodes/1"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode not found",
                json.path("message").asText());
    }

    @Test
    @Order(12)
    void testDeleteAllEpisodes() throws URISyntaxException {
        ResponseEntity<EpisodeInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeInfo[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, response.getBody().length);

        client.delete(createUri("/api/shows/1/seasons/1/episodes"));


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeInfo[].class);

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
