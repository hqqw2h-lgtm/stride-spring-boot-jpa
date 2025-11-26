package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.Fetchable;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQueryFactory;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import io.github.lgtm.springframework.jpa.querydsl.web.helper.PageableHelper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Transactional
public class CrudInterceptor
    implements BeanFactoryAware,
        CreateInvoker,
        ListInvoker,
        DeleteInvoker,
        FindByIdInvoker,
        PageListInvoker,
        UpdateInvoker {

  private BeanFactory beanFactory;

  @Override
  @Transactional()
  public Object create(Object entity, EntityInformation entityInformation) {
    getEntityManager().persist(entity);
    return entity;
  }

  @Override
  public Object update(Object entity, EntityInformation entityInformation) {
    getEntityManager().persist(entity);
    return entity;
  }

  protected EntityManager getEntityManager() {
    return beanFactory.getBean(EntityManager.class);
  }

  @Override
  public void delete(Object id, EntityInformation entityInformation) {
    EntityManager entityManager = getEntityManager();
    Object entity = entityManager.find(entityInformation.getEntityPath().getJavaType(), id);
    if (entity == null) {
      return;
    }
    entityManager.remove(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public Object findById(Object id, EntityInformation entityInformation) {
    return getEntityManager().find(entityInformation.getEntityPath().getJavaType(), id);
  }

  protected JPQLQueryFactory getQueryFactory() {
    return beanFactory.getBean(JPQLQueryFactory.class);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<?> pageList(
      Pageable pageable,
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request,
      EntityInformation entityInformation) {
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

  @Override
  @Transactional(readOnly = true)
  public Collection<?> list(
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request,
      EntityInformation entityInformation) {
    JPQLQueryFactory factory = getQueryFactory();
    return factory.selectFrom(entityInformation.getEntityClass()).where(predicate).fetch();
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
