package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public abstract class AbstractRestControllerBeanFactory
    implements RestControllerBeanFactory, BeanFactoryAware {
  protected ConfigurableListableBeanFactory beanFactory;

  @Override
  public String create(RestControllerEntity entity, EntityInformation entityInformation)
      throws Exception {
    Object instance = getInstance(entity, entityInformation);

    String name = determinateBeanName(entity, entityInformation, instance);
    beanFactory.autowireBean(instance);
    Object target = beanFactory.initializeBean(instance, name);
    beanFactory.registerSingleton(name, target);
    return name;
  }

  protected String determinateBeanName(
      RestControllerEntity entity, EntityInformation entityInformation, Object instance) {
    return instance.getClass().getName();
  }

  protected abstract Object getInstance(
      RestControllerEntity entity, EntityInformation entityInformation) throws Exception;

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
  }
}
