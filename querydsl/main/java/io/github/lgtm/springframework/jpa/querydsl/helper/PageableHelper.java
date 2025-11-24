package io.github.lgtm.springframework.jpa.querydsl.helper;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BeanPath;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.domain.Pageable;

/**
 * Utility class for handling pageable and sorting operations, particularly for converting Pageable
 * sorting requests to OrderSpecifier instances, which are used in Querydsl queries.
 *
 * <p>This class helps with constructing the list of sorting conditions (OrderSpecifier) based on
 * the Pageable object's sort information. It also allows adding a default sorting order when
 * necessary.
 *
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
public class PageableHelper {
  // Private constructor to prevent instantiation
  private PageableHelper() {}

  /**
   * Converts the Pageable object's sort into an array of OrderSpecifiers, ensuring that a default
   * sorting order (if provided) is included if it is not already present.
   *
   * @param pageable the Pageable object containing sort information
   * @param beanPath the BeanPath to the entity class used for constructing order expressions
   * @param dft the default OrderSpecifier to be included if it is not already part of the sorting
   *     order
   * @return an array of OrderSpecifier objects, including the default if necessary
   */
  public static OrderSpecifier<?>[] orders(
      final Pageable pageable, final BeanPath<?> beanPath, final OrderSpecifier<?> dft) {
    // Get the list of OrderSpecifiers based on the Pageable's sort
    final List<OrderSpecifier<?>> orderSpecifierList = orders(pageable, beanPath);
    // If the default OrderSpecifier is not already in the list, add it
    if (orderSpecifierList.stream().noneMatch(p -> dft.getTarget() == p.getTarget())) {
      orderSpecifierList.add(dft);
    }
    // Convert the list to an array and return it
    return orderSpecifierList.toArray(OrderSpecifier<?>[]::new);
  }

  /**
   * Converts the Pageable object's sort into a list of OrderSpecifiers based on the fields of the
   * given beanPath. This method processes each sorting order from the Pageable and creates a
   * corresponding OrderSpecifier.
   *
   * @param pageable the Pageable object containing sort information
   * @param beanPath the BeanPath to the entity class used for constructing order expressions
   * @return a list of OrderSpecifier objects representing the sorting order
   */
  @SuppressWarnings("unchecked")
  public static List<OrderSpecifier<?>> orders(
      final Pageable pageable, final BeanPath<?> beanPath) {
    // List to store the resulting OrderSpecifiers
    final List<OrderSpecifier<?>> orderSpecifierList = new ArrayList<>();
    // Iterate through each sort order in the Pageable's sort
    pageable
        .getSort()
        .forEach(
            order -> {
              final var accessor = PropertyAccessorFactory.forDirectFieldAccess(beanPath);
              final Class<?> tp = accessor.getPropertyType(order.getProperty());
              if (tp == null) {
                return;
              }

              final Object value = accessor.getPropertyValue(order.getProperty());
              if (value == null) {
                return;
              }
              // Check if the value can be treated as a Comparable expression
              try {
                orderSpecifierList.add(
                    new OrderSpecifier<>(
                        order.getDirection().isAscending() ? Order.ASC : Order.DESC,
                        (Expression<? extends Comparable<?>>) value));
              } catch (final Exception exception) {

              }
            });
    return orderSpecifierList;
  }
}
