package io.github.lgtm.springframework.jpa.querydsl.web;


import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.ActionDispatcherController;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.EntityControllerTemplate;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.CrudInterceptor;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.DefaultHandlerFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.predicate.customizer.DefaultsQuerydslBinderCustomizer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@SuppressWarnings("rawtypes")
public class EntityControllerFactoryBean implements ControllerBeanDefinitionFactory {
  public static final String INVOKER_HANDLER_FACTORY_BEAN_NAME =
      "INVOKER_HANDLER_FACTORY_BEAN_NAME";
  public static final String DEFAULT_CRUD_INTERCEPTOR_BEAN_NAME = "CRUD_INTERCEPTOR_BEAN_NAME";

  protected DynamicType.Builder<ActionDispatcherController> defineConstructor(
      DynamicType.Builder<ActionDispatcherController> builder) throws NoSuchMethodException {
    return builder.constructor(ElementMatchers.any()).intercept(SuperMethodCall.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  protected DynamicType.Builder<ActionDispatcherController> entityControllerBuilder(
      Class<?> entityClass, RestControllerEntity restControllerEntity) {

    String controllerClassName = getClassName(entityClass, restControllerEntity);
    return (DynamicType.Builder<ActionDispatcherController>)
        new ByteBuddy()
            .subclass(
                TypeDescription.Generic.Builder.parameterizedType(
                        ActionDispatcherController.class,
                        entityClass,
                        restControllerEntity.idClass())
                    .build())
            .name(controllerClassName)
            .annotateType(AnnotationDescription.Builder.ofType(RestController.class).build())
            .annotateType(
                AnnotationDescription.Builder.ofType(RequestMapping.class)
                    .defineArray("path", determinateBasePath(entityClass, restControllerEntity))
                    .build());
  }

  protected String determinateBasePath(
      Class<?> entityClass, RestControllerEntity restControllerEntity) {
    return StringUtils.hasText(restControllerEntity.basePath())
        ? restControllerEntity.basePath()
        : entityClass.getSimpleName();
  }

  @SuppressWarnings("unchecked")
  public Class<ActionDispatcherController> generateController(
      EntityInformation entityInformation,
      ClassLoader classLoader,
      RestControllerEntity restControllerEntity)
      throws NoSuchMethodException {

    DynamicType.Builder<ActionDispatcherController> builder =
        entityControllerBuilder(entityInformation.entityClass(), restControllerEntity);
    builder = defineConstructor(builder);

    builder =
        configureCreateMethod(builder, entityInformation.entityClass(), restControllerEntity);

    builder =
        configureUpdateMethod(builder, entityInformation.entityClass(), restControllerEntity);

    builder =
        configureDeleteMethod(builder, entityInformation.entityClass(), restControllerEntity);

    builder =
        configureFindByIdMethod(builder, entityInformation.entityClass(), restControllerEntity);

    builder =
        configurePageListMethod(builder, entityInformation.entityClass(), restControllerEntity);

    builder =
        configureListMethod(builder, entityInformation.entityClass(), restControllerEntity);

    try (DynamicType.Unloaded<ActionDispatcherController> controllerUnloaded = builder.make()) {
      return (Class<ActionDispatcherController>)
          controllerUnloaded.load(classLoader, ClassLoadingStrategy.Default.INJECTION).getLoaded();
    }
  }

  protected TypeDescription.Generic buildTypeDescriptionGeneric(
      Class<?> entityClass, RestControllerEntity restControllerEntity) {
    return TypeDescription.Generic.Builder.parameterizedType(
            EntityControllerTemplate.class, entityClass, restControllerEntity.idClass())
        .build();
  }

  protected String getClassName(Class<?> entityClass, RestControllerEntity restControllerEntity) {
    return StringUtils.hasText(restControllerEntity.className())
        ? restControllerEntity.className()
        : entityClass.getPackageName() + ".$_$." + entityClass.getSimpleName() + "Controller";
  }

  private DynamicType.Builder<ActionDispatcherController> configureCreateMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.CREATE, entityClass, restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("create").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(AnnotationDescription.Builder.ofType(PostMapping.class).build());
  }

  private DynamicType.Builder<ActionDispatcherController> configureUpdateMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.UPDATE, entityClass, restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("update").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(AnnotationDescription.Builder.ofType(PutMapping.class).build());
  }

  private DynamicType.Builder<ActionDispatcherController> configureDeleteMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.DELETE, entityClass, restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("delete").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(
            AnnotationDescription.Builder.ofType(DeleteMapping.class)
                .defineArray("path", "/{id}")
                .build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(PathVariable.class).define("name", "id").build());
  }

  private DynamicType.Builder<ActionDispatcherController> configureFindByIdMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.FIND_BY_ID, entityClass, restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("findById").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(
            AnnotationDescription.Builder.ofType(GetMapping.class)
                .defineArray("path", "/{id}")
                .build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(PathVariable.class).define("name", "id").build());
  }

  private DynamicType.Builder<ActionDispatcherController> configurePageListMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.PAGE_LIST, entityClass, restControllerEntity)) {
      return builder;
    }

    return builder
        .method(
            ElementMatchers.named("pageList")
                .and(
                    ElementMatchers.takesArguments(
                        Pageable.class,
                        Predicate.class,
                        MultiValueMap.class,
                        HttpHeaders.class,
                        HttpServletRequest.class)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(
            AnnotationDescription.Builder.ofType(GetMapping.class)
                .defineArray("path", "page")
                .build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(SortDefault.class)
                .defineArray("sort", restControllerEntity.idColumns())
                .define("direction", Sort.Direction.ASC)
                .build())
        .annotateParameter(
            1,
            AnnotationDescription.Builder.ofType(QuerydslPredicate.class)
                .define("root", entityClass)
                .define("bindings", DefaultsQuerydslBinderCustomizer.class)
                .build())
        .annotateParameter(
            2,
            AnnotationDescription.Builder.ofType(RequestParam.class)
                .define("required", false)
                .build());
  }

  private DynamicType.Builder<ActionDispatcherController> configureListMethod(
      DynamicType.Builder<ActionDispatcherController> builder,
      Class<?> entityClass,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(Action.LIST, entityClass, restControllerEntity)) {
      return builder;
    }
    return builder
        .method(
            ElementMatchers.named("list")
                .and(
                    ElementMatchers.takesArguments(
                        Predicate.class,
                        MultiValueMap.class,
                        HttpHeaders.class,
                        HttpServletRequest.class)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(AnnotationDescription.Builder.ofType(GetMapping.class).build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(QuerydslPredicate.class)
                .define("root", entityClass)
                .define("bindings", DefaultsQuerydslBinderCustomizer.class)
                .build())
        .annotateParameter(
            1,
            AnnotationDescription.Builder.ofType(RequestParam.class)
                .define("required", false)
                .build());
  }

  protected boolean isMethodInclude(
      Action action, Class<?> entityClass, RestControllerEntity restControllerEntity) {
    return restControllerEntity.exclude().length == 0
        || Arrays.stream(restControllerEntity.exclude()).noneMatch(exclude -> action == exclude);
  }

  @Override
  public BeanDefinitionBuilder controllerDefinition(
      EntityInformation entityInformation,
      ClassLoader classLoader,
      RestControllerEntity restControllerEntity,
      RestEntityControllerContext context)
      throws Exception {
    if (!context.getRegistry().containsBeanDefinition(INVOKER_HANDLER_FACTORY_BEAN_NAME)) {
      if (!context.getRegistry().containsBeanDefinition(DEFAULT_CRUD_INTERCEPTOR_BEAN_NAME)) {
        BeanDefinitionBuilder builder =
            BeanDefinitionBuilder.genericBeanDefinition(CrudInterceptor.class);
        context
            .getRegistry()
            .registerBeanDefinition(
                DEFAULT_CRUD_INTERCEPTOR_BEAN_NAME, builder.getBeanDefinition());
      }
      BeanDefinitionBuilder builder =
          BeanDefinitionBuilder.genericBeanDefinition(DefaultHandlerFactory.class)
              .addConstructorArgReference(DEFAULT_CRUD_INTERCEPTOR_BEAN_NAME);
      context
          .getRegistry()
          .registerBeanDefinition(INVOKER_HANDLER_FACTORY_BEAN_NAME, builder.getBeanDefinition());
    }

    BeanDefinitionBuilder builder =
        BeanDefinitionBuilder.genericBeanDefinition(
            generateController(entityInformation, classLoader, restControllerEntity));
    builder.addConstructorArgReference(INVOKER_HANDLER_FACTORY_BEAN_NAME);
    builder.addConstructorArgValue(entityInformation);
    return builder;
  }
}
