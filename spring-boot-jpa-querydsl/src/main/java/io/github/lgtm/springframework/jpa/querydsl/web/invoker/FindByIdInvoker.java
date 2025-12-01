package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface FindByIdInvoker<ENTITY, ID> {
  ENTITY findById(ID id);
}
