package io.github.lgtm.springframework.jpa.querydsl.web.controller;

import io.github.lgtm.springframework.jpa.querydsl.web.Action;
import io.github.lgtm.springframework.jpa.querydsl.web.EntityInformation;
import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Getter
@Setter
public class ProxyMetaInfo {

  private Action action;

  private RestControllerEntity entity;

  private EntityInformation entityInformation;

  private String targetBeanName;

  private Method targetMethod;
}
