package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.types.Predicate;

import java.util.Collection;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface ListInvoker<ENTITY> {
  Collection<ENTITY> list(Predicate predicate);
}
