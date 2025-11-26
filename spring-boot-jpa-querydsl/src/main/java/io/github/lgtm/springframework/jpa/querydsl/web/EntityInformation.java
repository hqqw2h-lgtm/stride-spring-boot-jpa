package io.github.lgtm.springframework.jpa.querydsl.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.EntityPathBase;
import jakarta.persistence.IdClass;
import jakarta.persistence.metamodel.*;

import java.util.*;

import lombok.Getter;
import org.springframework.beans.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Getter
public class EntityInformation {
  private final EntityPathBase<?> entityClass;
  private final EntityType<?> entityPath;

  public EntityInformation(EntityPathBase<?> entityClass, EntityType<?> entityPath) {
    this.entityClass = entityClass;
    this.entityPath = entityPath;
  }

  private List<String> getEmbeddablePaths(EmbeddableType<?> embeddable, String parent) {
    List<String> paths = new ArrayList<>();
    for (Attribute<?, ?> attr : embeddable.getAttributes()) {
      String root = StringUtils.hasText(parent) ? parent + "." + attr.getName() : attr.getName();
      if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
        EmbeddableType<?> nested = (EmbeddableType<?>) ((SingularAttribute<?, ?>) attr).getType();
        paths.addAll(getEmbeddablePaths(nested, root));
      } else {
        paths.add(root);
      }
    }
    return paths;
  }

  protected List<String> getSingleIdColumnName() {
    if (entityPath.getIdType() instanceof EmbeddableType<?>) {
      return getEmbeddablePaths(
          (EmbeddableType<?>) entityPath.getIdType(),
          entityPath.getId(entityPath.getIdType().getJavaType()).getName());
    } else {
      SingularAttribute<?, ?> idAttr = entityPath.getId(entityPath.getIdType().getJavaType());
      return List.of(idAttr.getName());
    }
  }

  public <T> Set<SingularAttribute<? super T, ?>> getIdClassAttributes(IdentifiableType<T> type) {
    try {
      return type.getIdClassAttributes();
    } catch (IllegalArgumentException e) {
      return Collections.emptySet();
    }
  }

  @SuppressWarnings("unchecked,rawtypes")
  public List<String> getIdColumnNames() {
    List<String> idColumnNames = new java.util.ArrayList<>();

    if (entityPath.hasSingleIdAttribute()) {
      if (entityPath.getIdType().getPersistenceType() == Type.PersistenceType.BASIC) {
        idColumnNames.add(entityPath.getId(entityPath.getIdType().getJavaType()).getName());
      } else if (entityPath.getIdType().getPersistenceType() == Type.PersistenceType.EMBEDDABLE) {
        // embedded id
        idColumnNames.addAll(getEmbeddablePaths((EmbeddableType<?>) entityPath.getIdType(), ""));
      }
    } else {
      Set<SingularAttribute<?, ?>> singularAttributes =
          (Set<SingularAttribute<?, ?>>) entityPath.getIdClassAttributes();
      singularAttributes.forEach(
          singularAttribute -> {
            if (singularAttribute.isId()) {
              idColumnNames.add(singularAttribute.getName());
            }
          });
    }
    return idColumnNames.stream().sorted(Comparator.naturalOrder()).toList();
  }

  protected Object bindIdValueWithAccessor(Map<String, String> ids, BeanFactory beanFactory) {

    Class<?> idClass = getIdClass();
    if (entityPath.hasSingleIdAttribute()
        && entityPath.getIdType().getPersistenceType() == Type.PersistenceType.BASIC) {
      Assert.isTrue(ids.size() == 1, "Cannot bind id values to " + idClass.getName());
      String raw = ids.values().iterator().next();
      ConversionService cs = beanFactory.getBean(ConversionService.class);
      return cs.convert(raw, idClass);
    } else {
      // @IdClass  @EmbeddedId
      Object target = BeanUtils.instantiateClass(idClass);
      DataBinder binder = new DataBinder(target);
      binder.setAutoGrowNestedPaths(true);
      binder.setIgnoreUnknownFields(false);
      binder.setConversionService(beanFactory.getBean(ConversionService.class));
      binder.bind(new MutablePropertyValues(ids));
      if (binder.getBindingResult().hasErrors()) {
        throw new IllegalArgumentException("Cannot bind id values to " + idClass.getName());
      }
      return target;
    }
  }

  public Class<?> getIdClass() {
    if (entityPath.hasSingleIdAttribute()) {
      return entityPath.getIdType().getJavaType();
    } else {
      Class<?> target = entityPath.getJavaType();
      while (target != null && target != Object.class) {
        IdClass idClassAnnotation = target.getAnnotation(IdClass.class);
        if (idClassAnnotation != null) {
          return idClassAnnotation.value();
        }
        target = target.getSuperclass();
      }
    }
    throw new IllegalStateException(
        "Cannot determinate the id class for entity " + entityPath.getJavaType().getName());
  }
}
