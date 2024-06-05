package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.MainCastDto;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class MainCastControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() throws URISyntaxException {
        client.delete(createUri("/api/main-cast/cache"));
    }

    @Test
    @Order(1)
    void testGetMainCasts() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/main-cast"), String.class);
        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        assertTrue(tree.size() >= 2);
        JsonNode mainCast = tree.get(0).get("content");
        assertEquals(1, mainCast.get("showId").asInt());
        assertEquals(1, mainCast.get("actorId").asInt());
        assertEquals("Nandor The Relentless", mainCast.get("character").asText());
        JsonNode links = tree.get(0).get("links");
        assertEquals(2, links.size());
        assertEquals("show", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get(0).get("href").asText());
        assertEquals("actor", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get(1).get("href").asText());

        mainCast = tree.get(1).get("content");
        assertEquals(2, mainCast.get("showId").asInt());
        assertEquals(2, mainCast.get("actorId").asInt());
        assertEquals("Eleanor Shellstrop", mainCast.get("character").asText());
        links = tree.get(1).get("links");
        assertEquals(2, links.size());
        assertEquals("show", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get(0).get("href").asText());
        assertEquals("actor", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/2".formatted(port), links.get(1).get("href").asText());
    }

    @Test
    @Order(2)
    void testAddShowToActor() throws URISyntaxException, JsonProcessingException {

        MainCastDto mainCastInput = MainCastDto.builder()
                .actorId(3L)
                .showId(1L)
                .character("The Guide")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInput, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode mainCast = tree.get("content");
        JsonNode links = tree.get("_links");

        assertEquals(mainCastInput.getActorId(), mainCast.get("actorId").asLong());
        assertEquals(mainCastInput.getShowId(), mainCast.get("showId").asLong());
        assertEquals("The Guide", mainCast.get("character").asText());
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
        assertEquals("http://localhost:%d/api/actors/3".formatted(port), links.get("actor").get("href").asText());
    }

    @Test
    @Order(3)
    void testAddRepeatedShowToActor() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(1L)
                .showId(1L)
                .character("Nandor The Relentless")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("That actor is already in that show",
                json.path("message").asText());
    }

    @Test
    @Order(4)
    void testGetActorShowsAsMainCast() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors/1/shows"),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode mainCast = tree.get(0).get("content");
        assertEquals(1, mainCast.get("showId").asInt());
        assertEquals(1, mainCast.get("actorId").asInt());
        assertEquals("Nandor The Relentless", mainCast.get("character").asText());
        JsonNode links = tree.get(0).get("links");
        assertEquals(2, links.size());
        assertEquals("show", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get(0).get("href").asText());
        assertEquals("actor", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get(1).get("href").asText());
    }

    @Test
    @Order(5)
    void testGetShowMainCast() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/main-cast"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode mainCast = tree.get(0).get("content");
        assertEquals(1, mainCast.get("showId").asInt());
        assertEquals(1, mainCast.get("actorId").asInt());
        assertEquals("Nandor The Relentless", mainCast.get("character").asText());
        JsonNode links = tree.get(0).get("links");
        assertEquals(2, links.size());
        assertEquals("show", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get(0).get("href").asText());
        assertEquals("actor", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get(1).get("href").asText());
    }

    @Test
    @Order(6)
    void testCreateMainCastShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(1L)
                .showId(99L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        JsonNode json = mapper.readTree(response.getBody());
        assertEquals("Show not found", json.path("message").asText());
    }

    @Test
    @Order(7)
    void testCreateMainCastActorDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(99L)
                .showId(1L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        JsonNode json = mapper.readTree(response.getBody());
        assertEquals("Actor not found", json.path("message").asText());
    }

    @Test
    @Order(8)
    void testModifyMainCast() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .showId(1L)
                .actorId(1L)
                .character("Nandor the Relentless")
                .build();

        RequestEntity<MainCastDto> request =
                new RequestEntity<>(mainCastDto, HttpMethod.PUT, createUri("/api/main-cast"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode mainCast = tree.get("content");
        JsonNode links = tree.get("_links");

        assertEquals(1, mainCast.get("actorId").asLong());
        assertEquals(1, mainCast.get("showId").asLong());
        assertEquals("Nandor the Relentless", mainCast.get("character").asText());
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("show").get("href").asText());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get("actor").get("href").asText());
    }

    @Test
    @Order(9)
    void deleteMainCast() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/main-cast"), String.class);

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        int size = tree.size();

        long actorId = tree.get(0).get("content").get("actorId").asLong();
        long showId = tree.get(0).get("content").get("showId").asLong();

        client.delete(createUri("/api/main-cast?actor=" + actorId + "&show=" + showId));

        response = client.getForEntity(createUri("/api/main-cast"), String.class);

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);
        assertEquals(size - 1, tree.size());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
