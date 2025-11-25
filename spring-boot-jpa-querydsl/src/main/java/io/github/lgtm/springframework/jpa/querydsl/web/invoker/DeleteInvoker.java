package io.github.lgtm.springframework.jpa.querydsl.web.invoker;


import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface DeleteInvoker extends Invoker {
  default void delete(Object id, EntityInformation entityInformation) {
    throw new UnsupportedOperationException("Delete operation is not supported.");
  }
}
