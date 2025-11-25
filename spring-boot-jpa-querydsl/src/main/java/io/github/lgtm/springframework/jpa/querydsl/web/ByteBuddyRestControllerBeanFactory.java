package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import io.github.lgtm.springframework.jpa.querydsl.web.controller.ActionDispatcherController;
import io.github.lgtm.springframework.jpa.querydsl.web.customizer.DefaultsQuerydslBinderCustomizer;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.InvokerHandlerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class ByteBuddyRestControllerBeanFactory extends AbstractRestControllerBeanFactory
    implements BeanClassLoaderAware {
  private final InvokerHandlerFactory invokerFactory;
  private ClassLoader classLoader;

  public ByteBuddyRestControllerBeanFactory(InvokerHandlerFactory invokerFactory) {
    this.invokerFactory = invokerFactory;
  }

  protected DynamicType.Builder<ActionDispatcherController<?, ?>> defineConstructor(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder) {
    return builder.constructor(ElementMatchers.any()).intercept(SuperMethodCall.INSTANCE);
  }

  @Override
  protected Object getInstance(RestControllerEntity entity, EntityInformation entityInformation)
      throws Exception {

    InvokerHandlerFactory invokerHandlerFactory =
        determinateInvokerHandlerFactory(entity, entityInformation);
    Class<ActionDispatcherController<?, ?>> clz = generateController(entityInformation, entity);
    return clz.getConstructor(InvokerHandlerFactory.class, EntityInformation.class)
        .newInstance(invokerHandlerFactory, entityInformation);
  }

  protected InvokerHandlerFactory determinateInvokerHandlerFactory(
      RestControllerEntity entity, EntityInformation entityInformation) {
    return invokerFactory;
  }

  @SuppressWarnings("unchecked")
  protected DynamicType.Builder<ActionDispatcherController<?, ?>> entityControllerBuilder(
      EntityInformation entityInformation, RestControllerEntity restControllerEntity) {

    String controllerClassName = getClassName(entityInformation, restControllerEntity);
    return (DynamicType.Builder<ActionDispatcherController<?, ?>>)
        new ByteBuddy()
            .subclass(
                TypeDescription.Generic.Builder.parameterizedType(
                        ActionDispatcherController.class,
                        entityInformation.getEntityPath().getJavaType(),
                        entityInformation.getEntityPath().getIdType().getJavaType())
                    .build())
            .name(controllerClassName)
            .annotateType(AnnotationDescription.Builder.ofType(RestController.class).build())
            .annotateType(
                AnnotationDescription.Builder.ofType(RequestMapping.class)
                    .defineArray(
                        "path", determinateBasePath(entityInformation, restControllerEntity))
                    .build());
  }

  protected String determinateBasePath(
      EntityInformation entityInformation, RestControllerEntity restControllerEntity) {
    return StringUtils.hasText(restControllerEntity.basePath())
        ? restControllerEntity.basePath()
        : entityInformation.getEntityPath().getJavaType().getSimpleName();
  }

  @SuppressWarnings("unchecked")
  public Class<ActionDispatcherController<?, ?>> generateController(
      EntityInformation entityInformation, RestControllerEntity restControllerEntity) {

    DynamicType.Builder<ActionDispatcherController<?, ?>> builder =
        entityControllerBuilder(entityInformation, restControllerEntity);
    builder = defineConstructor(builder);

    builder = configureCreateMethod(builder, entityInformation, restControllerEntity);

    builder = configureUpdateMethod(builder, entityInformation, restControllerEntity);

    builder = configureDeleteMethod(builder, entityInformation, restControllerEntity);

    builder = configureFindByIdMethod(builder, entityInformation, restControllerEntity);

    builder = configurePageListMethod(builder, entityInformation, restControllerEntity);

    builder = configureListMethod(builder, entityInformation, restControllerEntity);

    try (DynamicType.Unloaded<ActionDispatcherController<?, ?>> controllerUnloaded =
        builder.make()) {
      return (Class<ActionDispatcherController<?, ?>>)
          controllerUnloaded.load(classLoader, ClassLoadingStrategy.Default.INJECTION).getLoaded();
    }
  }

  protected String getClassName(
      EntityInformation entityInformation, RestControllerEntity restControllerEntity) {
    return StringUtils.hasText(restControllerEntity.className())
        ? restControllerEntity.className()
        : entityInformation.getEntityPath().getJavaType().getPackageName()
            + ".$_$."
            + entityInformation.getEntityPath().getJavaType().getSimpleName()
            + "Controller";
  }

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configureCreateMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.CREATE, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("create").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(AnnotationDescription.Builder.ofType(PostMapping.class).build())
        .annotateParameter(0, AnnotationDescription.Builder.ofType(RequestBody.class).build());
  }

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configureUpdateMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.UPDATE, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
      return builder;
    }

    return builder
        .method(ElementMatchers.named("update").and(ElementMatchers.takesArguments(1)))
        .intercept(SuperMethodCall.INSTANCE)
        .annotateMethod(AnnotationDescription.Builder.ofType(PutMapping.class).build())
        .annotateParameter(0, AnnotationDescription.Builder.ofType(RequestBody.class).build());
  }

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configureDeleteMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.DELETE, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
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

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configureFindByIdMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.FIND_BY_ID, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
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

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configurePageListMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.PAGE_LIST, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
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
                .defineArray("sort", entityInformation.getIdColumnNames())
                .define("direction", Sort.Direction.ASC)
                .build())
        .annotateParameter(
            1,
            AnnotationDescription.Builder.ofType(QuerydslPredicate.class)
                .define("root", entityInformation.getEntityPath().getJavaType())
                .define("bindings", DefaultsQuerydslBinderCustomizer.class)
                .build())
        .annotateParameter(
            2,
            AnnotationDescription.Builder.ofType(RequestParam.class)
                .define("required", false)
                .build());
  }

  private DynamicType.Builder<ActionDispatcherController<?, ?>> configureListMethod(
      DynamicType.Builder<ActionDispatcherController<?, ?>> builder,
      EntityInformation entityInformation,
      RestControllerEntity restControllerEntity) {
    if (!isMethodInclude(
        Action.LIST, entityInformation.getEntityPath().getJavaType(), restControllerEntity)) {
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
                .define("root", entityInformation.getEntityPath().getJavaType())
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
  public void setBeanClassLoader(@NonNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
}
