package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface PageListInvoker extends Invoker {
  default Page<?> pageList(
      Pageable pageable,
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request,
      EntityInformation entityInformation) {
    throw new UnsupportedOperationException("Page list operation is not supported.");
  }
}
