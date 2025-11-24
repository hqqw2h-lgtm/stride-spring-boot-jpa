package io.github.lgtm.springframework.jpa.querydsl.web.annotation;


import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.ControllerBeanDefinitionFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityControllerFactoryBean;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestControllerEntity {
  /**
   * 这两个属性有点问题,jpa事实上提供了entity信息的支持,所以这两个字段理论上是多余的; 如果能在jpa初始化之后 request
   * mapping处理之前解析到,应该是呢能做到的,或者直接注册request mapping
   */
  Class<?> idClass() default Long.class;

  String[] idColumns() default {"id"};

  String basePath() default "";

  String className() default "";

  Action[] exclude() default {};

  Class<? extends ControllerBeanDefinitionFactory> controllerFactory() default
      EntityControllerFactoryBean.class;
}
