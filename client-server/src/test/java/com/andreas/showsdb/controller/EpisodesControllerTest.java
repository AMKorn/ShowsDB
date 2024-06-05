package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.SeasonInputDto;
import com.andreas.showsdb.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class EpisodesControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() throws URISyntaxException {
        SeasonInputDto season = SeasonInputDto.builder()
                .seasonNumber(1)
                .build();
        System.out.println(Utils.validate(season));

        client.postForEntity(createUri("/api/shows/1/seasons"), season, Void.class);
    }

    @Test
    @Order(1)
    void testAddEpisode() throws URISyntaxException, JsonProcessingException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(3)
                .name("Werewolf Feud")
                .releaseDate(Utils.parseDate("11/04/2019"))
                .build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(1, episode.get("seasonNumber").asInt());
        assertEquals(3, episode.get("episodeNumber").asInt());
        assertEquals("Werewolf Feud", episode.get("name").asText());
        assertEquals("2019-04-11", episode.get("releaseDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/1/episodes/3".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/1".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(2)
    void testAddEmptyEpisode() throws URISyntaxException, JsonProcessingException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder().build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(1, episode.get("seasonNumber").asInt());
        assertEquals(4, episode.get("episodeNumber").asInt());
        assertEquals("null", episode.get("name").asText());
        assertEquals("null", episode.get("releaseDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/1/episodes/4".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/1".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(3)
    void testAddEmptyEpisodeToEmptySeason() throws URISyntaxException, JsonProcessingException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder().build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/2/episodes"), episodeInputDto, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(2, episode.get("seasonNumber").asInt());
        assertEquals(1, episode.get("episodeNumber").asInt());
        assertEquals("null", episode.get("name").asText());
        assertEquals("null", episode.get("releaseDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/2/episodes/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/2".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(4)
    void testAddEpisodeAlreadyExists() throws URISyntaxException, JsonProcessingException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(1)
                .name("Another name")
                .releaseDate(new Date())
                .build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(1, episode.get("seasonNumber").asInt());
        assertEquals(1, episode.get("episodeNumber").asInt());
        assertNotEquals("Another name", episode.get("name").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/1/episodes/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/1".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(5)
    void testAddEpisodeShowOrSeasonDoesNotExist() throws URISyntaxException {
        EpisodeInputDto episode = EpisodeInputDto.builder()
                .episodeNumber(1)
                .name("Nonexistent episode")
                .build();

        ResponseEntity<Void> response =
                client.postForEntity(createUri("/api/shows/9/seasons/9/episodes"), episode, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
    }

    @Test
    @Order(6)
    void testGetAllSeasonEpisodes() throws URISyntaxException {
        ResponseEntity<EpisodeOutputDto[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<EpisodeOutputDto> episodes = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(4, episodes.size());
    }

    @Test
    @Order(7)
    void testGetEpisode() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/1"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(1, episode.get("seasonNumber").asInt());
        assertEquals(1, episode.get("episodeNumber").asInt());
        assertEquals("Pilot", episode.get("name").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/1/episodes/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/1".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(8)
    void testGetNonexistentEpisode() throws URISyntaxException {
        ResponseEntity<Void> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/99"),
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
    }

    @Test
    @Order(9)
    void testModifyEpisode() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/4"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode episode = tree.get("content");
        int episodeNumber = episode.get("episodeNumber").asInt();
        assertEquals(4, episodeNumber);

        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(episodeNumber)
                .name("Manhattan Night Club")
                .releaseDate(Utils.parseDate("18/04/2019"))
                .build();

        RequestEntity<EpisodeInputDto> request = new RequestEntity<>(episodeInputDto, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        response = client.exchange(request, String.class);

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);
        episode = tree.get("content");
        assertEquals(1, episode.get("showId").asInt());
        assertEquals(1, episode.get("seasonNumber").asInt());
        assertEquals(4, episode.get("episodeNumber").asInt());
        assertEquals("Manhattan Night Club", episode.get("name").asText());
        assertEquals("2019-04-18", episode.get("releaseDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals("http://localhost:%d/api/shows/1/seasons/1/episodes/4".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons/1".formatted(port), links.get("season").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
    }

    @Test
    @Order(10)
    void testModifyEpisodeDoesNotExist() throws URISyntaxException {
        EpisodeInputDto episode = EpisodeInputDto.builder()
                .episodeNumber(5)
                .name("Animal Control")
                .build();

        RequestEntity<EpisodeInputDto> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(11)
    void testDeleteEpisode() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        assertEquals(4, tree.size());

        RequestEntity<Void> requestEntity =
                new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/1/seasons/1/episodes/4"));
        ResponseEntity<String> response1 = client.exchange(requestEntity, String.class);
        System.err.println(response1);


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);
        assertEquals(3, tree.size());
    }

    @Test
    @Order(12)
    void testDeleteEpisodeDoesNotExist() throws URISyntaxException {

        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE,
                createUri("/api/shows/1/seasons/1/episodes/4"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
    }

    @Test
    @Order(13)
    void testDeleteAllEpisodes() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        assertEquals(3, tree.size());

        client.delete(createUri("/api/shows/1/seasons/1/episodes"));

        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);
        assertEquals(0, tree.size());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
