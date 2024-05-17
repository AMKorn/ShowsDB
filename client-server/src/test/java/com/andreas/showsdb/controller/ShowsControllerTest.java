package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
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
class ShowsControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() throws URISyntaxException {
        client.delete(createUri("/api/shows/cache"));
    }

    @Test
    @Order(1)
    void testSearchAll() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response =
                client.getForEntity(createUri("/api/shows"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        assertEquals(2, tree.size());
        JsonNode node = tree.get(0);
        JsonNode show = node.get("content");
        assertEquals(1, show.get("id").asLong());
        assertEquals("What We Do in the Shadows", show.get("name").asText());
        assertEquals("United States", show.get("country").asText());
        JsonNode links = node.get("links");
        assertNotNull(links);
        assertEquals(2, links.size());
        assertEquals("self", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get(0).get("href").asText());
        assertEquals("seasons", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons".formatted(port), links.get(1).get("href").asText());

        node = tree.get(1);
        show = node.get("content");
        assertEquals(2, show.get("id").asLong());
        assertEquals("The Good Place", show.get("name").asText());
        assertEquals("United States", show.get("country").asText());
        links = node.get("links");
        assertNotNull(links);
        assertEquals(2, links.size());
        assertEquals("self", links.get(0).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get(0).get("href").asText());
        assertEquals("seasons", links.get(1).get("rel").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons".formatted(port), links.get(1).get("href").asText());
    }

    @Test
    @Order(2)
    void testGetShowExists() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode show = tree.get("content");
        assertEquals(1, show.get("id").asLong());
        assertEquals("What We Do in the Shadows", show.get("name").asText());
        assertEquals("United States", show.get("country").asText());
        JsonNode links = tree.get("_links");
        assertNotNull(links);
        System.out.println(links);
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/shows/1".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/1/seasons".formatted(port), links.get("seasons").get("href").asText());
    }

    @Test
    @Order(3)
    void testGetShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/10"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(4)
    void testAddShow() throws URISyntaxException, JsonProcessingException {
        ShowInputDto showInputDto = ShowInputDto.builder()
                .name("Bojack Horseman")
                .country("United States")
                .build();

        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows"), showInputDto, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);

        JsonNode show = tree.get("content");
        assertEquals(3, show.get("id").asLong());
        assertEquals("Bojack Horseman", show.get("name").asText());
        assertEquals("United States", show.get("country").asText());
        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/shows/3".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/3/seasons".formatted(port), links.get("seasons").get("href").asText());
    }

    @Test
    @Order(5)
    void testModifyShow() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/2"), String.class);

        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        long id = tree.get("content").get("id").asLong();

        ShowOutputDto showOutputDto1 = ShowOutputDto.builder()
                .id(id)
                .name("The Good Place")
                .country("Canada")
                .build();


        client.put(createUri("/api/shows"), showOutputDto1);

        response = client.getForEntity(createUri("/api/shows/2"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);

        JsonNode show = tree.get("content");
        assertEquals(2, show.get("id").asLong());
        assertEquals("The Good Place", show.get("name").asText());
        assertEquals("Canada", show.get("country").asText());
        JsonNode links = tree.get("_links");
        assertNotNull(links);
        assertEquals(2, links.size());
        assertEquals("http://localhost:%d/api/shows/2".formatted(port), links.get("self").get("href").asText());
        assertEquals("http://localhost:%d/api/shows/2/seasons".formatted(port), links.get("seasons").get("href").asText());
    }

    @Test
    @Order(6)
    void testModifyShowDoesNotExist() throws URISyntaxException {
        Show show = Show.builder()
                .name("Nonexistent Show")
                .country("Nonexistent Country")
                .build();
        show.setId(99L);
        RequestEntity<Show> request = new RequestEntity<>(show, HttpMethod.PUT, createUri("/api/shows"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(7)
    void testDeleteShow() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows"), String.class);
        String body = response.getBody();
        assertNotNull(body);
        JsonNode tree = mapper.readTree(body);
        int numberOfShows = tree.size();

        client.delete(createUri("/api/shows/3"));

        response = client.getForEntity(createUri("/api/shows"), String.class);
        body = response.getBody();
        assertNotNull(body);
        tree = mapper.readTree(body);

        assertNotEquals(numberOfShows, tree.size());
    }

    @Test
    @Order(8)
    void testDeleteNonexistentShow() throws URISyntaxException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/99"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }

}
