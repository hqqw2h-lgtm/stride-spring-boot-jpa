package io.github.lgtm.springframework.jpa.querydsl.web.invoker;

import com.querydsl.core.types.Predicate;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface ListInvoker extends Invoker {
  default Collection<?> list(
      Predicate predicate,
      MultiValueMap<String, String> valueMap,
      HttpHeaders headers,
      HttpServletRequest request,
      EntityInformation entityInformation) {
    throw new UnsupportedOperationException("List operation is not supported.");
  }
}
