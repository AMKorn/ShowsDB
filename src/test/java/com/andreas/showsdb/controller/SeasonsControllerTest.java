package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.SeasonInfo;
import com.andreas.showsdb.model.dto.SeasonInput;
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
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SeasonsControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddSeasonToShow() throws URISyntaxException {
        // Set-up necessary for when all tests are run together. Can't use @BeforeAll because client is not static
//        client.delete(createUri("/api/shows/1/seasons"));

        SeasonInput seasonInput = SeasonInput.builder()
                .seasonNumber(1)
                .build();

        ResponseEntity<SeasonInfo> response = client.postForEntity(createUri("/api/shows/1/seasons"), seasonInput, SeasonInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        SeasonInfo seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertEquals(1, seasonResponse.getShowId());
        assertEquals(1, seasonResponse.getSeasonNumber());
        assertEquals(0, seasonResponse.getNumberOfEpisodes());
    }

    @Test
    @Order(2)
    void testAddSecondSeasonToShow() throws URISyntaxException {
        SeasonInput seasonInput = SeasonInput.builder()
                .seasonNumber(2)
                .build();
        ResponseEntity<SeasonInfo> response = client.postForEntity(createUri("/api/shows/1/seasons"), seasonInput,
                SeasonInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        SeasonInfo seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertEquals(1, seasonResponse.getShowId());
        assertEquals(2, seasonResponse.getSeasonNumber());
    }

    @Test
    @Order(3)
    void testAddUnnumberedSeasonToShow() throws URISyntaxException {
        ResponseEntity<SeasonInfo> response = client.postForEntity(createUri("/api/shows/1/seasons"),
                SeasonInfo.builder().build(), SeasonInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        SeasonInfo seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertEquals(1, seasonResponse.getShowId());
        assertEquals(3, seasonResponse.getSeasonNumber());

    }

    @Test
    @Order(4)
    void testAddSeasonToShowAlreadyExists() throws JsonProcessingException, URISyntaxException {
        SeasonInput seasonInput = SeasonInput.builder()
                .seasonNumber(1)
                .build();
        ResponseEntity<SeasonInfo> response = client.postForEntity(createUri("/api/shows/1/seasons"), seasonInput,
                SeasonInfo.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        SeasonInfo seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertEquals(1, seasonResponse.getShowId());
        assertEquals(1, seasonResponse.getSeasonNumber());
        assertEquals(0, seasonResponse.getNumberOfEpisodes());

//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode json = objectMapper.readTree(response.getBody());
//        assertEquals("Show already has a Season 1",
//                json.path("message").asText());
//        assertEquals(1, json.path("season").path("showId").asInt());
//        assertEquals(1, json.path("season").path("seasonNumber").asInt());
//        assertEquals(0, json.path("season").path("numberOfEpisodes").asInt());
    }

    @Test
    @Order(5)
    void testAddSeasonToShowThatDoesNotExist() throws JsonProcessingException, URISyntaxException {
        SeasonInfo season = SeasonInfo.builder().build();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/99/seasons"), season, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show not found",
                json.path("message").asText());
    }


    @Test
    @Order(6)
    void testGetShowSeasons() throws URISyntaxException {
        ResponseEntity<SeasonInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), SeasonInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<SeasonInfo> seasons = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(3, seasons.size());
    }

    @Test
    @Order(7)
    void testGetShowSeasonByNumber() throws URISyntaxException {
        ResponseEntity<SeasonInfo> response = client.getForEntity(createUri("/api/shows/1/seasons/1"), SeasonInfo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        SeasonInfo season = response.getBody();
        assertNotNull(season);
        assertEquals(1, season.getShowId());
        assertEquals(1, season.getSeasonNumber());
        assertEquals(0, season.getNumberOfEpisodes());
    }

    @Test
    @Order(8)
    void testGetNonexistentSeason() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/15"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Season not found",
                json.path("message").asText());
    }

    @Test
    @Order(9)
    void testDeleteSeason() throws URISyntaxException {
        ResponseEntity<SeasonInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), SeasonInfo[].class);

        assertNotNull(response.getBody());
        int numberOfSeasons = response.getBody().length;

        client.delete(createUri("/api/shows/1/seasons/2"));

        response = client.getForEntity(createUri("/api/shows/1/seasons"), SeasonInfo[].class);

        assertNotNull(response.getBody());
        assertEquals(numberOfSeasons - 1, response.getBody().length);
    }

    @Test
    @Order(10)
    void testDeleteNonexistentSeason() throws URISyntaxException, JsonProcessingException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/1/seasons/99"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Season not found",
                json.path("message").asText());
    }

    @Test
    @Order(11)
    void testDeleteAllSeasons() throws URISyntaxException {
        ResponseEntity<SeasonInfo[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), SeasonInfo[].class);

        assertNotNull(response.getBody());
        int numberOfSeasons = response.getBody().length;
        assertNotEquals(0, numberOfSeasons);

        client.delete(createUri("/api/shows/1/seasons"));

        response = client.getForEntity(createUri("/api/shows/1/seasons"), SeasonInfo[].class);

        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);

    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
