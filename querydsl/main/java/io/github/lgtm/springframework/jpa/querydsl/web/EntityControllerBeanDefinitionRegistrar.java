package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

import io.github.lgtm.springframework.jpa.querydsl.web.annotation.RestControllerEntity;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class EntityControllerBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @SuppressWarnings("unchecked")
    protected EntityIterator entityBaseFinder(
            MultiValueMap<String, Object> valueMap,
            @NonNull AnnotationMetadata importingClassMetadata,
            @NonNull BeanDefinitionRegistry registry,
            @NonNull BeanNameGenerator importBeanNameGenerator)
            throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        Class<? extends EntityIterator> finder =
                (Class<? extends EntityIterator>) valueMap.getFirst("finder");
        return Objects.requireNonNull(finder).getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public void registerBeanDefinitions(
            @NonNull AnnotationMetadata importingClassMetadata,
            @NonNull BeanDefinitionRegistry registry,
            @NonNull BeanNameGenerator importBeanNameGenerator) {
        MultiValueMap<String, Object> valueMap =
                importingClassMetadata.getAllAnnotationAttributes(
                        EnableAutoEntityController.class.getName());

        EntityIterator finder =
                entityBaseFinder(
                        Objects.requireNonNull(valueMap),
                        importingClassMetadata,
                        registry,
                        importBeanNameGenerator);

        String[] packages = (String[]) Objects.requireNonNull(valueMap.getFirst("basePackages"));

        finder.doScan(packages, registry.getClass().getClassLoader());

        Set<Class<?>> entityClasses = finder.getEntityClasses();
        for (Class<?> entityClass : entityClasses) {
            finder
                    .find(entityClass)
                    .ifPresent(
                            (Consumer<EntityPathBase<?>>)
                                    entityPathBase ->
                                            registryControllerBeanDefinition(
                                                    valueMap,
                                                    registry,
                                                    entityClass,
                                                    entityPathBase,
                                                    importBeanNameGenerator));
        }
    }

    protected void registryControllerBeanDefinition(
            MultiValueMap<String, Object> valueMap,
            BeanDefinitionRegistry registry,
            Class<?> entityClass,
            EntityPathBase<?> entityPathBase,
            BeanNameGenerator importBeanNameGenerator) {

        resolveRestControllerEntity(entityClass)
                .ifPresent(
                        restControllerEntity -> {
                            RestEntityControllerContext context =
                                    createRestEntityControllerContext(
                                            valueMap, restControllerEntity, registry, importBeanNameGenerator);
                            try {
                                ControllerBeanDefinitionFactory factory =
                                        determinateControllerBeanDefinitionFactory(restControllerEntity, context);
                                BeanDefinitionBuilder definitionBuilder =
                                        factory.controllerDefinition(
                                                new EntityInformation(entityClass, entityPathBase),
                                                importBeanNameGenerator.getClass().getClassLoader(),
                                                restControllerEntity,
                                                context);
                                BeanDefinition controllerDefinition = definitionBuilder.getBeanDefinition();
                                registry.registerBeanDefinition(
                                        importBeanNameGenerator.generateBeanName(controllerDefinition, registry),
                                        controllerDefinition);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    protected RestEntityControllerContext createRestEntityControllerContext(
            MultiValueMap<String, Object> valueMap,
            RestControllerEntity restControllerEntity,
            @NonNull BeanDefinitionRegistry registry,
            @NonNull BeanNameGenerator importBeanNameGenerator) {
        return new RestEntityControllerContext() {
            @SuppressWarnings("unchecked")
            @Override
            public Class<? extends ControllerBeanDefinitionFactory>
            getDefaultControllerBeanDefinitionFactory() {
                return (Class<? extends ControllerBeanDefinitionFactory>)
                        valueMap.getFirst("controllerFactory");
            }

            @Override
            public Map<String, Object> getAttribute() {
                return new HashMap<>();
            }

            @Override
            public BeanDefinitionRegistry getRegistry() {
                return registry;
            }

            @Override
            public BeanNameGenerator getBeanNameGenerator() {
                return importBeanNameGenerator;
            }
        };
    }

    protected Optional<RestControllerEntity> resolveRestControllerEntity(Class<?> entityClass) {
        RestControllerEntity restControllerEntity =
                entityClass.getAnnotation(RestControllerEntity.class);
        return Optional.ofNullable(restControllerEntity);
    }

    protected ControllerBeanDefinitionFactory determinateControllerBeanDefinitionFactory(
            RestControllerEntity restControllerEntity, RestEntityControllerContext context)
            throws Exception {
        Class<? extends ControllerBeanDefinitionFactory> factoryClass =
                restControllerEntity.controllerFactory();
        if (factoryClass == null) {
            factoryClass = context.getDefaultControllerBeanDefinitionFactory();
        }
        Assert.notNull(factoryClass, "ControllerBeanDefinitionFactory class must not be null");
        return factoryClass.getConstructor().newInstance();
    }
}
