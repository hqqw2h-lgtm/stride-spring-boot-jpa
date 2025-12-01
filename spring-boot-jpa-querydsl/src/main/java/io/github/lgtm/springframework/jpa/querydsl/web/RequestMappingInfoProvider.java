package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.Getter;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface RequestMappingInfoProvider extends Ordered {

  RequestMappingInfoWrapper OBJECT = new RequestMappingInfoWrapper(null, null, null);

  Optional<RequestMappingInfoWrapper> getRequestMappingInfoWrapper(
      Action action, RestControllerEntity controller, EntityInformation entityInformation);

  @Getter
  class RequestMappingInfoWrapper {
    private final String beanName;
    private final Method method;
    private final RequestMappingInfo mapping;

    public RequestMappingInfoWrapper(String beanName, Method method, RequestMappingInfo mapping) {
      this.beanName = beanName;
      this.method = method;
      this.mapping = mapping;
    }
  }
}
