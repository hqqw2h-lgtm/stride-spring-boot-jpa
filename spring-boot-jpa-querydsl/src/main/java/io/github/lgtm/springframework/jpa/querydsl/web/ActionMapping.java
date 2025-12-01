package io.github.lgtm.springframework.jpa.querydsl.web;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Getter
public class ActionMapping {

  private final MappingKey mappingKey;
  private final TargetHandler targetHandler;

  public ActionMapping(MappingKey mappingKey, TargetHandler targetHandler) {
    this.mappingKey = mappingKey;
    this.targetHandler = targetHandler;
  }

  @Getter
  public static class MappingKey {
    private final Action action;
    private final EntityInformation entityInformation;

    public MappingKey(Action action, EntityInformation entityInformation) {
      this.action = action;
      this.entityInformation = entityInformation;
    }

    @Override
    public boolean equals(Object object) {
      if (object == null || getClass() != object.getClass()) return false;
      MappingKey that = (MappingKey) object;
      return action == that.action && Objects.equals(entityInformation, that.entityInformation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(action, entityInformation);
    }
  }

  @Getter
  public static class TargetHandler {
    private final String beanName;
    private final Method method;

    public TargetHandler(String beanName, Method method) {
      this.beanName = beanName;
      this.method = method;
    }

    @Override
    public boolean equals(Object object) {
      if (object == null || getClass() != object.getClass()) return false;
      TargetHandler that = (TargetHandler) object;
      return Objects.equals(beanName, that.beanName) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
      return Objects.hash(beanName, method);
    }
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    ActionMapping that = (ActionMapping) object;
    return Objects.equals(mappingKey, that.mappingKey)
        && Objects.equals(targetHandler, that.targetHandler);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mappingKey, targetHandler);
  }
}
