package io.github.lgtm.springframework.jpa.querydsl.web.controller;

import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface ProxyController extends BeanFactoryAware {
  ProxyMetaInfo getProxyMetaInfo();
}
