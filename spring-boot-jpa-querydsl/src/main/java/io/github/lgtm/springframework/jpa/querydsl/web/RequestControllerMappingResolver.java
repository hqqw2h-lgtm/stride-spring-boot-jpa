package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class RequestControllerMappingResolver implements RequestMappingResolver, BeanFactoryAware {
  private final RestControllerBeanFactory restControllerBeanFactory;
  private final RequestMappingHandlerMapping abstractHandlerMethodMapping;
  private BeanFactory beanFactory;

  public RequestControllerMappingResolver(
      RestControllerBeanFactory restControllerBeanFactory,
      RequestMappingHandlerMapping abstractHandlerMethodMapping) {
    this.restControllerBeanFactory = restControllerBeanFactory;
    this.abstractHandlerMethodMapping = abstractHandlerMethodMapping;
  }

  private static Optional<RequestMapping> getRequestMapping(AnnotatedElement element) {
    return MergedAnnotations.from(
            element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none())
        .stream()
        .filter(MergedAnnotationPredicates.typeIn(RequestMapping.class))
        .filter(MergedAnnotationPredicates.firstRunOf(MergedAnnotation::getAggregateIndex))
        .map(MergedAnnotation::synthesize)
        .map(f -> (RequestMapping) f)
        .findFirst();
  }

  @Override
  public void resolve(RestControllerEntity entity, EntityInformation entityInformation)
      throws Exception {
    String beanName = restControllerBeanFactory.create(entity, entityInformation);
    detectHandlerMethods(beanName);
  }

  private RequestMappingInfo.Builder builder(RequestMapping requestMapping) {
    return RequestMappingInfo.paths(requestMapping.path())
        .methods(requestMapping.method())
        .params(requestMapping.params())
        .headers(requestMapping.headers())
        .consumes(requestMapping.consumes())
        .produces(requestMapping.produces())
        .mappingName(requestMapping.name());
  }

  protected Optional<RequestMappingInfo> getMappingForMethod(Method method, Class<?> handlerType) {
    return getRequestMapping(method)
        .map(
            md -> {
              RequestMappingInfo methodMappingInfo = builder(md).build();
              return getRequestMapping(handlerType)
                  .map(
                      type -> {
                        RequestMappingInfo classMappingInfo = builder(type).build();
                        return classMappingInfo.combine(methodMappingInfo);
                      })
                  .orElse(methodMappingInfo);
            });
  }

  protected void detectHandlerMethods(String handler) {
    Class<?> handlerType = beanFactory.getType(handler);
    Assert.notNull(handlerType, "Cannot determine type of bean with name '" + handler + "'");

    Class<?> userType = ClassUtils.getUserClass(handlerType);

    Map<Method, RequestMappingInfo> methods =
        MethodIntrospector.selectMethods(
            userType,
            (MethodIntrospector.MetadataLookup<RequestMappingInfo>)
                method -> {
                  try {
                    return getMappingForMethod(method, userType).orElse(null);
                  } catch (Throwable ex) {
                    throw new IllegalStateException(
                        "Invalid mapping on handler class [" + userType.getName() + "]: " + method,
                        ex);
                  }
                });

    methods.forEach(
        (method, mapping) -> {
          Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
          registerHandlerMethod(handler, invocableMethod, mapping);
        });
  }

  protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
    abstractHandlerMethodMapping.registerMapping(mapping, handler, method);
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
