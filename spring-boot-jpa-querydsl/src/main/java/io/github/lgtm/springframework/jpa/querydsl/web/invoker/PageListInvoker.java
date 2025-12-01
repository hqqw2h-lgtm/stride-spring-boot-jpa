package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface PageListInvoker<ENTITY> {
  Page<ENTITY> page(Pageable pageable, Predicate predicate);
}
