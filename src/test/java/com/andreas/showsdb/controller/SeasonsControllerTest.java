package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Season;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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
public class SeasonsControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(9)
    void testAddSeasonToShow() throws URISyntaxException {
        // Set-up necessary for when all tests are run together. Can't use @BeforeAll because client is not static
        client.delete(createUri("/api/shows/1/seasons"));

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

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
