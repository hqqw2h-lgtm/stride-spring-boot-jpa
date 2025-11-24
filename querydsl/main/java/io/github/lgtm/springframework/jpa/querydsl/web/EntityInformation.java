package io.github.lgtm.springframework.jpa.querydsl.web;

import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public record EntityInformation(Class<?> entityClass, EntityPathBase<?> entityPath) {
}
