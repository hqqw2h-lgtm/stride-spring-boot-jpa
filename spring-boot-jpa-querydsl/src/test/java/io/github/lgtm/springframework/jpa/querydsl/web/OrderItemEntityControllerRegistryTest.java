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
class OrderItemEntityControllerRegistryTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private OrderItemEntityRepository orderEntityRepository;

  @BeforeEach
  void before() {

    orderEntityRepository.deleteAll();
    String url = "http://127.0.0.1:" + port + "/OrderItem";

    OrderItem orderItem = new OrderItem();
    orderItem.setName("item-001");
    orderItem.setItemId(1L);
    orderItem.setOrderId(12L);

    ResponseEntity<OrderItem> response =
        restTemplate.postForEntity(url, orderItem, OrderItem.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(orderItem.getOrderId(), response.getBody().getOrderId());
    assertEquals(orderItem.getItemId(), response.getBody().getItemId());
  }

  @Test
  void testGetController() {
    String url = "http://127.0.0.1:" + port + "/OrderItem/ids?orderId=12&itemId=1";
    ResponseEntity<OrderItem> response = restTemplate.getForEntity(url, OrderItem.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testListController() {
    String url = "http://127.0.0.1:" + port + "/OrderItem";

    ResponseEntity<List<OrderItem>> listResponseEntity =
        restTemplate.exchange(
            url + "?name=item", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    assertEquals(HttpStatus.OK, listResponseEntity.getStatusCode());
    assertNotNull(listResponseEntity.getBody());
    assertEquals(1, listResponseEntity.getBody().size());
  }

  @Test
  void testDeleteController() {
    String url = "http://127.0.0.1:" + port + "/OrderItem?orderId=12&itemId=1";
    restTemplate.delete(url);
    url = "http://127.0.0.1:" + port + "/OrderItem/ids?orderId=12&itemId=1";

    ResponseEntity<OrderItem> response = restTemplate.getForEntity(url, OrderItem.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
    assertEquals(0, orderEntityRepository.findAll().size());
  }
}
