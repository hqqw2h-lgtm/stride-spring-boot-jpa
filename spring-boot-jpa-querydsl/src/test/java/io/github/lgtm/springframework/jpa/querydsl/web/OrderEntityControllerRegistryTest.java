package io.github.lgtm.springframework.jpa.querydsl.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.lgtm.springframework.jpa.querydsl.web.entity.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderEntityControllerRegistryTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private OrderEntityRepository orderEntityRepository;

  @BeforeEach
  void before() {

    orderEntityRepository.deleteAll();
    String url = "http://127.0.0.1:" + port + "/OrderEntity";

    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setDesc("order-001");
    OrderId orderId = new OrderId();
    orderId.setOrderId(1L);
    orderId.setVersion(2L);
    orderEntity.setId(orderId);

    ResponseEntity<OrderEntity> response =
        restTemplate.postForEntity(url, orderEntity, OrderEntity.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(orderEntity.getId().getOrderId(), response.getBody().getId().getOrderId());
    assertEquals(orderEntity.getId().getVersion(), response.getBody().getId().getVersion());
  }

  @Test
  void testGetController() {
    String url = "http://127.0.0.1:" + port + "/OrderEntity/ids?orderId=1&version=2";
    ResponseEntity<OrderEntity> response = restTemplate.getForEntity(url, OrderEntity.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testListController() {
    String url = "http://127.0.0.1:" + port + "/OrderEntity";

    ResponseEntity<List<OrderEntity>> listResponseEntity =
        restTemplate.exchange(
            url + "?desc=order", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    assertEquals(HttpStatus.OK, listResponseEntity.getStatusCode());
    assertNotNull(listResponseEntity.getBody());
    assertEquals(1, listResponseEntity.getBody().size());
  }

  @Test
  void testDeleteController() {
    String url = "http://127.0.0.1:" + port + "/OrderEntity?orderId=1&version=2";
    restTemplate.delete(url);
    url = "http://127.0.0.1:" + port + "/OrderEntity/ids?orderId=1&version=2";
    ResponseEntity<UserEntity> response = restTemplate.getForEntity(url, UserEntity.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
    assertEquals(0, orderEntityRepository.findAll().size());
  }
}
