package io.github.lgtm.springframework.jpa.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public interface ProxyArgumentResolver {
  default Optional<Object> resolveArgument(
      Method method, Parameter parameter, Object[] provideArgs) {
    for (Object provideArg : provideArgs) {
      if (parameter.getType().isInstance(provideArg)) {
        return Optional.of(provideArg);
      }
    }
    return Optional.empty();
  }
}
