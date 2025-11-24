package io.github.lgtm.springframework.jpa.querydsl.web.invoker;


import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface FindByIdInvoker extends Invoker {
  default Object findById(Object id, EntityInformation entityInformation) {
    throw new UnsupportedOperationException("FindById operation is not supported.");
  }
}
