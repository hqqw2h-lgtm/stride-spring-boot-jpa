package io.github.lgtm.springframework.jpa.querydsl.web.controller;

import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface EntityControllerTemplate<ENTITY, ID> {

  default ENTITY create(ENTITY entity) {
    throw new UnsupportedOperationException("Create operation is not supported.");
  }

  default ENTITY update(ENTITY entity) {
    throw new UnsupportedOperationException("Update operation is not supported.");
  }

  default void delete(ID id) {
    throw new UnsupportedOperationException("Delete operation is not supported.");
  }

  default ENTITY findById(ID id) {
    throw new UnsupportedOperationException("FindById operation is not supported.");
  }

  default Page<? extends ENTITY> pageList(
      Pageable pageable,
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request) {
    throw new UnsupportedOperationException("PageList operation is not supported.");
  }

  default Collection<ENTITY> list(
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request) {
    throw new UnsupportedOperationException("List operation is not supported.");
  }
}
