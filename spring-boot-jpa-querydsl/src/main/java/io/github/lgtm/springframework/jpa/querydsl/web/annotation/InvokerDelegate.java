package io.github.lgtm.springframework.jpa.querydsl.web.annotation;

import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvokerDelegate {
  Class<?>[] entity();

  Action action();

  class _DEFAULT {}
}
