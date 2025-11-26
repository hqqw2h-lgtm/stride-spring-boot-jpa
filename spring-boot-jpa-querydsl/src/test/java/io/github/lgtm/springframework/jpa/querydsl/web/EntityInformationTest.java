package io.github.lgtm.springframework.jpa.querydsl.web;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.lgtm.springframework.jpa.querydsl.web.entity.OrderEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.QOrderEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.QUserEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@SpringBootTest
@ActiveProfiles("test")
class EntityInformationTest {
  @PersistenceContext private EntityManager em;

  @Test
  void getSingleIdColumnName() {
    Metamodel metamodel = em.getMetamodel();
    EntityType<UserEntity> entityType = metamodel.entity(UserEntity.class);

    QUserEntity qUser = QUserEntity.userEntity;

    EntityInformation info = new EntityInformation(qUser, entityType);

    List<String> idColumns = info.getIdColumnNames();

    assertThat(idColumns.contains("id"));
  }

  @Test
  void getIdClassAttributes() {
    Metamodel metamodel = em.getMetamodel();
    EntityType<OrderEntity> entityType = metamodel.entity(OrderEntity.class);

    QOrderEntity qOrderEntity = QOrderEntity.orderEntity;

    EntityInformation info = new EntityInformation(qOrderEntity, entityType);

    List<String> idColumns = info.getIdColumnNames();

    assertThat(idColumns.contains("id"));
    assertThat(idColumns.contains("version"));
  }
}
