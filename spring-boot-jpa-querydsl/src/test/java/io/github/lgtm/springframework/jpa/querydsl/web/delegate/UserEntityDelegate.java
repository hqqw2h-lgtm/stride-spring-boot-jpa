package io.github.lgtm.springframework.jpa.querydsl.web.delegate;

import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.InvokerDelegate;
import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntity;
import org.springframework.core.Ordered;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class UserEntityDelegate implements Ordered {
  @InvokerDelegate(
      entity = {UserEntity.class},
      action = Action.FIND_BY_ID)
  public UserEntity findById(Long id) {
    UserEntity user = new UserEntity();
    user.setName("delegate-user");
    return user;
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }
}
