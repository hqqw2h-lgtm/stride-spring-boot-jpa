package io.github.lgtm.springframework.jpa.querydsl.web;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestEntityControllerRegistryTest {
  @LocalServerPort private int port;
  @Autowired private RestEntityControllerRegistry restEntityControllerRegistry;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void testGetController() {
    String url = "http://127.0.0.1:" + port + "/UserEntity/1";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testCreateController() throws JsonProcessingException {
    String url = "http://127.0.0.1:" + port + "/UserEntity";


    UserEntity user = new UserEntity();
    user.setName("test");


    ResponseEntity<UserEntity> response =
        restTemplate.postForEntity(url, user, UserEntity.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(user.getName(), response.getBody().getName());
  }

  @Test
  void testDeleteController() {
    String url = "http://127.0.0.1:" + port + "/UserEntity/1";
    restTemplate.delete(url);
  }
}
