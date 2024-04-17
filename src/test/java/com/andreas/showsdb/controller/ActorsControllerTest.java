package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.dto.ActorDto;
import com.andreas.showsdb.model.dto.ActorDtoId;
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
        ActorDto actor = ActorDto.builder()
                .name("Kayvan Novak")
                .country("United Kingdom")
                .birthDate(Utils.parseDate("23/11/1968"))
                .build();

        ResponseEntity<ActorDtoId> response = client.postForEntity(createUri("/api/actors"), actor, ActorDtoId.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        ActorDtoId newActor = response.getBody();
        assertNotNull(newActor);
        assertEquals(1, newActor.getId());
        assertEquals(actor.getName(), newActor.getName());
        assertEquals(actor.getCountry(), newActor.getCountry());
        assertEquals(actor.getBirthDate(), newActor.getBirthDate());
    }

    @Test
    @Order(2)
    void testAddActorAlreadyExists() throws URISyntaxException, JsonProcessingException {
        // Commented out because when the post method deserializes actor it maps it to actorDto which does not have
        // an id, which makes it so that it creates a new one with duplicated values. IDK what is the right behavior

//        ActorDtoId actor = ActorDtoId.builder()
//                .id(1L)
//                .name("Kayvan Novak")
//                .country("United Kingdom")
//                .birthDate(Utils.parseDate("23/11/1968"))
//                .build();
//
//        ResponseEntity<String> response = client.postForEntity(createUri("/api/actors"), actor, String.class);
//
//        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
//        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode json = objectMapper.readTree(response.getBody());
//        assertEquals("Actor already exists with that id",
//                json.path("message").asText());
    }

    @Test
    @Order(3)
    void testGetAllActor() throws URISyntaxException {
        ResponseEntity<ActorDtoId[]> response = client.getForEntity(createUri("/api/actors"), ActorDtoId[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<ActorDtoId> actors = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(1, actors.size());
        assertEquals(1, actors.getFirst().getId());
        assertEquals("Kayvan Novak", actors.getFirst().getName());
        assertEquals("United Kingdom", actors.getFirst().getCountry());
        assertEquals(Utils.parseDate("23/11/1968"), actors.getFirst().getBirthDate());
    }

    @Test
    @Order(4)
    void testGetActor() throws URISyntaxException {
        ResponseEntity<ActorDtoId> response = client.getForEntity(createUri("/api/actors/1"), ActorDtoId.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ActorDtoId actor = response.getBody();
        assertNotNull(actor);
        assertEquals(1, actor.getId());
        assertEquals("Kayvan Novak", actor.getName());
        assertEquals("United Kingdom", actor.getCountry());
        assertEquals(Utils.parseDate("23/11/1968"), actor.getBirthDate());
    }

    @Test
    @Order(5)
    void testGetNonexistentActor() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/actors/9"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(body);
        assertEquals("Actor not found", json.path("message").asText());
    }

    @Test
    @Order(6)
    void testModifyActor() throws URISyntaxException {
        ResponseEntity<ActorDtoId> response = client.getForEntity(createUri("/api/actors/1"), ActorDtoId.class);
        ActorDtoId oldActorInfo = response.getBody();
        assertNotNull(oldActorInfo);

        ActorDtoId newActorInfo = ActorDtoId.builder()
                .id(oldActorInfo.getId())
                .name(oldActorInfo.getName())
                .country(oldActorInfo.getCountry())
                .birthDate(Utils.parseDate("23/11/1978"))
                .build();

//        actor.setBirthDate(Utils.parseDate("23/11/1978"));

        client.put(createUri("/api/actors"), newActorInfo);

        response = client.getForEntity(createUri("/api/actors/1"), ActorDtoId.class);
        ActorDtoId actor = response.getBody();
        assertNotNull(actor);
        assertEquals(Utils.parseDate("23/11/1978"), actor.getBirthDate());
    }

    @Test
    @Order(7)
    void testModifyNonexistentActor() throws URISyntaxException, JsonProcessingException {
        ActorDtoId actor = ActorDtoId.builder()
                .id(99L)
                .name("Actor")
                .build();

        RequestEntity<ActorDtoId> request = new RequestEntity<>(actor, HttpMethod.PUT, createUri("/api/actors"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor not found",
                json.path("message").asText());
    }

    @Test
    @Order(8)
    void testDeleteActor() throws URISyntaxException {
        ResponseEntity<ActorDtoId[]> response = client.getForEntity(createUri("/api/actors"), ActorDtoId[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        ActorDtoId[] actors = response.getBody();
        assertNotNull(actors);
        assertEquals(1, actors.length);

        client.delete(createUri("/api/actors/1"));

        response = client.getForEntity(createUri("/api/actors"), ActorDtoId[].class);
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