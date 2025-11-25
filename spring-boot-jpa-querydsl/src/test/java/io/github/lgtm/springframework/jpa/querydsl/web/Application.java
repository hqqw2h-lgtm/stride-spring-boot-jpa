package io.github.lgtm.springframework.jpa.querydsl.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author <a href="mailto:weiwei.han2@cn.bosch.com">Weiwei Han</a>
 */
@SpringBootApplication()
@EntityScan(basePackages = "io.github.lgtm.springframework.jpa")
@EnableAutoEntityController()
public class Application extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }
}
