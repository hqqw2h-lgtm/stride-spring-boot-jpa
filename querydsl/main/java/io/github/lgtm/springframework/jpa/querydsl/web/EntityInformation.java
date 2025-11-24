package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class EntityInformation  {
    private final Class<?> entityClass;
    private final EntityPathBase<?> entityPath;

    public EntityInformation(Class<?> entityClass, EntityPathBase<?> entityPath) {
        this.entityClass = entityClass;
        this.entityPath = entityPath;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public EntityPathBase<?> getEntityPath() {
        return entityPath;
    }
}
