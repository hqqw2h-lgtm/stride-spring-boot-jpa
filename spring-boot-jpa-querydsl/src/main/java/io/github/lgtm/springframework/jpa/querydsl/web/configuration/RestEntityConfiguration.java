package io.github.lgtm.springframework.jpa.querydsl.web.configuration;

import com.querydsl.jpa.JPQLQueryFactory;
import io.github.lgtm.springframework.jpa.querydsl.ClassPathScanningEntityIterator;
import io.github.lgtm.springframework.jpa.querydsl.EntityPathBaseFinder;
import io.github.lgtm.springframework.jpa.querydsl.web.*;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.ByteBuddyRestControllerFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.DefaultDelegator;
import java.util.List;
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
  @Lazy
  public DelegateRequestMappingInfoProvider delegateRequestMappingInfoProvider() {
    return new DelegateRequestMappingInfoProvider();
  }

  @Bean
  @Lazy
  public RequestMappingInfoFilterProvider requestMappingInfoFilterProvider() {
    return new RequestMappingInfoFilterProvider();
  }

  @Bean
  @ConditionalOnMissingBean
  public RestEntityControllerRegistry restEntityControllerRegistry(EntityPathBaseFinder finder) {
    return new RestEntityControllerRegistry(finder);
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
      RequestMappingHandlerMapping mappingHandlerMapping,
      List<RequestMappingInfoProvider> providers) {
    return new RequestControllerMappingResolver(mappingHandlerMapping, providers);
  }

  @Bean
  @Lazy
  @ConditionalOnMissingBean
  public ByteBuddyRestControllerFactory byteBuddyRestControllerFactory() {
    return new ByteBuddyRestControllerFactory();
  }

  @Bean
  @ConditionalOnBean(JPQLQueryFactory.class)
  public DefaultDelegator crudInterceptor(JPQLQueryFactory jpqlQueryFactory) {
    return new DefaultDelegator();
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
