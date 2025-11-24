package io.github.lgtm.springframework.jpa.querydsl.web.invoker;


import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface UpdateInvoker extends Invoker {
  default Object update(Object entity, EntityInformation entityInformation) {
    throw new UnsupportedOperationException("Update operation is not supported.");
  }
}
