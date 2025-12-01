package io.github.lgtm.springframework.jpa.querydsl.web.controller;

import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.customizer.DefaultsQuerydslBinderCustomizer;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.*;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.util.Pair;
import org.springframework.data.web.SortDefault;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class ByteBuddyRestControllerFactory implements BeanClassLoaderAware {
  private ClassLoader classLoader;

  @RuntimeType
  public static Object invoke(
      @This InvokeAbleProxyController proxy, @AllArguments Object... arguments) throws Throwable {
    Method method = proxy.getProxyMetaInfo().getTargetMethod();
    Parameter[] parameters = method.getParameters();
    if (parameters.length == 0) {
      return proxy.invoke();
    } else {
      if (arguments == null || arguments.length == 0) {
        if (parameters.length == 1
            && parameters[0].getType().isAssignableFrom(ProxyMetaInfo.class)) {
          return proxy.invoke(proxy.getProxyMetaInfo());
        }
        throw new IllegalArgumentException(
            "Cannot invoke method " + method.getName() + " due to missing arguments");
      } else {
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
          for (int j = 0; j < arguments.length && args[i] == null; j++) {
            if (parameters[i].getType() == arguments[j].getClass()) {
              args[i] = arguments[j];
            }
          }
        }
        for (int i = 0; i < args.length; i++) {
          if (args[i] != null) {
            continue;
          }
          for (Object argument : arguments) {
            if (parameters[i].getType().isAssignableFrom(argument.getClass())) {
              args[i] = argument;
            } else if (parameters[i].getType().isInstance(proxy.getProxyMetaInfo())) {
              args[i] = proxy.getProxyMetaInfo();
            } else if (parameters[i]
                .getType()
                .isInstance(proxy.getProxyMetaInfo().getEntityInformation())) {
              args[i] = proxy.getProxyMetaInfo().getEntityInformation();
            }
          }
        }
        return proxy.invoke(args);
      }
    }
  }

  protected DynamicType.Builder<InvokeAbleProxyController> entityControllerBuilder(
      ProxyMetaInfo proxyMetaInfo) {
    String controllerClassName = getClassName(proxyMetaInfo);
    return new ByteBuddy()
        .subclass(InvokeAbleProxyController.class)
        .name(controllerClassName)
        .annotateType(AnnotationDescription.Builder.ofType(RestController.class).build())
        .annotateType(
            AnnotationDescription.Builder.ofType(RequestMapping.class)
                .defineArray("path", determinateBasePath(proxyMetaInfo))
                .build());
  }

  protected String determinateBasePath(ProxyMetaInfo proxyMetaInfo) {
    return StringUtils.hasText(proxyMetaInfo.getEntity().basePath())
        ? proxyMetaInfo.getEntity().basePath()
        : proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType().getSimpleName();
  }

  public DynamicType.Builder<InvokeAbleProxyController> proxyBuilder(ProxyMetaInfo proxyMetaInfo) {
    DynamicType.Builder<InvokeAbleProxyController> builder = entityControllerBuilder(proxyMetaInfo);
    builder =
        builder
            .method(ElementMatchers.named("getProxyMetaInfo"))
            .intercept(FixedValue.value(proxyMetaInfo));
    return builder;
  }

  public Pair<Class<? extends ProxyController>, Method> generateController(
      ProxyMetaInfo proxyMetaInfo) throws Exception {

    var builder = proxyBuilder(proxyMetaInfo);

    if (proxyMetaInfo.getAction() == Action.CREATE) {
      builder = configureCreateMethod(builder, proxyMetaInfo);
    } else if (proxyMetaInfo.getAction() == Action.UPDATE) {
      builder = configureUpdateMethod(builder, proxyMetaInfo);
    } else if (proxyMetaInfo.getAction() == Action.DELETE) {
      builder = configureDeleteMethod(builder, proxyMetaInfo);
    } else if (proxyMetaInfo.getAction() == Action.FIND_BY_ID) {
      builder = configureFindByIdMethod(builder, proxyMetaInfo);
    } else if (proxyMetaInfo.getAction() == Action.PAGE_LIST) {
      builder = configurePageListMethod(builder, proxyMetaInfo);
    } else if (proxyMetaInfo.getAction() == Action.LIST) {
      builder = configureListMethod(builder, proxyMetaInfo);
    }
    if (builder == null) {
      throw new UnsupportedOperationException("Unsupported action: " + proxyMetaInfo.getAction());
    }
    var proxyController = make(builder, classLoader);
    for (Method method : proxyController.getDeclaredMethods()) {
      if (method.getName().equals("invoke")) {
        return Pair.of(proxyController, method);
      }
    }
    throw new NoSuchMethodException("No invoke method found in " + proxyController.getName());
  }

  public Class<? extends ProxyController> make(
      DynamicType.Builder<InvokeAbleProxyController> builder, ClassLoader classLoader) {
    try (DynamicType.Unloaded<InvokeAbleProxyController> controllerUnloaded = builder.make()) {
      return controllerUnloaded
          .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
          .getLoaded();
    }
  }

  public Class<? extends ProxyController> make(
      DynamicType.Builder<InvokeAbleProxyController> builder) {
    return make(builder, classLoader);
  }

  protected String getClassName(ProxyMetaInfo proxyMetaInfo) {
    return StringUtils.hasText(proxyMetaInfo.getEntity().className())
        ? proxyMetaInfo.getEntity().className() + "_" + proxyMetaInfo.getAction()
        : proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType().getPackageName()
            + ".$_$."
            + proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType().getSimpleName()
            + "Controller_"
            + proxyMetaInfo.getAction();
  }

  DynamicType.Builder<InvokeAbleProxyController> configureCreateMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {

    return builder
        .defineMethod(
            "invoke",
            proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(),
            java.lang.reflect.Modifier.PUBLIC)
        .withParameter(proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(), "entity")
        .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
        .annotateMethod(AnnotationDescription.Builder.ofType(PostMapping.class).build())
        .annotateParameter(0, AnnotationDescription.Builder.ofType(RequestBody.class).build());
  }

  protected DynamicType.Builder<InvokeAbleProxyController> configureUpdateMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {

    return builder
        .defineMethod(
            "invoke",
            proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(),
            java.lang.reflect.Modifier.PUBLIC)
        .withParameter(proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(), "entity")
        .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
        .annotateMethod(AnnotationDescription.Builder.ofType(PutMapping.class).build())
        .annotateParameter(0, AnnotationDescription.Builder.ofType(RequestBody.class).build());
  }

  private DynamicType.Builder<InvokeAbleProxyController> configureDeleteMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {

    var ids = proxyMetaInfo.getEntityInformation().getIdColumnNames();
    if (ids.size() > 1) {
      return builder
          .defineMethod("invoke", Boolean.class, java.lang.reflect.Modifier.PUBLIC)
          .withParameter(proxyMetaInfo.getEntityInformation().getIdClass(), "id")
          .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
          .annotateParameter(0, AnnotationDescription.Builder.ofType(ModelAttribute.class).build())
          .annotateMethod(AnnotationDescription.Builder.ofType(DeleteMapping.class).build());
    } else {
      return builder
          .defineMethod("invoke", void.class, java.lang.reflect.Modifier.PUBLIC)
          .withParameter(proxyMetaInfo.getEntityInformation().getIdClass(), "id")
          .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
          .annotateParameter(
              0,
              AnnotationDescription.Builder.ofType(PathVariable.class)
                  .define("name", "{id}")
                  .build())
          .annotateMethod(
              AnnotationDescription.Builder.ofType(DeleteMapping.class)
                  .defineArray("path", new String[] {"{id}"})
                  .build());
    }
  }

  private DynamicType.Builder<InvokeAbleProxyController> configureFindByIdMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {
    var ids = proxyMetaInfo.getEntityInformation().getIdColumnNames();
    if (ids.size() > 1) {
      return builder
          .defineMethod(
              "invoke",
              proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(),
              java.lang.reflect.Modifier.PUBLIC)
          .withParameter(proxyMetaInfo.getEntityInformation().getIdClass())
          .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
          .annotateParameter(0, AnnotationDescription.Builder.ofType(ModelAttribute.class).build())
          .annotateMethod(
              AnnotationDescription.Builder.ofType(GetMapping.class)
                  .defineArray("path", new String[] {"ids"})
                  .build());
    } else {
      return builder
          .defineMethod(
              "invoke",
              proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType(),
              java.lang.reflect.Modifier.PUBLIC)
          .withParameter(proxyMetaInfo.getEntityInformation().getIdClass(), "id")
          .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
          .annotateParameter(
              0,
              AnnotationDescription.Builder.ofType(PathVariable.class).define("name", "id").build())
          .annotateMethod(
              AnnotationDescription.Builder.ofType(GetMapping.class)
                  .defineArray("path", new String[] {"{id}"})
                  .build());
    }
  }

  private DynamicType.Builder<InvokeAbleProxyController> configurePageListMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {

    return builder
        .defineMethod(
            "invoke",
            TypeDescription.Generic.Builder.parameterizedType(
                    Page.class, proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType())
                .build(),
            java.lang.reflect.Modifier.PUBLIC)
        .withParameter(Pageable.class)
        .withParameter(Predicate.class)
        .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
        .annotateMethod(
            AnnotationDescription.Builder.ofType(GetMapping.class)
                .defineArray("path", "page")
                .build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(SortDefault.class)
                .defineArray(
                    "sort",
                    proxyMetaInfo.getEntityInformation().getIdColumnNames().toArray(String[]::new))
                .define("direction", Sort.Direction.ASC)
                .build())
        .annotateParameter(
            1,
            AnnotationDescription.Builder.ofType(QuerydslPredicate.class)
                .define("root", proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType())
                .define("bindings", DefaultsQuerydslBinderCustomizer.class)
                .build());
  }

  private DynamicType.Builder<InvokeAbleProxyController> configureListMethod(
      DynamicType.Builder<InvokeAbleProxyController> builder, ProxyMetaInfo proxyMetaInfo) {

    return builder
        .defineMethod(
            "invoke",
            TypeDescription.Generic.Builder.parameterizedType(
                    Collection.class,
                    proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType())
                .build(),
            java.lang.reflect.Modifier.PUBLIC)
        .withParameter(Predicate.class)
        .intercept(MethodDelegation.to(ByteBuddyRestControllerFactory.class))
        .annotateMethod(AnnotationDescription.Builder.ofType(GetMapping.class).build())
        .annotateParameter(
            0,
            AnnotationDescription.Builder.ofType(QuerydslPredicate.class)
                .define("root", proxyMetaInfo.getEntityInformation().getEntityPath().getJavaType())
                .define("bindings", DefaultsQuerydslBinderCustomizer.class)
                .build());
  }

  @Override
  public void setBeanClassLoader(@NonNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
}
