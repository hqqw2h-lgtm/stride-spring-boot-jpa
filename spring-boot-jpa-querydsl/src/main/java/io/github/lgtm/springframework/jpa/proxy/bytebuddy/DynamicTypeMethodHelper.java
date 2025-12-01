package io.github.lgtm.springframework.jpa.proxy.bytebuddy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
public class DynamicTypeMethodHelper {
  /** copy原方法,并绑定到object上 */
  public static <T> DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<T> delegateTo(
      Method source, DynamicType.Builder<T> builder, Object object) {

    var md = cloneMethod(source, builder);

    return md.intercept(MethodDelegation.to(object));
  }

  public static <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition<T> cloneMethod(
      Method source, DynamicType.Builder<T> builder) {
    Assert.isTrue(
        Modifier.isPublic(source.getModifiers()), "Only public  methods can be cloned: " + source);
    var loadedMethod = new MethodDescription.ForLoadedMethod(source);
    return builder
        .defineMethod(
            loadedMethod.getName(), loadedMethod.getReturnType(), loadedMethod.getModifiers())
        .withParameters(loadedMethod.getParameters().asTypeList())
        .throwing(loadedMethod.getExceptionTypes());
  }
}
