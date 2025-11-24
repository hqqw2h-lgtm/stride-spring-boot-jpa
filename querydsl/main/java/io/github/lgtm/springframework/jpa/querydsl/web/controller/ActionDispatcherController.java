package io.github.lgtm.springframework.jpa.querydsl.web.controller;


import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformationAware;
import io.github.lgtm.springframework.jpa.querydsl.web.invoker.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@SuppressWarnings("unchecked")
@Transactional
public class ActionDispatcherController<ENTITY, ID>
    implements EntityInformationAware, EntityControllerTemplate<ENTITY, ID> {
  private final InvokerHandlerFactory factory;
  private final EntityInformation entityInformation;

  public ActionDispatcherController(
      InvokerHandlerFactory factory,
      Class<ENTITY> entityClass,
      Class<ID> idClass,
      EntityPathBase<ENTITY> entityPath) {
    this.factory = factory;
    this.entityInformation = new EntityInformation(entityClass, entityPath);
  }

  public ActionDispatcherController(
      InvokerHandlerFactory factory, EntityInformation entityInformation) {
    this.factory = factory;
    this.entityInformation = entityInformation;
  }

  @Override
  public ENTITY create(ENTITY entity) {
    return (ENTITY)
        factory
            .getHandlerMethod(Action.CREATE, getEntityInformation())
            .map(invoker -> (CreateInvoker) invoker)
            .orElseThrow()
            .create(entity, getEntityInformation());
  }

  @Override
  public ENTITY update(ENTITY entity) {
    return (ENTITY)
        factory
            .getHandlerMethod(Action.UPDATE, getEntityInformation())
            .map(invoker -> (UpdateInvoker) invoker)
            .orElseThrow()
            .update(entity, getEntityInformation());
  }

  @Override
  public void delete(ID id) {
    factory
        .getHandlerMethod(Action.DELETE, getEntityInformation())
        .map(invoker -> (DeleteInvoker) invoker)
        .orElseThrow()
        .delete(id, getEntityInformation());
  }

  @Override
  @Transactional(readOnly = true)
  public ENTITY findById(ID id) {
    return (ENTITY)
        factory
            .getHandlerMethod(Action.FIND_BY_ID, getEntityInformation())
            .map(invoker -> (FindByIdInvoker) invoker)
            .orElseThrow()
            .findById(id, getEntityInformation());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<? extends ENTITY> pageList(
      Pageable pageable,
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request) {
    return (Page<? extends ENTITY>)
        factory
            .getHandlerMethod(Action.PAGE_LIST, getEntityInformation())
            .map(invoker -> (PageListInvoker) invoker)
            .orElseThrow()
            .pageList(pageable, predicate, valueMap, headers, request, getEntityInformation());
  }

  @Override
  @Transactional(readOnly = true)
  public Collection<ENTITY> list(
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request) {
    return (Collection<ENTITY>)
        factory
            .getHandlerMethod(Action.LIST, getEntityInformation())
            .map(invoker -> (ListInvoker) invoker)
            .orElseThrow()
            .list(predicate, valueMap, headers, request, getEntityInformation());
  }

  @Override
  public EntityInformation getEntityInformation() {
    return entityInformation;
  }
}
