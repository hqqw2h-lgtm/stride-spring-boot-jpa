package io.github.lgtm.springframework.jpa.querydsl.web.mapping;

import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.ActionMapping;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.*;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class InvokerMappingProvider implements MappingProvider {
  public final ConfigurableListableBeanFactory beanFactory;
  private final Map<Action, InvokerResolver> resolverMap =
      Map.of(
          Action.CREATE, new CreateInvokerResolver(),
          Action.DELETE, new DeleteInvokerResolver(),
          Action.LIST, new ListInvokerResolver(),
          Action.PAGE_LIST, new PageListInvokerResolver(),
          Action.FIND_BY_ID, new FindByIdInvokerResolver());

  public InvokerMappingProvider(ConfigurableListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public Optional<ActionMapping.TargetHandler> getTarget(ActionMapping.MappingKey mappingKey) {
    return Optional.ofNullable(resolverMap.get(mappingKey.getAction()))
        .flatMap(
            invokerResolver ->
                invokerResolver.resolve(mappingKey.getEntityInformation(), beanFactory));
  }

  public interface InvokerResolver {
    Optional<ActionMapping.TargetHandler> resolve(
        EntityInformation entityInformation, ConfigurableListableBeanFactory beanFactory);
  }

  public abstract static class AbstractInvokerResolver implements InvokerResolver {
    protected abstract ResolvableType getType(EntityInformation entityInformation);

    protected abstract Method getMethod();

    @Override
    public Optional<ActionMapping.TargetHandler> resolve(
        EntityInformation entityInformation, ConfigurableListableBeanFactory beanFactory) {
      ResolvableType resolvableType = getType(entityInformation);

      String[] beanNames = beanFactory.getBeanNamesForType(resolvableType);
      if (beanNames.length == 0) {
        return Optional.empty();
      }
      if (beanNames.length > 1) {
        throw new IllegalStateException(
            "Multiple invoker beans found for entity "
                + entityInformation.getEntityPath().getJavaType());
      }

      return Optional.of(new ActionMapping.TargetHandler(beanNames[0], getMethod()));
    }
  }

  public static class CreateInvokerResolver extends AbstractInvokerResolver {

    @Override
    protected ResolvableType getType(EntityInformation entityInformation) {
      return ResolvableType.forClassWithGenerics(
          CreateInvoker.class, entityInformation.getEntityPath().getJavaType());
    }

    @Override
    protected Method getMethod() {
      return ReflectionUtils.findMethod(CreateInvoker.class, "create", null);
    }
  }

  public static class DeleteInvokerResolver extends AbstractInvokerResolver {

    @Override
    protected ResolvableType getType(EntityInformation entityInformation) {
      return ResolvableType.forClassWithGenerics(
          DeleteInvoker.class,
          entityInformation.getEntityPath().getJavaType(),
          entityInformation.getIdClass());
    }

    @Override
    protected Method getMethod() {
      return ReflectionUtils.findMethod(DeleteInvoker.class, "delete", null);
    }
  }

  public static class ListInvokerResolver extends AbstractInvokerResolver {

    @Override
    protected ResolvableType getType(EntityInformation entityInformation) {
      return ResolvableType.forClassWithGenerics(
          ListInvoker.class, entityInformation.getEntityPath().getJavaType());
    }

    @Override
    protected Method getMethod() {
      return ReflectionUtils.findMethod(ListInvoker.class, "list", Predicate.class);
    }
  }

  public static class PageListInvokerResolver extends AbstractInvokerResolver {

    @Override
    protected ResolvableType getType(EntityInformation entityInformation) {
      return ResolvableType.forClassWithGenerics(
          PageListInvoker.class, entityInformation.getEntityPath().getJavaType());
    }

    @Override
    protected Method getMethod() {
      return ReflectionUtils.findMethod(
          PageListInvoker.class, "page", Pageable.class, Predicate.class);
    }
  }

  public static class FindByIdInvokerResolver extends AbstractInvokerResolver {

    @Override
    protected ResolvableType getType(EntityInformation entityInformation) {
      return ResolvableType.forClassWithGenerics(
          FindByIdInvoker.class,
          entityInformation.getEntityPath().getJavaType(),
          entityInformation.getIdClass());
    }

    @Override
    protected Method getMethod() {
      return ReflectionUtils.findMethod(FindByIdInvoker.class, "findById", null);
    }
  }
}
