package io.github.lgtm.springframework.jpa.querydsl.web;

import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface RestEntityControllerContext {
  Class<? extends ControllerBeanDefinitionFactory> getDefaultControllerBeanDefinitionFactory();

  Map<String, Object> getAttribute();

  BeanDefinitionRegistry getRegistry();

  BeanNameGenerator getBeanNameGenerator();
}
