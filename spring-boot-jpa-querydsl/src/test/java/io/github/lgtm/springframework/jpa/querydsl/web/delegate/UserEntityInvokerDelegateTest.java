package io.github.lgtm.springframework.jpa.querydsl.web.delegate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserEntityInvokerDelegateTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserEntityRepository userEntityRepository;

  @BeforeEach
  void before() {

    userEntityRepository.deleteAll();
    String url = "http://127.0.0.1:" + port + "/UserEntity";

    UserEntity user = new UserEntity();
    user.setName("test");

    ResponseEntity<UserEntity> response = restTemplate.postForEntity(url, user, UserEntity.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(user.getName(), response.getBody().getName());
  }

  @Test
  void testGetController() {
    String url = "http://127.0.0.1:" + port + "/UserEntity/1";
    ResponseEntity<UserEntity> response = restTemplate.getForEntity(url, UserEntity.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("delegate-invoker-user", response.getBody().getName());
  }

  @TestConfiguration
  static class UserEntityDelegateConfiguration {
    @Bean
    public UserEntityInvokerDelegate userEntityDelegate() {
      return new UserEntityInvokerDelegate();
    }
  }
}
