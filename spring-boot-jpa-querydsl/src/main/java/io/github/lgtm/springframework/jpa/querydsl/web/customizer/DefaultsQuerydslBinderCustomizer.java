package io.github.lgtm.springframework.jpa.querydsl.web.customizer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ParameterizedExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.*;
import jakarta.persistence.Transient;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponseException;

/**
 * Default Querydsl Binder Customizer. This class customizes the Querydsl binding for Spring Data
 * repositories. It primarily modifies string field bindings to use 'like' queries and manages the
 * inclusion and exclusion of properties for binding.
 *
 * @author Weiwei Han <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Slf4j
public class DefaultsQuerydslBinderCustomizer
    implements org.springframework.data.querydsl.binding.QuerydslBinderCustomizerDefaults {
  public static MethodHandles.Lookup lookup = MethodHandles.lookup();
  @Setter private NameStrategy nameStrategy = new CamelCaseNameStrategy();

  /**
   * Retrieves user-defined properties of the entity. This method excludes properties without both
   * getter and setter, as well as properties annotated with @Transient.
   *
   * @param root the root entity path
   * @return a set of user-defined property descriptors
   */
  private static Set<PropertyDescriptor> getUserDefinedProperties(Path<?> root) {
    Set<PropertyDescriptor> propertyDescriptors = new HashSet<>();
    Class<?> clz = ResolvableType.forInstance(root).getSuperType().getGeneric(0).getRawClass();
    if (clz == null) {
      return propertyDescriptors;
    }
    for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(clz)) {
      if (descriptor.getReadMethod() == null || descriptor.getWriteMethod() == null) {
        continue;
      }
      if (null
              == AnnotationUtils.findAnnotation(
                  Objects.requireNonNull(ReflectionUtils.findField(clz, descriptor.getName())),
                  Transient.class)
          && !descriptor.getReadMethod().isAnnotationPresent(Transient.class)
          && !descriptor.getWriteMethod().isAnnotationPresent(Transient.class)
          && null
              == AnnotationUtils.findAnnotation(
                  Objects.requireNonNull(ReflectionUtils.findField(clz, descriptor.getName())),
                  JsonIgnore.class)
          && null
              == AnnotationUtils.findAnnotation(
                  Objects.requireNonNull(ReflectionUtils.findField(clz, descriptor.getName())),
                  JsonBackReference.class)) {
        propertyDescriptors.add(descriptor);
      }
    }
    return propertyDescriptors;
  }

  private static BooleanExpression string2Like(StringPath stringPath, String value) {
    return stringPath.startsWith(value);
  }

  @SneakyThrows
  protected static BooleanExpression bindingWithRawTypeEquals(
      SimpleExpression<?> path, Object value) {
    return (BooleanExpression)
        lookup
            .findVirtual(
                SimpleExpression.class,
                "eq",
                MethodType.methodType(BooleanExpression.class, Object.class))
            .bindTo(path)
            .invoke(value);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  protected static BooleanExpression bindingWithRawType(Path<?> path, Collection<?> value) {
    if (path instanceof TemporalExpression<?>) {
      return processWithDateTimeRangeQuery((TemporalExpression<?>) path, value);
    }
    if (value.size() == 1) {
      if (path instanceof StringPath) {
        return string2Like((StringPath) path, value.iterator().next().toString());
      }
      return bindingWithRawTypeEquals((SimpleExpression<?>) path, value.iterator().next());
    }
    return processCollections(path, (Collection<Object>) value);
  }

  protected static BooleanExpression processCollections(Path<?> path, Collection<Object> value)
      throws Throwable {
    if (path instanceof TemporalExpression<?>) {
      return processWithDateTimeRangeQuery((TemporalExpression<?>) path, value);
    }
    return (BooleanExpression)
        lookup
            .findVirtual(
                path.getClass(),
                "in",
                MethodType.methodType(BooleanExpression.class, Collection.class))
            .bindTo(path)
            .invoke(value);
  }

  protected static BooleanExpression processWithDateTimeRangeQuery(
      TemporalExpression<?> path, Collection<?> value) throws Throwable {
    if (value.size() != 2) {
      throw new ErrorResponseException(
          HttpStatus.BAD_REQUEST,
          new IllegalArgumentException("Date time required  2 values for range query"));
    }
    List<?> objects = value.stream().sorted().toList();
    Object lowBound = objects.get(0);
    Object upperBound = objects.get(objects.size() - 1);

    return (BooleanExpression)
        lookup
            .findVirtual(
                ComparableExpression.class,
                "between",
                MethodType.methodType(BooleanExpression.class, Comparable.class, Comparable.class))
            .bindTo(path)
            .invoke(lowBound, upperBound);
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private void doBind(QuerydslBindings querydslBindings, Path<?> root, List<String> traces) {
    if (root == null) {
      return;
    }

    if (root instanceof CollectionPathBase<?, ?, ?> collectionPathBase) {
        SimpleExpression<?> expression = collectionPathBase.any();
      if (expression instanceof Path<?>) {
        doBind(querydslBindings, (Path<?>) expression, traces);
      }
    } else if (root instanceof BeanPath<?>) {
      Set<PropertyDescriptor> propertyDescriptors = getUserDefinedProperties(root);
      ReflectionUtils.doWithFields(
          root.getClass(),
          field -> {
            Path<?> path = (Path<?>) field.get(root);
            traces.add(field.getName());
            doBind(querydslBindings, path, traces);
            traces.remove(traces.size() - 1);
          },
          field ->
              Modifier.isFinal(field.getModifiers())
                  && Path.class.isAssignableFrom(field.getType())
                  && propertyDescriptors.stream()
                      .anyMatch(f -> Objects.equals(f.getName(), field.getName())));
    } else if (!(root instanceof ParameterizedExpression)) {
      if (root instanceof SimpleExpression) {

        String als = nameStrategy.getName(traces);
        querydslBindings
            .bind(root)
            .as(als)
            .all((path, value) -> Optional.ofNullable(bindingWithRawType(path, value)));
        Field field = QuerydslBindings.class.getDeclaredField("denyList");
        field.setAccessible(true);
        Set<String> values = (Set<String>) field.get(querydslBindings);
        values.remove(als);
      }
    }
  }

  /**
   * Customizes the QuerydslBindings for the given entity path. It excludes unlisted properties and
   * binds string fields to 'like' queries.
   *
   * @param bindings the QuerydslBindings to customize
   * @param root the root entity path
   */
  @Override
  public void customize(@NonNull QuerydslBindings bindings, @NonNull EntityPath<?> root) {
    bindings.excludeUnlistedProperties(true);
    doBind(bindings, root, new ArrayList<>());
  }

  interface NameStrategy {
    String getName(List<String> traces);
  }

  public static class CamelCaseNameStrategy implements NameStrategy {
    @Override
    public String getName(List<String> traces) {

      StringBuilder sb = new StringBuilder(traces.get(0));

      for (int i = 1; i < traces.size(); i++) {
        sb.append(StringUtils.capitalize(traces.get(i)));
      }

      return sb.toString();
    }
  }
}
