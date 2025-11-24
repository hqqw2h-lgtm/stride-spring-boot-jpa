package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class ClassPathScanningEntityIterator implements EntityIterator {
  private final Map<Class<?>, Class<EntityPathBase<?>>> entityPathBases = new HashMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public void doScan(String[] packages, ClassLoader classLoader) {
    try (ScanResult scanResult =
        new ClassGraph()
            .addClassLoader(classLoader)
            .enableClassInfo()
            .acceptPackages(packages)
            .scan()) {
      scanResult
          .getAllClasses()
          .forEach(
              classInfo -> {
                Class<?> clz = classInfo.loadClass();
                if (EntityPathBase.class.isAssignableFrom(clz)) {
                  Class<?> entityClass =
                      ResolvableType.forClass(clz)
                          .as(EntityPathBase.class)
                          .getGeneric(0)
                          .getRawClass();
                  entityPathBases.put(entityClass, (Class<EntityPathBase<?>>) clz);
                }
              });
    }
  }

  @Override
  public Set<Class<?>> getEntityClasses() {
    return Collections.unmodifiableSet(entityPathBases.keySet());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<EntityPathBase<T>> find(Class<T> entityClass) throws IllegalAccessException {
    List<Field> candidates = new ArrayList<>(1);
    Class<EntityPathBase<?>> entityPathBaseClass = entityPathBases.get(entityClass);

    ReflectionUtils.doWithLocalFields(
        entityPathBaseClass,
        field -> {
          int mds = field.getModifiers();
          if (Modifier.isFinal(mds)
              && Modifier.isPublic(mds)
              && Modifier.isStatic(mds)
              && field.getType() == field.getDeclaringClass()) {
            candidates.add(field);
          }
        });
    Assert.isTrue(
        candidates.size() == 1,
        "Cannot determinate the entity path instance for " + entityClass.getName());

    return Optional.of((EntityPathBase<T>) getValue(candidates, entityClass));
  }

  protected EntityPathBase<?> getValue(List<Field> candidates, Class<?> entityClass)
      throws IllegalAccessException {
    Assert.isTrue(
        candidates.size() == 1,
        "Cannot determinate the entity path instance for " + entityClass.getName());
    Field field = candidates.get(0);
    return (EntityPathBase<?>) field.get(null);
  }
}
