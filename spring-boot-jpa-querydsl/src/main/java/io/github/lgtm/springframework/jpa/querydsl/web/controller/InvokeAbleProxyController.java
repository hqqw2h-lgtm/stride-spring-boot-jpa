package io.github.lgtm.springframework.jpa.querydsl.web.controller;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public abstract class InvokeAbleProxyController implements ProxyController, InitializingBean {
  public static final MethodHandles.Lookup INVOKE_METHOD_LOOKUP = MethodHandles.lookup();
  protected BeanFactory beanFactory;
  private MethodHandle methodHandle;

  public Object invoke(Object... args) throws Throwable {
    Object bean = beanFactory.getBean(getProxyMetaInfo().getTargetBeanName());
    return methodHandle.bindTo(bean).asSpreader(Object[].class, args.length).invoke(args);
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Object bean = beanFactory.getBean(getProxyMetaInfo().getTargetBeanName());
    Class<?> target = ClassUtils.getUserClass(bean);
    methodHandle =
        INVOKE_METHOD_LOOKUP.findVirtual(
            target,
            getProxyMetaInfo().getTargetMethod().getName(),
            MethodType.methodType(
                getProxyMetaInfo().getTargetMethod().getReturnType(),
                getProxyMetaInfo().getTargetMethod().getParameterTypes()));
  }
}
