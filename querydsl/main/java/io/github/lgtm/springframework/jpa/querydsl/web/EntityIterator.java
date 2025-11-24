package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface EntityIterator {
  void doScan(String[] packages, ClassLoader classLoader);

  Set<Class<?>> getEntityClasses();

  <T> Optional<EntityPathBase<T>> find(Class<T> entityClass) throws IllegalAccessException;
}
