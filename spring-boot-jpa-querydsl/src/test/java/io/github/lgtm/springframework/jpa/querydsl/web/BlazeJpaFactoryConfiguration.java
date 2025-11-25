package io.github.lgtm.springframework.jpa.querydsl.web;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@Configuration
public class BlazeJpaFactoryConfiguration {

  @Bean
  @ConditionalOnMissingBean(BlazeJPAQueryFactory.class)
  public BlazeJPAQueryFactory queryFactory(
      final EntityManager entityManager, final CriteriaBuilderFactory criteriaBuilderFactory) {
    return new BlazeJPAQueryFactory(entityManager, criteriaBuilderFactory);
  }

  @Bean
  @ConditionalOnMissingBean(CriteriaBuilderFactory.class)
  public CriteriaBuilderFactory createCriteriaBuilderFactory(
      final EntityManagerFactory entityManagerFactory) {
    final CriteriaBuilderConfiguration config = Criteria.getDefault();
    return config.createCriteriaBuilderFactory(entityManagerFactory);
  }
}
