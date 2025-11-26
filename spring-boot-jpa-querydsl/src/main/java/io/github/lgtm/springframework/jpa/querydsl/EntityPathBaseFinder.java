package io.github.lgtm.springframework.jpa.querydsl;

import com.querydsl.core.types.dsl.EntityPathBase;
import jakarta.persistence.metamodel.EntityType;
import java.util.Optional;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public interface EntityPathBaseFinder {
  Optional<EntityPathBase<?>> find(EntityType<?> entityType) throws Exception;
}
