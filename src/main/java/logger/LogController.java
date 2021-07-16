package logger;

import batch.logger.BatchProcessor;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

@Controller
public class LogController {

  private final BatchProcessor batchProcessor;

  public LogController(BatchProcessor batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  /**
   * @return HTTP 200 with OK as body
   */
  @Get("/healthz")
  HttpResponse<String> healthz() {
    return HttpResponse.ok("OK");
  }

  /**
   * Log payloads are received at this endpoint
   *
   * @param payload Log payload
   * @return HTTP 200 with success message
   */
  @Post("/log")
  HttpResponse<String> log(@Body Payload payload) {
    batchProcessor.log(payload);
    return HttpResponse.ok("Logged Successfully");
  }

}
