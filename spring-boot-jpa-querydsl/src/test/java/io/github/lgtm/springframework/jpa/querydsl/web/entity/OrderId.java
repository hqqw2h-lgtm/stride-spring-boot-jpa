package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderId implements Serializable {
  private Long orderId;
  private Long version;
}
