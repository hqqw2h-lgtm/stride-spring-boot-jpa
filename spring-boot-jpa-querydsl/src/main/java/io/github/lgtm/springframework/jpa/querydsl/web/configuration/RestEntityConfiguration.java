package io.github.lgtm.springframework.jpa.querydsl.web.configuration;

import com.querydsl.jpa.JPQLQueryFactory;
import io.github.lgtm.springframework.jpa.querydsl.ClassPathScanningEntityIterator;
import io.github.lgtm.springframework.jpa.querydsl.EntityPathBaseFinder;
import io.github.lgtm.springframework.jpa.querydsl.web.*;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.CrudInterceptor;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.DefaultHandlerFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.InvokerHandlerFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author <a href="hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Configuration
public class RestEntityConfiguration implements BeanFactoryAware {
  private BeanFactory beanFactory;

  @Bean
  @ConditionalOnMissingBean
  public RestEntityControllerRegistry restEntityControllerRegistry(
      EntityManagerFactory entityManagerFactory, EntityPathBaseFinder finder) {
    return new RestEntityControllerRegistry(entityManagerFactory, finder);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  public EntityPathBaseFinder entityPathBaseFinder() {
    return new ClassPathScanningEntityIterator(
        EntityScanPackages.get(beanFactory).getPackageNames().toArray(new String[0]));
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  public RequestControllerMappingResolver resolver(
      RestControllerBeanFactory restControllerBeanFactory,
      RequestMappingHandlerMapping mappingHandlerMapping) {
    return new RequestControllerMappingResolver(restControllerBeanFactory, mappingHandlerMapping);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  @ConditionalOnBean(JPQLQueryFactory.class)
  public CrudInterceptor crudInterceptor(JPQLQueryFactory jpqlQueryFactory) {
    return new CrudInterceptor();
  }

  @ConditionalOnMissingBean
  @Bean
  @Lazy
  @ConditionalOnBean(CrudInterceptor.class)
  public InvokerHandlerFactory invokerHandlerFactory(CrudInterceptor crudInterceptor) {
    return new DefaultHandlerFactory(crudInterceptor);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  public RestControllerBeanFactory restControllerBeanFactory(InvokerHandlerFactory factory) {
    return new ByteBuddyRestControllerBeanFactory(factory);
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
