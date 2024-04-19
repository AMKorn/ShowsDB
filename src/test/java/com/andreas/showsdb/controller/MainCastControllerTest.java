package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.dto.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
//@Disabled
public class MainCastControllerTest {

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddShowToActor() throws URISyntaxException {
        ResponseEntity<ShowInfo> showResponse = client.getForEntity(createUri("/api/shows/1"), ShowInfo.class);
        ShowInfo show = showResponse.getBody();
        assertNotNull(show);
        ActorInput actorInput = ActorInput.builder()
                .name("Kayvan Novak")
                .country("United Kingdom")
                .birthDate(Utils.parseDate("23/11/1978"))
                .build();

        ResponseEntity<ActorInfo> actorResponse = client.postForEntity(createUri("/api/actors"), actorInput, ActorInfo.class);
        ActorInfo actorInfo = actorResponse.getBody();

        assertNotNull(actorInfo);

        MainCastInfo mainCastInput = MainCastInfo.builder()
                .actorId(actorInfo.getId())
                .showId(show.getId())
                .character("Nandor the Relentless")
                .build();

        ResponseEntity<MainCastInfo> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInput, MainCastInfo.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastInfo mainCastResponse = response.getBody();
        assertNotNull(mainCastResponse);
        assertEquals(actorInfo.getId(), mainCastResponse.getActorId());
        assertEquals(show.getId(), mainCastResponse.getShowId());
        assertEquals("Nandor the Relentless", mainCastResponse.getCharacter());
    }

    @Test
    @Order(2)
    void testAddRepeatedShowToActor() throws URISyntaxException, JsonProcessingException {
        MainCastInfo mainCastInfo = MainCastInfo.builder()
                .actorId(1L)
                .showId(1L)
                .character("Nandor the Relentless")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInfo, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("That actor is already in that show",
                json.path("message").asText());
    }

    @Test
    @Order(3)
    void testGetMainCasts() throws URISyntaxException {
        ResponseEntity<MainCastInfo[]> response = client.getForEntity(createUri("/api/main-cast"), MainCastInfo[].class);
        MainCastInfo[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(4)
    void testGetActorShowsAsMainCast() throws URISyntaxException {
        ResponseEntity<MainCastInfo[]> response = client.getForEntity(createUri("/api/actors/1/shows"),
                MainCastInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastInfo[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(5)
    void testGetShowMainCast() throws URISyntaxException {
        ResponseEntity<MainCastInfo[]> response = client.getForEntity(createUri("/api/shows/1/main-cast"), MainCastInfo[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastInfo[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(6)
    void testCreateMainCastShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastInfo mainCastInfo = MainCastInfo.builder()
                .actorId(1L)
                .showId(99L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInfo, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show not found", json.path("message").asText());
    }

    @Test
    @Order(7)
    void testCreateMainCastActorDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastInfo mainCastInfo = MainCastInfo.builder()
                .actorId(99L)
                .showId(1L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastInfo, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor not found", json.path("message").asText());
    }

    @Test
    @Order(8)
    void testModifyMainCast() throws URISyntaxException {
        MainCastInfo mainCastInfo = MainCastInfo.builder()
                .showId(1L)
                .actorId(1L)
                .character("Nandor, the Relentless")
                .build();

        RequestEntity<MainCastInfo> request =
                new RequestEntity<>(mainCastInfo, HttpMethod.PUT, createUri("/api/main-cast"));
        ResponseEntity<MainCastInfo> response = client.exchange(request, MainCastInfo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCastInfo mainCast = response.getBody();
        assertNotNull(mainCast);
        assertEquals(1, mainCast.getShowId());
        assertEquals(1, mainCast.getActorId());
        assertEquals("Nandor, the Relentless", mainCast.getCharacter());
    }

    @Test
    @Order(9)
    void testAddSecondMainCast() throws URISyntaxException {
        ActorInput actorInput = ActorInput.builder()
                .name("Kristen Bell")
                .country("United States")
                .build();
        ResponseEntity<ActorInfo> actorResponseEntity = client.postForEntity(createUri("/api/actors"),
                actorInput, ActorInfo.class);
        ActorInfo actor = actorResponseEntity.getBody();
        assertNotNull(actor);
        ShowInput showInput = ShowInput.builder()
                .name("The Good Place")
                .build();
        ResponseEntity<ShowInfo> showResponseEntity = client.postForEntity(createUri("/api/shows"), showInput, ShowInfo.class);
        ShowInfo show = showResponseEntity.getBody();
        assertNotNull(show);
        MainCastInfo mainCastInfo = MainCastInfo.builder()
                .actorId(actor.getId())
                .showId(show.getId())
                .character("Eleanor Shellstrop")
                .build();
        client.postForEntity(createUri("/api/main-cast"), mainCastInfo, Void.class);


        ResponseEntity<MainCastInfo[]> response = client.getForEntity(createUri("/api/main-cast"), MainCastInfo[].class);

        MainCastInfo[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(2, mainCasts.length);
        assertEquals(1, mainCasts[0].getShowId());
        assertEquals(1, mainCasts[0].getActorId());
        assertEquals("Nandor, the Relentless", mainCasts[0].getCharacter());
        assertEquals(show.getId(), mainCasts[1].getShowId());
        assertEquals(actor.getId(), mainCasts[1].getActorId());
        assertEquals("Eleanor Shellstrop", mainCasts[1].getCharacter());
    }

    @Test
    @Order(10)
    void deleteMainCast() throws URISyntaxException {
        ResponseEntity<MainCastInfo[]> response1 = client.getForEntity(createUri("/api/main-cast"), MainCastInfo[].class);
        MainCastInfo[] mainCasts1 = response1.getBody();
        assertNotNull(mainCasts1);
        assertEquals(2, mainCasts1.length);

        long actorId = mainCasts1[1].getActorId();
        long showId = mainCasts1[1].getShowId();

        client.delete(createUri("/api/main-cast?actor=" + actorId + "&show=" + showId));

        ResponseEntity<MainCastInfo[]> response2 = client.getForEntity(createUri("/api/main-cast"), MainCastInfo[].class);
        MainCastInfo[] mainCasts2 = response2.getBody();
        assertNotNull(mainCasts2);
        assertEquals(1, mainCasts2.length);
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
