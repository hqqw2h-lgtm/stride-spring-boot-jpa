package io.github.lgtm.springframework.jpa.querydsl.web;

 import java.lang.annotation.*;

 import io.github.lgtm.springframework.jpa.querydsl.web.controller.EntityControllerTemplate;
 import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EntityControllerBeanDefinitionRegistrar.class)
public @interface EnableAutoEntityController {
  /**
   * the bean must implement EntityControllerTemplate
   *
   * @see EntityControllerTemplate
   */
  String controllerDelegatorName() default "";

  Class<? extends ControllerBeanDefinitionFactory> controllerFactory() default
      EntityControllerFactoryBean.class;

  String[] basePackages();

  Class<? extends EntityIterator> finder() default ClassPathScanningEntityIterator.class;
}
