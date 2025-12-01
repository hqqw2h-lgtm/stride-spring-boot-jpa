package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.Fetchable;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQueryFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.EntityArgument;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.IdArgument;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.InvokerDelegate;
import io.github.lgtm.springframework.jpa.querydsl.web.helper.PageableHelper;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Transactional
public class DefaultDelegator implements BeanFactoryAware {

  private BeanFactory beanFactory;

  @Transactional()
  @InvokerDelegate(action = Action.CREATE, entity = InvokerDelegate._DEFAULT.class)
  public Object create(@EntityArgument Object entity, EntityInformation entityInformation) {
    getEntityManager().persist(entity);
    return entity;
  }

  @InvokerDelegate(action = Action.UPDATE, entity = InvokerDelegate._DEFAULT.class)
  public Object update(@EntityArgument Object entity, EntityInformation entityInformation) {
    getEntityManager().persist(entity);
    return entity;
  }

  protected EntityManager getEntityManager() {
    return beanFactory.getBean(EntityManager.class);
  }

  @InvokerDelegate(action = Action.DELETE, entity = InvokerDelegate._DEFAULT.class)
  public void delete(@IdArgument Object id, EntityInformation entityInformation) {
    EntityManager entityManager = getEntityManager();
    Object entity = entityManager.find(entityInformation.getEntityPath().getJavaType(), id);
    if (entity == null) {
      return;
    }
    entityManager.remove(entity);
  }

  @Transactional(readOnly = true)
  @InvokerDelegate(action = Action.FIND_BY_ID, entity = InvokerDelegate._DEFAULT.class)
  public Object findById(@IdArgument Object id, EntityInformation entityInformation) {
    return getEntityManager().find(entityInformation.getEntityPath().getJavaType(), id);
  }

  protected JPQLQueryFactory getQueryFactory() {
    return beanFactory.getBean(JPQLQueryFactory.class);
  }

  @Transactional(readOnly = true)
  @InvokerDelegate(action = Action.PAGE_LIST, entity = InvokerDelegate._DEFAULT.class)
  public Page<?> pageList(
      Pageable pageable, Predicate predicate, EntityInformation entityInformation) {
    JPQLQueryFactory factory = getQueryFactory();
    Fetchable<?> fetchable =
        factory.selectFrom(entityInformation.getEntityClass()).where(predicate);
    long count = fetchable.fetchCount();
    List<?> content = List.of();
    if (count != 0) {

      content =
          factory
              .selectFrom(entityInformation.getEntityClass())
              .where(predicate)
              .orderBy(
                  PageableHelper.orders(pageable, entityInformation.getEntityClass())
                      .toArray(OrderSpecifier<?>[]::new))
              .limit(pageable.getPageSize())
              .offset(pageable.getOffset())
              .fetch();
    }

    return new PageImpl<>(content, pageable, count);
  }

  @Transactional(readOnly = true)
  @InvokerDelegate(action = Action.LIST, entity = InvokerDelegate._DEFAULT.class)
  public Collection<?> list(Predicate predicate, EntityInformation entityInformation) {
    JPQLQueryFactory factory = getQueryFactory();
    return factory.selectFrom(entityInformation.getEntityClass()).where(predicate).fetch();
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
