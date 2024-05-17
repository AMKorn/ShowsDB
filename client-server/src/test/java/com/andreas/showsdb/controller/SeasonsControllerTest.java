package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.SeasonInputDto;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeasonsControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() throws URISyntaxException {
        client.delete(createUri("/api/seasons/cache"));
    }

    @Test
    @Order(1)
    void testAddSeasonToShow() throws URISyntaxException, JsonProcessingException {
        SeasonInputDto seasonInputDto = SeasonInputDto.builder()
                .seasonNumber(1)
                .build();

        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/2/seasons"),
                seasonInputDto, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode season = tree.get("content");

        assertNotNull(season);
        assertEquals(2, season.get("showId").asLong());
        assertEquals(1, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(2)
    void testAddSecondSeasonToShow() throws URISyntaxException, JsonProcessingException {
        SeasonInputDto seasonInputDto = SeasonInputDto.builder()
                .seasonNumber(2)
                .build();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/2/seasons"), seasonInputDto,
                String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode season = tree.get("content");

        assertNotNull(season);
        assertEquals(2, season.get("showId").asLong());
        assertEquals(2, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/2".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/2/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(3)
    void testAddUnnumberedSeasonToShow() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/2/seasons"),
                SeasonOutputDto.builder().build(), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode season = tree.get("content");

        assertNotNull(season);
        assertEquals(2, season.get("showId").asLong());
        assertEquals(3, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/3".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/3/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());

    }

    @Test
    @Order(4)
    void testAddSeasonToShowAlreadyExists() throws URISyntaxException, JsonProcessingException {
        SeasonInputDto seasonInputDto = SeasonInputDto.builder()
                .seasonNumber(1)
                .build();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/2/seasons"), seasonInputDto,
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode season = tree.get("content");

        assertNotNull(season);
        assertEquals(2, season.get("showId").asLong());
        assertEquals(1, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(5)
    void testAddSeasonToShowThatDoesNotExist() throws JsonProcessingException, URISyntaxException {
        SeasonOutputDto season = SeasonOutputDto.builder().build();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/99/seasons"), season, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        JsonNode json = mapper.readTree(response.getBody());
        assertEquals("Show not found",
                json.path("message").asText());
    }


    @Test
    @Order(6)
    void testGetShowSeasons() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2/seasons"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());


        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        assertEquals(3, tree.size());
        JsonNode season = tree.get(0).get("content");
        JsonNode links = tree.get(0).get("links");

        assertEquals(2, season.get("showId").asLong());
        assertEquals(1, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode link = links.get(0);
        assertEquals("self", link.get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1".formatted(port), link.get("href").asText());
        link = links.get(1);
        assertEquals("show", link.get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), link.get("href").asText());
        link = links.get(2);
        assertEquals("episodes", link.get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1/episodes".formatted(port), link.get("href").asText());
    }

    @Test
    @Order(7)
    void testGetShowSeasonByNumber() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2/seasons/1"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        System.out.println(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode season = tree.get("content");
        assertEquals(2, season.get("showId").asLong());
        assertEquals(1, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(8)
    void testGetNonexistentSeason() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2/seasons/15"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Season not found",
                json.path("message").asText());
    }

    @Test
    @Order(9)
    void testDeleteSeason() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2/seasons"), String.class);

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        int numberOfSeasons = tree.size();

        client.delete(createUri("/api/shows/2/seasons/2"));

        response = client.getForEntity(createUri("/api/shows/2/seasons"), String.class);

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);

        assertEquals(numberOfSeasons - 1, tree.size());
    }

    @Test
    @Order(10)
    void testDeleteNonexistentSeason() throws URISyntaxException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/2/seasons/99"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(11)
    void testDeleteAllSeasons() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2/seasons"), String.class);

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        assertNotEquals(0, tree.size());

        client.delete(createUri("/api/shows/2/seasons"));

        response = client.getForEntity(createUri("/api/shows/2/seasons"), String.class);

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);

        assertEquals(0, tree.size());
    }

    @Test
    @Order(12)
    void testAddEmptySeasonToEmptyShow() throws URISyntaxException, JsonProcessingException {
        SeasonInputDto seasonInputDto = SeasonInputDto.builder().build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/2/seasons"), seasonInputDto, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode season = tree.get("content");

        assertNotNull(season);
        assertEquals(2, season.get("showId").asLong());
        assertEquals(1, season.get("seasonNumber").asInt());
        assertEquals(0, season.get("numberOfEpisodes").asInt());

        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals("http://localhost:%d/api/shows/2/seasons/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons/1/episodes".formatted(port), links.get("episodes").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("show").get("href").asText());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
