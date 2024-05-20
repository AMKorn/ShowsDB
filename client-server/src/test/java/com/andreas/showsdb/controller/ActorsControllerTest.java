package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.util.Utils;
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
    void testGetAllActors() throws URISyntaxException {
        ResponseEntity<ActorOutputDto[]> response = client.getForEntity(createUri("/api/actors"), ActorOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<ActorOutputDto> actors = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(3, actors.size());
        assertEquals(1, actors.getFirst().getId());
        assertEquals("Kayvan Novak", actors.getFirst().getName());
        assertEquals("United Kingdom", actors.getFirst().getCountry());
        assertEquals(Utils.parseDate("23/11/1978"), actors.getFirst().getBirthDate());
        assertEquals(2, actors.get(1).getId());
        assertEquals("Kristen Bell", actors.get(1).getName());
        assertEquals("United States", actors.get(1).getCountry());
        assertEquals(Utils.parseDate("18/07/1980"), actors.get(1).getBirthDate());
    }

    @Test
    @Order(2)
    void testGetActor() throws URISyntaxException {
        ResponseEntity<ActorOutputDto> response = client.getForEntity(createUri("/api/actors/1"), ActorOutputDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ActorOutputDto actor = response.getBody();
        assertNotNull(actor);
        assertEquals(1, actor.getId());
        assertEquals("Kayvan Novak", actor.getName());
        assertEquals("United Kingdom", actor.getCountry());
        assertEquals(Utils.parseDate("23/11/1978"), actor.getBirthDate());
    }

    @Test
    @Order(3)
    void testAddActor() throws URISyntaxException {
        ActorInputDto actor = ActorInputDto.builder()
                .name("Timothée Chalamet")
                .country("United States")
                .birthDate(Utils.parseDate("27/12/1995"))
                .build();

        ResponseEntity<ActorOutputDto> response = client.postForEntity(createUri("/api/actors"), actor, ActorOutputDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        ActorOutputDto newActor = response.getBody();
        assertNotNull(newActor);
        assertEquals(4, newActor.getId());
        assertEquals(actor.getName(), newActor.getName());
        assertEquals(actor.getCountry(), newActor.getCountry());
        assertEquals(actor.getBirthDate(), newActor.getBirthDate());
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
    void testModifyActor() throws URISyntaxException {
        ResponseEntity<ActorOutputDto> response = client.getForEntity(createUri("/api/actors/4"), ActorOutputDto.class);
        ActorOutputDto oldActorOutputDto = response.getBody();
        assertNotNull(oldActorOutputDto);

        ActorOutputDto newActorOutputDto = ActorOutputDto.builder()
                .id(oldActorOutputDto.getId())
                .name(oldActorOutputDto.getName())
                .country("United States/France")
                .birthDate(oldActorOutputDto.getBirthDate())
                .build();

        client.put(createUri("/api/actors"), newActorOutputDto);

        response = client.getForEntity(createUri("/api/actors/4"), ActorOutputDto.class);
        ActorOutputDto actor = response.getBody();
        assertNotNull(actor);
        assertEquals("United States/France", actor.getCountry());
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
    void testDeleteActor() throws URISyntaxException {
        ResponseEntity<ActorOutputDto[]> response = client.getForEntity(createUri("/api/actors"), ActorOutputDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        ActorOutputDto[] actors = response.getBody();
        assertNotNull(actors);
        assertEquals(4, actors.length);

        client.delete(createUri("/api/actors/4"));

        response = client.getForEntity(createUri("/api/actors"), ActorOutputDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        actors = response.getBody();
        assertNotNull(actors);
        assertEquals(3, actors.length);
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}