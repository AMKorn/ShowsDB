package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
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
class ActorsControllerTest {

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddActor() throws URISyntaxException {
        Actor actor = Actor.builder()
                .name("Kayvan Novak")
                .country("United Kingdom")
                .birthDate(Utils.parseDate("23/11/1968"))
                .build();

        ResponseEntity<Actor> response = client.postForEntity(createUri("/api/actors"), actor, Actor.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        Actor newActor = response.getBody();
        assertNotNull(newActor);
        assertEquals(1, newActor.getId());
        assertEquals(actor.getName(), newActor.getName());
        assertEquals(actor.getCountry(), newActor.getCountry());
        assertEquals(actor.getBirthDate(), newActor.getBirthDate());
    }

    @Test
    @Order(2)
    void testAddActorAlreadyExists() throws URISyntaxException, JsonProcessingException {
        Actor actor = Actor.builder()
                .name("Kayvan Novak")
                .country("United Kingdom")
                .birthDate(Utils.parseDate("23/11/1968"))
                .id(1L)
                .build();

        ResponseEntity<String> response = client.postForEntity(createUri("/api/actors"), actor, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor already exists with that id",
                json.path("message").asText());
    }

    @Test
    @Order(3)
    void testGetAllActor() throws URISyntaxException {
        ResponseEntity<Actor[]> response = client.getForEntity(createUri("/api/actors"), Actor[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Actor> actors = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(1, actors.size());
        assertEquals(1, actors.getFirst().getId());
        assertEquals("Kayvan Novak", actors.getFirst().getName());
        assertEquals("United Kingdom", actors.getFirst().getCountry());
        assertEquals(Utils.parseDate("23/11/1968"), actors.getFirst().getBirthDate());
    }

    @Test
    @Order(4)
    void testGetActor() throws URISyntaxException {
        ResponseEntity<Actor> response = client.getForEntity(createUri("/api/actors/1"), Actor.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Actor actor = response.getBody();
        assertNotNull(actor);
        assertEquals(1, actor.getId());
        assertEquals("Kayvan Novak", actor.getName());
        assertEquals("United Kingdom", actor.getCountry());
        assertEquals(Utils.parseDate("23/11/1968"), actor.getBirthDate());
    }

    @Test
    @Order(5)
    void testGetNonexistentActor() throws URISyntaxException {
        ResponseEntity<Void> response = client.getForEntity(createUri("/api/actors/9"), Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(6)
    void testModifyActor() throws URISyntaxException {
        ResponseEntity<Actor> response = client.getForEntity(createUri("/api/actors/1"), Actor.class);
        Actor actor = response.getBody();
        assertNotNull(actor);
        actor.setBirthDate(Utils.parseDate("23/11/1978"));

        client.put(createUri("/api/actors"), actor);

        response = client.getForEntity(createUri("/api/actors/1"), Actor.class);
        actor = response.getBody();
        assertNotNull(actor);
        assertEquals(Utils.parseDate("23/11/1978"), actor.getBirthDate());
    }

    @Test
    @Order(7)
    void testModifyNonexistentActor() throws URISyntaxException, JsonProcessingException {
        Actor actor = new Actor();

        RequestEntity<Actor> request = new RequestEntity<>(actor, HttpMethod.PUT, createUri("/api/actors"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor does not exist",
                json.path("message").asText());
    }

    @Test
    @Order(8)
    void testDeleteActor() throws URISyntaxException {
        ResponseEntity<Actor[]> response = client.getForEntity(createUri("/api/actors"), Actor[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        Actor[] actors = response.getBody();
        assertNotNull(actors);
        assertEquals(1, actors.length);

        client.delete(createUri("/api/actors/1"));

        response = client.getForEntity(createUri("/api/actors"), Actor[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        actors = response.getBody();
        assertNotNull(actors);
        assertEquals(0, actors.length);
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}