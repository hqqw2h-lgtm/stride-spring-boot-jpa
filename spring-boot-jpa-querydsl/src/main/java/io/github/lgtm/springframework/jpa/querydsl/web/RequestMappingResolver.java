package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface RequestMappingResolver {

  void resolve(RestControllerEntity entity, EntityInformation entityInformation) throws Exception;
}
