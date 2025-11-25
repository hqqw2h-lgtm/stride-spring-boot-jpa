package io.github.lgtm.springframework.jpa.querydsl;

import com.querydsl.core.types.dsl.EntityPathBase;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jakarta.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class ClassPathScanningEntityIterator
    implements EntityPathBaseFinder, BeanClassLoaderAware, InitializingBean {
  private final Map<Class<?>, Class<EntityPathBase<?>>> entityPathBases = new HashMap<>();
  private final String[] packages;
  private ClassLoader classLoader;

  public ClassPathScanningEntityIterator(String[] packages) {
    this.packages = packages;
  }

  @SuppressWarnings("unchecked")
  public void doScan() {
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

  protected EntityPathBase<?> getValue(List<Field> candidates, Class<?> entityClass)
      throws IllegalAccessException {
    Assert.isTrue(
        candidates.size() == 1,
        "Cannot determinate the entity path instance for " + entityClass.getName());
    Field field = candidates.get(0);
    return (EntityPathBase<?>) field.get(null);
  }

  @Override
  public Optional<EntityPathBase<?>> find(EntityType<?> entityType) throws Exception {
    List<Field> candidates = new ArrayList<>(1);
    Class<EntityPathBase<?>> entityPathBaseClass = entityPathBases.get(entityType.getJavaType());

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
        "Cannot determinate the entity path instance for " + entityType.getJavaType().getName());

    return Optional.of(getValue(candidates, entityType.getJavaType()));
  }

  @Override
  public void setBeanClassLoader(@NonNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    doScan();
  }
}
