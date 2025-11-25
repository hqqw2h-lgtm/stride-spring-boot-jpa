package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;
import jakarta.persistence.metamodel.*;

import java.util.*;

import lombok.Getter;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

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
      if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
        EmbeddableType<?> nested = (EmbeddableType<?>) ((SingularAttribute<?, ?>) attr).getType();
        paths.addAll(getEmbeddablePaths(nested, parent + "." + attr.getName()));
      } else {
        paths.add(parent + "." + attr.getName());
      }
    }
    return paths;
  }

  protected List<String> getSingleIdColumnName() {
    if (entityPath.getIdType() instanceof EmbeddableType<?>) {
      /*  return getEmbeddablePaths(
      (EmbeddableType<?>) entityPath.getIdType(),
      entityPath.getId(entityPath.getIdType().getJavaType()).getName());*/
      throw new UnsupportedOperationException("Embeddable id is not supported yet.");
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
  public String[] getIdColumnNames() {
    List<String> idColumnNames = new java.util.ArrayList<>();

    if (entityPath.hasSingleIdAttribute()) {

      idColumnNames.addAll(getSingleIdColumnName());

    } else {
      Set<SingularAttribute<?, ?>> singularAttributes =
          getIdClassAttributes((IdentifiableType) entityPath.getIdType());
      singularAttributes.stream().map(Attribute::getName).forEach(idColumnNames::add);
    }

    return idColumnNames.toArray(new String[0]);
  }

  protected Object bindIdValueWithAccessor(Map<String, String> ids) {
    Class<?> idClass = entityPath.getIdType().getJavaType();
    Binder binder = new Binder(new MapConfigurationPropertySource(ids));
    BindResult<?> result = binder.bind("", Bindable.of(idClass));
    return result.orElseThrow(
        () -> new IllegalArgumentException("Cannot bind id values to " + idClass.getName()));
  }
}
