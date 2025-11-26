package io.github.lgtm.springframework.jpa.querydsl.web.exception;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author <a href="mailto:hqq.w2h@gmail.com">Weiwei Han</a>
 */
@Slf4j
@Component
public class ExceptionHandler {
  @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public final ResponseEntity<?> handleBusinessException(Exception ex) {
    log.error(ex.getMessage(), ex);
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

    pd.setTitle("Service Exception");
    pd.setDetail(ex.getMessage());

    pd.setProperty("timestamp", Instant.now().toString());

    return ResponseEntity.internalServerError().body(pd);
  }
}
