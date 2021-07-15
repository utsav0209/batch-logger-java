package logger;

import batch.logger.BatchLogger;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

@Controller("/")
public class LogController {

  private final BatchLogger batchLogger;

  public LogController(BatchLogger batchLogger) {
    this.batchLogger = batchLogger;
  }

  @Get("/healthz")
  HttpResponse<String> healthz() {
    return HttpResponse.ok("OK");
  }

  @Post("/log")
  HttpResponse<String> log(@Body Payload payload) {
    batchLogger.log(payload);
    return HttpResponse.ok("Logged Successfully");
  }

}
