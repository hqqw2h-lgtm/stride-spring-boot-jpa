package io.github.lgtm.springframework.jpa.helper;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.util.Pair;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class SpringFactoryContainer {
  private final ConfigurableListableBeanFactory beanFactory;

  public SpringFactoryContainer(ConfigurableListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public Pair<String, Object> applySingleton(Class<?> target) {
    String name = target.getName();
    var controller =
        this.beanFactory.autowire(target, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);

    this.beanFactory.autowireBeanProperties(
        controller, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
    controller = this.beanFactory.initializeBean(controller, name);
    beanFactory.registerSingleton(name, controller);

    return Pair.of(name, controller);
  }
}
