package io.github.lgtm.springframework.jpa.querydsl.web.invoker;


import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface CreateInvoker extends Invoker {
  default Object create(Object entity, EntityInformation entityInformation) {
    throw new UnsupportedOperationException("Create operation is not supported.");
  }
}
