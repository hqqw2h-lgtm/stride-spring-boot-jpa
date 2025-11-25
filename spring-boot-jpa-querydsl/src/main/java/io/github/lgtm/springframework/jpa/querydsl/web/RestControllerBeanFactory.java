package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface RestControllerBeanFactory {
  String create(RestControllerEntity entity, EntityInformation entityInformation) throws Exception;
}
