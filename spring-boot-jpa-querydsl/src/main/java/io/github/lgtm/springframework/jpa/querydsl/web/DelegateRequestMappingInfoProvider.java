package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.helper.SpringFactoryContainer;
import io.github.lgtm.springframework.jpa.proxy.bytebuddy.DynamicTypeMethodHelper;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.InvokerDelegate;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.ByteBuddyRestControllerFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.ProxyMetaInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.github.lgtm.springframework.jpa.querydsl.web.mapping.InvokerMappingProvider;
import net.bytebuddy.implementation.MethodDelegation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.*;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class DelegateRequestMappingInfoProvider
    implements RequestMappingInfoProvider,
        BeanPostProcessor,
        BeanFactoryAware,
        SmartInitializingSingleton {
  private final Map<Pair<Action, Class<?>>, Pair<String, Method>> delegateMappings =
      new ConcurrentHashMap<>();
  private final Map<Action, Pair<String, Method>> defaultDelegate = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Object> lazyProcessedBeans = new ConcurrentHashMap<>();
  private ByteBuddyRestControllerFactory byteBuddyRestControllerFactory;
  private ConfigurableListableBeanFactory beanFactory;
  private SpringFactoryContainer container;
  private InvokerMappingProvider invokerMappingProvider;

  @Autowired
  public void setByteBuddyRestControllerFactory(
      ByteBuddyRestControllerFactory byteBuddyRestControllerFactory) {
    this.byteBuddyRestControllerFactory = byteBuddyRestControllerFactory;
  }

  @Override
  public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
      throws BeansException {
    lazyProcessedBeans.putIfAbsent(beanName, bean);
    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }

  protected void visitPairs(InvokerDelegate invokerDelegate, String beanName, Method method) {
    if (invokerDelegate.entity().length == 0) {
      throw new IllegalStateException("InvokerDelegate entity class must be set.");
    } else {
      Arrays.stream(invokerDelegate.entity())
          .forEach(
              f -> {
                if (f == InvokerDelegate._DEFAULT.class) {
                  Pair<String, Method> pair = Pair.of(beanName, method);
                  defaultDelegate.putIfAbsent(invokerDelegate.action(), pair);
                } else {
                  var key = Pair.<Action, Class<?>>of(invokerDelegate.action(), f);
                  if (delegateMappings.containsKey(key)) {
                    throw new IllegalStateException(
                        "Duplicate InvokerDelegate for action "
                            + invokerDelegate.action()
                            + " and entityClass "
                            + f.getName());
                  }
                  delegateMappings.put(key, Pair.of(beanName, method));
                }
              });
    }
  }

  protected Pair<String, Method> proxyExistingRequestMappingService(
      RequestMapping requestMapping,
      Pair<Action, Class<?>> controllerDefinition,
      Pair<String, Method> service,
      ProxyMetaInfo proxyMetaInfo)
      throws Exception {
    var builder = byteBuddyRestControllerFactory.proxyBuilder(proxyMetaInfo);
    builder =
        DynamicTypeMethodHelper.cloneMethod(service.getSecond(), builder)
            .intercept(MethodDelegation.to(beanFactory.getBean(service.getFirst())));
    var applied = container.applySingleton(byteBuddyRestControllerFactory.make(builder));
    return Pair.of(applied.getFirst(), service.getSecond());
  }

  protected Pair<String, Method> proxyDefaultRequestMapping(
      Pair<Action, Class<?>> controllerDefinition,
      Pair<String, Method> service,
      ProxyMetaInfo proxyMetaInfo)
      throws Exception {
    var proxy = byteBuddyRestControllerFactory.generateController(proxyMetaInfo);
    var applied = container.applySingleton(proxy.getFirst());
    return Pair.of(applied.getFirst(), proxy.getSecond());
  }

  protected Pair<String, Method> proxyController(
      Pair<Action, Class<?>> controllerDefinition,
      Pair<String, Method> service,
      ProxyMetaInfo proxyMetaInfo)
      throws Exception {
    var requestMapping = AnnotationUtils.findAnnotation(service.getSecond(), RequestMapping.class);
    return requestMapping == null
        ? proxyDefaultRequestMapping(controllerDefinition, service, proxyMetaInfo)
        : proxyExistingRequestMappingService(
            requestMapping, controllerDefinition, service, proxyMetaInfo);
  }

  @Override
  public Optional<RequestMappingInfoWrapper> getRequestMappingInfoWrapper(
      Action action,
      RestControllerEntity restControllerEntity,
      EntityInformation entityInformation) {

    processLazyBeansIfNecessary();

    var key = Pair.<Action, Class<?>>of(action, entityInformation.getEntityPath().getJavaType());
    return invokerMappingProvider
        .getTarget(new ActionMapping.MappingKey(action, entityInformation))
        .map(targetHandler -> Pair.of(targetHandler.getBeanName(), targetHandler.getMethod()))
        .or(() -> Optional.ofNullable(delegateMappings.get(key)))
        .or(() -> Optional.ofNullable(defaultDelegate.get(action)))
        .map(
            cached -> {
              Controller controller =
                  AnnotationUtils.findAnnotation(
                      cached.getSecond().getDeclaringClass(), Controller.class);

              if (controller != null) {
                return RequestMappingInfoProvider.OBJECT;
              }

              ProxyMetaInfo proxyMetaInfo = new ProxyMetaInfo();
              proxyMetaInfo.setAction(action);
              proxyMetaInfo.setEntityInformation(entityInformation);
              proxyMetaInfo.setEntity(restControllerEntity);
              proxyMetaInfo.setTargetMethod(cached.getSecond());
              proxyMetaInfo.setTargetBeanName(cached.getFirst());
              try {

                var proxyController = proxyController(key, cached, proxyMetaInfo);
                var controllerStub = beanFactory.getBean(proxyController.getFirst());
                var clz = ClassUtils.getUserClass(controllerStub);

                var requestMapping =
                    getAnnotationDescriptors(proxyController.getSecond()).stream()
                        .filter(f -> f.synthesize() instanceof RequestMapping)
                        .map(f -> (RequestMapping) f.synthesize())
                        .findFirst()
                        .orElse(null);

                var mth = builder(Objects.requireNonNull(requestMapping)).build();
                var classRequestMappings =
                    getAnnotationDescriptors(clz).stream()
                        .filter(f -> f.synthesize() instanceof RequestMapping)
                        .map(f -> (RequestMapping) f.synthesize())
                        .findFirst()
                        .orElse(null);
                if (classRequestMappings != null) {
                  mth = builder(classRequestMappings).build().combine(mth);
                }
                return new RequestMappingInfoWrapper(
                    proxyController.getFirst(), proxyController.getSecond(), mth);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  private List<MergedAnnotation<Annotation>> getAnnotationDescriptors(AnnotatedElement element) {
    return MergedAnnotations.from(
            element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none())
        .stream()
        .filter(MergedAnnotationPredicates.typeIn(RequestMapping.class))
        .filter(MergedAnnotationPredicates.firstRunOf(MergedAnnotation::getAggregateIndex))
        .distinct()
        .toList();
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

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    container = new SpringFactoryContainer((ConfigurableListableBeanFactory) beanFactory);
    this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    this.invokerMappingProvider = new InvokerMappingProvider(this.beanFactory);
  }

  @Override
  public void afterSingletonsInstantiated() {
    processLazyBeansIfNecessary();
  }

  protected void processLazyBeansIfNecessary() {

    lazyProcessedBeans
        .entrySet()
        .removeIf(
            entry -> {
              String beanName = entry.getKey();
              Object bean = entry.getValue();
              if (beanFactory.isSingleton(beanName)) {
                Class<?> userType = ClassUtils.getUserClass(bean);

                MethodIntrospector.selectMethods(
                        userType,
                        (ReflectionUtils.MethodFilter)
                            method -> method.isAnnotationPresent(InvokerDelegate.class))
                    .forEach(
                        method -> {
                          InvokerDelegate invokerDelegate =
                              method.getAnnotation(InvokerDelegate.class);
                          visitPairs(invokerDelegate, beanName, method);
                        });
              }
              return true;
            });
  }
}
