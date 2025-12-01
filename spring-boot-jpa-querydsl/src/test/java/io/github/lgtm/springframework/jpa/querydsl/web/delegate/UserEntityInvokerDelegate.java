package io.github.lgtm.springframework.jpa.querydsl.web.delegate;

import io.github.lgtm.springframework.jpa.querydsl.web.entity.UserEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.FindByIdInvoker;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class UserEntityInvokerDelegate implements FindByIdInvoker<UserEntity, Long> {

  @Override
  public UserEntity findById(Long aLong) {
    UserEntity user = new UserEntity();
    user.setName("delegate-invoker-user");
    return user;
  }
}
