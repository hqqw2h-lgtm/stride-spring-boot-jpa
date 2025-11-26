package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

import lombok.*;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Embeddable
@Getter
@Setter
public class OrderId implements Serializable {
  private Long orderId;
  private Long version;
}
