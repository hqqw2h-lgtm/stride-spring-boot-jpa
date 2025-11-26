package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Entity
@Table(name = "t_user_entity")
@RestControllerEntity
@Getter
@Setter
public class UserEntity {

  @Id
  @GeneratedValue(generator = "id-generator", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "id-generator", sequenceName = "seq_id-generator")
  private Long id;

  private String name;
}
