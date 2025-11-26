package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Getter
@Setter
@Entity(name = "t_order_item")
@IdClass(OrderItem.OrderItemId.class)
@RestControllerEntity()
public class OrderItem {

  @Id private Long orderId;
  @Id private Long itemId;

  private String name;

  @Getter
  @Setter
  public static class OrderItemId implements Serializable {
    private Long orderId;
    private Long itemId;
  }
}
