package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class RequestControllerMappingResolver implements RequestMappingResolver, BeanFactoryAware {
  private final RequestMappingHandlerMapping abstractHandlerMethodMapping;
  private BeanFactory beanFactory;
  private final List<RequestMappingInfoProvider> requestMappingInfoProvider;

  public RequestControllerMappingResolver(
      RequestMappingHandlerMapping abstractHandlerMethodMapping,
      List<RequestMappingInfoProvider> requestMappingInfoProvider) {
    this.abstractHandlerMethodMapping = abstractHandlerMethodMapping;
    this.requestMappingInfoProvider = requestMappingInfoProvider;
  }

  @Override
  public void resolve(RestControllerEntity entity, EntityInformation entityInformation) {
    for (Action action : Action.values()) {
      for (RequestMappingInfoProvider provider : requestMappingInfoProvider) {
        Optional<RequestMappingInfoProvider.RequestMappingInfoWrapper> mappingInfoWrapper =
            provider.getRequestMappingInfoWrapper(action, entity, entityInformation);
        if (mappingInfoWrapper.isPresent()) {
          RequestMappingInfoProvider.RequestMappingInfoWrapper wrapper = mappingInfoWrapper.get();
          if (wrapper != RequestMappingInfoProvider.OBJECT) {
            Class<?> handlerType = beanFactory.getType(wrapper.getBeanName());
            Class<?> userType = ClassUtils.getUserClass(Objects.requireNonNull(handlerType));

            Method invocableMethod = AopUtils.selectInvocableMethod(wrapper.getMethod(), userType);
            registerHandlerMethod(wrapper.getBeanName(), invocableMethod, wrapper.getMapping());
          }
          break;
        }
      }
    }
  }

  protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
    abstractHandlerMethodMapping.registerMapping(mapping, handler, method);
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
