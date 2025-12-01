package io.github.lgtm.springframework.jpa.querydsl.web.mapping;

import io.github.lgtm.springframework.jpa.querydsl.web.ActionMapping;

import java.util.Optional;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface MappingProvider {
  Optional<ActionMapping.TargetHandler> getTarget(ActionMapping.MappingKey mappingKey);
}
