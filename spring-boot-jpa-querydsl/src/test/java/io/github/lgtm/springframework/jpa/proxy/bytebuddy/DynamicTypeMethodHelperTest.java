package io.github.lgtm.springframework.jpa.proxy.bytebuddy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.mockito.Mockito.verify;

public class DynamicTypeMethodHelperTest {

  private ByteBuddy byteBuddy;
  private DynamicType.Builder<SourceInterface> builder;

  @BeforeEach
  void setUp() {
    byteBuddy = new ByteBuddy();
    builder = byteBuddy.subclass(SourceInterface.class);
  }

  /** 测试 delegateTo 完整流程： 1. 动态生成类。 2. 实例化委托目标（Mocked）。 3. 验证调用是否被转发。 */
  @Test
  void testDelegateTo_withParameters() throws Exception {
    Method sourceMethod = SourceInterface.class.getMethod("process", String.class);

    DelegationTarget targetMock = Mockito.mock(DelegationTarget.class);
    String testInput = "hello";
    String expectedResult = "Processed: " + testInput;

    Mockito.when(targetMock.process(testInput)).thenReturn(expectedResult);

    DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<SourceInterface> receiver =
        DynamicTypeMethodHelper.delegateTo(sourceMethod, builder, targetMock);

    Class<? extends SourceInterface> dynamicType =
        receiver.make().load(getClass().getClassLoader()).getLoaded();

    // 5. 实例化动态类
    SourceInterface instance = dynamicType.getDeclaredConstructor().newInstance();

    // 6. 执行方法
    String actualResult = instance.process(testInput);

    // 7. 断言结果和委托行为
    assertEquals(
        expectedResult,
        actualResult,
        "Delegate method should return the expected processed value.");
    verify(targetMock).process(testInput);
  }

  /** 测试 delegateTo 针对无参数方法。 */
  @Test
  void testDelegateTo_noParameters() throws Exception {
    Method sourceMethod = SourceInterface.class.getMethod("noArgs");

    DelegationTarget targetMock = Mockito.mock(DelegationTarget.class);

    DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<SourceInterface> receiver =
        DynamicTypeMethodHelper.delegateTo(sourceMethod, builder, targetMock);

    Class<? extends SourceInterface> dynamicType =
        receiver.make().load(getClass().getClassLoader()).getLoaded();

    SourceInterface instance = dynamicType.getDeclaredConstructor().newInstance();

    // 执行方法
    instance.noArgs();

    // 断言委托行为
    verify(targetMock).noArgs(); // 验证无参数方法被调用
  }

  @Test
  void testCopyParameters_retainsAnnotations() throws Exception {
    Method sourceMethod = SourceInterface.class.getMethod("process", String.class);

    DynamicType.Builder.MethodDefinition.ImplementationDefinition<SourceInterface> implementation =
        DynamicTypeMethodHelper.cloneMethod(sourceMethod, builder);

    Class<? extends SourceInterface> dynamicType =
        implementation
            .intercept(
                MethodDelegation.to(
                    new Object() {
                      @SuppressWarnings("unused")
                      public String process(String input) {
                        return "";
                      }
                    }))
            .make()
            .load(getClass().getClassLoader())
            .getLoaded();

    // 4. 获取动态类中对应的方法
    Method clonedMethod = dynamicType.getMethod("process", String.class);

    // 5. 断言注解存在
    // 检查参数上的注解是否被复制
    assertTrue(
        clonedMethod.getParameters()[0].isAnnotationPresent(TestAnnotation.class),
        "Cloned method parameter should retain the @TestAnnotation.");
  }

  @Test
  void testCopyMethodDefinition_throwsOnNonPublic() throws Exception {
    class PrivateMethodHolder {
      private void secretMethod() {}
    }

    Method privateMethod = PrivateMethodHolder.class.getDeclaredMethod("secretMethod");
    privateMethod.setAccessible(true);

    assertThrows(
        IllegalArgumentException.class,
        () -> DynamicTypeMethodHelper.cloneMethod(privateMethod, builder),
        "Should throw exception when cloning a non-public method.");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {}

  interface SourceInterface {
    String process(@TestAnnotation String input);

    void noArgs();
  }

  class Assert {
    public static void isTrue(boolean condition, String message) {
      if (!condition) {
        throw new IllegalArgumentException(message);
      }
    }
  }

  class DelegationTarget {
    public String process(String input) {
      return "Processed: " + input;
    }

    public void noArgs() {}
  }
}
