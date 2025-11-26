package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.EntityPathBaseFinder;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import java.util.Optional;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class RestEntityControllerRegistry implements BeanFactoryAware, SmartInitializingSingleton {
  private final EntityManagerFactory entityManagerFactory;
  private final EntityPathBaseFinder entityPathBaseFinder;
  private ConfigurableListableBeanFactory factory;

  public RestEntityControllerRegistry(
      EntityManagerFactory entityManagerFactory, EntityPathBaseFinder entityPathBaseFinder) {
    this.entityManagerFactory = entityManagerFactory;
    this.entityPathBaseFinder = entityPathBaseFinder;
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.factory = (ConfigurableListableBeanFactory) beanFactory;
  }

  @Override
  public void afterSingletonsInstantiated() {
    entityManagerFactory
        .getMetamodel()
        .getEntities()
        .forEach(
            entity ->
                Optional.ofNullable(resolveAnnotation(entity))
                    .ifPresent(
                        restControllerEntity -> {
                          RequestMappingResolver resolver =
                              factory.getBean(restControllerEntity.controllerFactory());
                          try {
                            entityPathBaseFinder
                                .find(entity)
                                .ifPresent(
                                    entityPathBase -> {
                                      try {
                                        resolver.resolve(
                                            restControllerEntity,
                                            new EntityInformation(entityPathBase, entity));
                                      } catch (Exception e) {
                                        throw new RuntimeException(e);
                                      }
                                    });
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        }));
  }

  protected RestControllerEntity resolveAnnotation(EntityType<?> entityType) {
    return entityType.getJavaType().getAnnotation(RestControllerEntity.class);
  }
}
