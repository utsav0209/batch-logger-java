package filter;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import java.time.Duration;
import java.time.LocalTime;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter("/log")
public class RequestTracer implements HttpServerFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestTracer.class);

  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request,
      ServerFilterChain chain) {

    // Set current time for start of the request
    LocalTime startTime = LocalTime.now();

    return Publishers.map(chain.proceed(request), response -> {
      Duration timeElapsed = Duration.between(startTime, LocalTime.now());

      log.info("Received request with method: {}, route: {} and served in {}ms\n",
          request.getMethod(), request.getUri(), timeElapsed.toMillis());
      return response;
    });
  }
}
