package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import java.util.Optional;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class RequestMappingInfoFilterProvider implements RequestMappingInfoProvider {
  @Override
  public Optional<RequestMappingInfoWrapper> getRequestMappingInfoWrapper(
      Action action, RestControllerEntity controller, EntityInformation entityInformation) {
    for (Action excludeAction : controller.exclude()) {
      if (excludeAction == action) {
        return Optional.of(RequestMappingInfoProvider.OBJECT);
      }
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }
}
