package io.github.lgtm.springframework.jpa.querydsl.web.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity,Long> {}
