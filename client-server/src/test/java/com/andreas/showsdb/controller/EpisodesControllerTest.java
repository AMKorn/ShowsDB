package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.SeasonInputDto;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
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
class EpisodesControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddEpisode() throws URISyntaxException {
        // Setup. Can't use @BeforeAll because client is not static
        SeasonInputDto season = SeasonInputDto.builder()
                .seasonNumber(1)
                .build();
        System.out.println(Utils.validate(season));

        ResponseEntity<String> response1 = client.postForEntity(createUri("/api/shows/1/seasons"), season, String.class);
        System.err.println(response1);

        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(1)
                .name("Pilot")
                .releaseDate(Utils.parseDate("28/03/2019"))
                .build();
        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, String.class);
        System.out.println(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    @Order(2)
    void testAddEmptyEpisode() throws URISyntaxException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder().build();
        ResponseEntity<EpisodeOutputDto> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, EpisodeOutputDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertNull(episode.getName());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(3)
    void testAddEmptyEpisodeToEmptySeason() throws URISyntaxException {
        SeasonInputDto seasonInputDto = SeasonInputDto.builder().build();
        ResponseEntity<SeasonOutputDto> seasonResponse =
                client.postForEntity(createUri("/api/shows/1/seasons"), seasonInputDto, SeasonOutputDto.class);
        SeasonOutputDto seasonOutputDto = seasonResponse.getBody();
        assertNotNull(seasonOutputDto);
        assertEquals(2, seasonOutputDto.getSeasonNumber());

        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder().build();
        ResponseEntity<EpisodeOutputDto> response =
                client.postForEntity(createUri("/api/shows/1/seasons/2/episodes"), episodeInputDto, EpisodeOutputDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto episode = response.getBody();
        assertNotNull(episode);
        assertEquals(1, episode.getEpisodeNumber());
        assertNull(episode.getName());
        assertEquals(2, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(4)
    void testAddEpisodeAlreadyExists() throws URISyntaxException {
        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(1)
                .name("Another name")
                .releaseDate(new Date())
                .build();
        ResponseEntity<EpisodeOutputDto> response =
                client.postForEntity(createUri("/api/shows/1/seasons/1/episodes"), episodeInputDto, EpisodeOutputDto.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto episodeOutputDto = response.getBody();
        assertNotNull(episodeOutputDto);
        assertEquals(1, episodeOutputDto.getEpisodeNumber());
        assertEquals("Pilot", episodeOutputDto.getName());
        assertEquals(1, episodeOutputDto.getSeasonNumber());
        assertEquals(1L, episodeOutputDto.getShowId());
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

        assertEquals(2, episodes.size());
    }

    @Test
    @Order(7)
    void testGetEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeOutputDto> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/1"),
                EpisodeOutputDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto episode = response.getBody();

        assertNotNull(episode);
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals("Pilot", episode.getName());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
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
    void testModifyEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeOutputDto> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes/2"),
                EpisodeOutputDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto episode = response.getBody();
        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());

        EpisodeInputDto episodeInputDto = EpisodeInputDto.builder()
                .episodeNumber(episode.getEpisodeNumber())
                .name("City Council")
                .releaseDate(Utils.parseDate("04/04/2019"))
                .build();

        RequestEntity<EpisodeInputDto> request = new RequestEntity<>(episodeInputDto, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        response = client.exchange(request, EpisodeOutputDto.class);

        episode = response.getBody();

        assertNotNull(episode);
        assertEquals(2, episode.getEpisodeNumber());
        assertEquals("City Council", episode.getName());
        assertEquals(Utils.parseDate("04/04/2019"), episode.getReleaseDate());
        assertEquals(1, episode.getSeasonNumber());
        assertEquals(1L, episode.getShowId());
    }

    @Test
    @Order(10)
    void testModifyEpisodeDoesNotExist() throws URISyntaxException, JsonProcessingException {
        EpisodeInputDto episode = EpisodeInputDto.builder()
                .episodeNumber(3)
                .name("Werewolf Feud")
                .build();

        RequestEntity<EpisodeInputDto> request = new RequestEntity<>(episode, HttpMethod.PUT,
                createUri("/api/shows/1/seasons/1/episodes"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Episode not found or trying to modify episode number.",
                json.path("message").asText());
    }

    @Test
    @Order(11)
    void testDeleteEpisode() throws URISyntaxException {
        ResponseEntity<EpisodeOutputDto[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto[] body = response.getBody();
        assertNotNull(body);
        assertEquals(2, response.getBody().length);

        RequestEntity<Void> requestEntity =
                new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/1/seasons/1/episodes/1"));
        ResponseEntity<String> response1 = client.exchange(requestEntity, String.class);
        System.err.println(response1);


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
    }

    @Test
    @Order(12)
    void testDeleteEpisodeDoesNotExist() throws URISyntaxException {

        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE,
                createUri("/api/shows/1/seasons/1/episodes/1"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
    }

    @Test
    @Order(13)
    void testDeleteAllEpisodes() throws URISyntaxException {
        ResponseEntity<EpisodeOutputDto[]> response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EpisodeOutputDto[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, response.getBody().length);

        client.delete(createUri("/api/shows/1/seasons/1/episodes"));


        response = client.getForEntity(createUri("/api/shows/1/seasons/1/episodes"),
                EpisodeOutputDto[].class);

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
