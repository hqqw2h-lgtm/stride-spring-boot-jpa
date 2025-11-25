package io.github.lgtm.springframework.jpa.querydsl.web;

import io.github.lgtm.springframework.jpa.querydsl.web.configuration.RestEntityConfiguration;
import java.lang.annotation.*;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RestEntityConfiguration.class)
public @interface EnableAutoEntityController {}
