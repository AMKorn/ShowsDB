package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.MainCastDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Disabled
class MainCastControllerTest {

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testGetMainCasts() throws URISyntaxException {
        ResponseEntity<MainCastDto[]> response = client.getForEntity(createUri("/api/main-cast"), MainCastDto[].class);
        MainCastDto[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(2, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor The Relentless", mainCasts[0].getCharacter());
        assertEquals(2, mainCasts[1].getShowId());
        assertEquals(2, mainCasts[1].getActorId());
        assertEquals("Eleanor Shellstrop", mainCasts[1].getCharacter());
    }

    @Test
    @Order(2)
    void testAddShowToActor() throws URISyntaxException {

        MainCastDto mainCastInput = MainCastDto.builder()
                .actorId(3L)
                .showId(1L)
                .character("The Guide")
                .build();

        ResponseEntity<MainCastDto> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInput, MainCastDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastDto mainCastResponse = response.getBody();
        assertNotNull(mainCastResponse);
        assertEquals(mainCastInput.getActorId(), mainCastResponse.getActorId());
        assertEquals(mainCastInput.getShowId(), mainCastResponse.getShowId());
        assertEquals("The Guide", mainCastResponse.getCharacter());
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
    void testGetActorShowsAsMainCast() throws URISyntaxException {
        ResponseEntity<MainCastDto[]> response = client.getForEntity(createUri("/api/actors/1/shows"),
                MainCastDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastDto[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor The Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(5)
    void testGetShowMainCast() throws URISyntaxException {
        ResponseEntity<MainCastDto[]> response = client.getForEntity(createUri("/api/shows/1/main-cast"), MainCastDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastDto[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(2, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor The Relentless", mainCasts[0].getCharacter());
        assertEquals(1, mainCasts[1].getShowId());
        assertEquals(3, mainCasts[1].getActorId());
        assertEquals("The Guide", mainCasts[1].getCharacter());
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

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
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

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor not found", json.path("message").asText());
    }

    @Test
    @Order(8)
    void testModifyMainCast() throws URISyntaxException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .showId(1L)
                .actorId(1L)
                .character("Nandor, the Relentless")
                .build();

        RequestEntity<MainCastDto> request =
                new RequestEntity<>(mainCastDto, HttpMethod.PUT, createUri("/api/main-cast"));
        ResponseEntity<MainCastDto> response = client.exchange(request, MainCastDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastDto mainCast = response.getBody();
        assertNotNull(mainCast);
        assertEquals(1, mainCast.getShowId());
        assertEquals(1, mainCast.getActorId());
        assertEquals("Nandor, the Relentless", mainCast.getCharacter());
    }

    @Test
    @Order(9)
    void deleteMainCast() throws URISyntaxException {
        ResponseEntity<MainCastDto[]> response1 = client.getForEntity(createUri("/api/main-cast"), MainCastDto[].class);
        MainCastDto[] mainCasts1 = response1.getBody();
        assertNotNull(mainCasts1);
        assertEquals(3, mainCasts1.length);

        long actorId = mainCasts1[1].getActorId();
        long showId = mainCasts1[1].getShowId();

        client.delete(createUri("/api/main-cast?actor=" + actorId + "&show=" + showId));

        ResponseEntity<MainCastDto[]> response2 = client.getForEntity(createUri("/api/main-cast"), MainCastDto[].class);
        MainCastDto[] mainCasts2 = response2.getBody();
        assertNotNull(mainCasts2);
        assertEquals(2, mainCasts2.length);
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
