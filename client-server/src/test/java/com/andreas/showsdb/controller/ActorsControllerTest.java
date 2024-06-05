package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.util.Utils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActorsControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    void beforeAll() throws URISyntaxException {
        client.delete(createUri("/api/actors/cache"));
    }

    @Test
    @Order(1)
    void testGetAllActors() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        assertTrue(tree.size() >= 2);
        JsonNode actor = tree.get(0).get("content");
        assertEquals(1, actor.get("id").asLong());
        assertEquals("Kayvan Novak", actor.get("name").asText());
        assertEquals("United Kingdom", actor.get("country").asText());
        assertEquals("1978-11-23", actor.get("birthDate").asText());
        JsonNode links = tree.get(0).get("links");
        assertEquals(2, links.size());
        assertEquals("self", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get(0).get("href").asText());
        assertEquals("shows", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/1/shows".formatted(port), links.get(1).get("href").asText());

        actor = tree.get(1).get("content");
        assertEquals(2, actor.get("id").asLong());
        assertEquals("Kristen Bell", actor.get("name").asText());
        assertEquals("United States", actor.get("country").asText());
        assertEquals("1980-07-18", actor.get("birthDate").asText());
        links = tree.get(1).get("links");
        assertEquals(2, links.size());
        assertEquals("self", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/2".formatted(port), links.get(0).get("href").asText());
        assertEquals("shows", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/actors/2/shows".formatted(port), links.get(1).get("href").asText());
    }

    @Test
    @Order(2)
    void testGetActor() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors/1"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode actor = tree.get("content");
        assertEquals(1, actor.get("id").asLong());
        assertEquals("Kayvan Novak", actor.get("name").asText());
        assertEquals("United Kingdom", actor.get("country").asText());
        assertEquals("1978-11-23", actor.get("birthDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/actors/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/actors/1/shows".formatted(port), links.get("shows").get("href").asText());
    }

    @Test
    @Order(3)
    void testAddActor() throws URISyntaxException, JsonProcessingException {
        ActorInputDto actorIn = ActorInputDto.builder()
                .name("Timothée Chalamet")
                .country("United States")
                .birthDate(Utils.parseDate("27/12/1995"))
                .build();

        ResponseEntity<String> response = client.postForEntity(createUri("/api/actors"), actorIn, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode actorOut = tree.get("content");
        assertTrue(actorOut.get("id").isNumber());
        long id = actorOut.get("id").asLong();
        assertNotEquals(0, id);
        assertEquals("Timothée Chalamet", actorOut.get("name").asText());
        assertEquals("United States", actorOut.get("country").asText());
        assertEquals("1995-12-27", actorOut.get("birthDate").asText());
        JsonNode links = tree.get("_links");
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/actors/%d".formatted(port, id), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/actors/%d/shows".formatted(port, id), links.get("shows").get("href").asText());
    }

    @Test
    @Order(5)
    void testGetNonexistentActor() throws URISyntaxException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors/9"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(6)
    void testModifyActor() throws URISyntaxException, JsonProcessingException, ParseException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors/4"), String.class);

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        JsonNode actorOut = tree.get("content");

        Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(actorOut.get("birthDate").asText());
        LocalDate localDate = birthDate != null ?
                birthDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                : null;
        ActorOutputDto newActorOutputDto = ActorOutputDto.builder()
                .id(actorOut.get("id").asLong())
                .name(actorOut.get("name").asText())
                .country("United States/France")
                .birthDate(localDate)
                .build();

        client.put(createUri("/api/actors"), newActorOutputDto);

        response = client.getForEntity(createUri("/api/actors/4"), String.class);

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);
        JsonNode newActorOut = tree.get("content");

        assertEquals("United States/France", newActorOut.get("country").asText());
    }

    @Test
    @Order(7)
    void testModifyNonexistentActor() throws URISyntaxException {
        ActorOutputDto actor = ActorOutputDto.builder()
                .id(99L)
                .name("Actor")
                .build();

        RequestEntity<ActorOutputDto> request = new RequestEntity<>(actor, HttpMethod.PUT, createUri("/api/actors"));
        ResponseEntity<ActorOutputDto> response = client.exchange(request, ActorOutputDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(8)
    void testDeleteActor() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        String body = response.getBody();
        assertNotNull(body);
        JsonNode actors = mapper.readTree(body);

        assertEquals(4, actors.size());

        client.delete(createUri("/api/actors/4"));

        response = client.getForEntity(createUri("/api/actors"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertNotNull(body);
        actors = mapper.readTree(body);

        assertEquals(3, actors.size());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}