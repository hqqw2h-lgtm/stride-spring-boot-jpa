package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface ControllerBeanDefinitionFactory {
    BeanDefinitionBuilder controllerDefinition(
            EntityInformation entityInformation,
            ClassLoader classLoader,
            RestControllerEntity restControllerEntity,
            RestEntityControllerContext context)
            throws Exception;
}
