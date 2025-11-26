package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Getter
@Setter
@Entity(name = "t_order_entity")
@RestControllerEntity(className = "OrderEntityController")
public class OrderEntity {

  @EmbeddedId
  private OrderId id;

  private String desc;
}
