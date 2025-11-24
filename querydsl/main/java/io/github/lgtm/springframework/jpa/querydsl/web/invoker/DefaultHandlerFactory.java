package io.github.lgtm.springframework.jpa.querydsl.web.invoker;


import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;

import java.util.Optional;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class DefaultHandlerFactory implements InvokerHandlerFactory {
  private final CrudInterceptor crudInterceptor;

  public DefaultHandlerFactory(CrudInterceptor crudInterceptor) {
    this.crudInterceptor = crudInterceptor;
  }

  @Override
  public Optional<Invoker> getHandlerMethod(Action action, EntityInformation entityInformation) {
    return Optional.of(crudInterceptor);
  }
}
