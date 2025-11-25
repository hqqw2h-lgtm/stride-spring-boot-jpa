package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Getter
@Setter
@Entity(name = "t_order_entity")
@RestControllerEntity(className = "OrderEntityController")
public class OrderEntity {

  @EmbeddedId
  @GeneratedValue(generator = "id-generator", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "id-generator", sequenceName = "seq_id-generator")
  private OrderId id;

  private String desc;
}
