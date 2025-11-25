package io.github.lgtm.springframework.jpa.querydsl.web.annotation;

import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.RequestControllerMappingResolver;
import io.github.lgtm.springframework.jpa.querydsl.web.RequestMappingResolver;
import java.lang.annotation.*;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestControllerEntity {

  String basePath() default "";

  String className() default "";

  Action[] exclude() default {};

  Class<? extends RequestMappingResolver> controllerFactory() default
      RequestControllerMappingResolver.class;
}
