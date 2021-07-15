package batch.logger;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import javax.inject.Singleton;
import logger.Payload;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Syncs payloads passed to the target endpoint
 */
@Singleton
public class BatchSyncer {

  private final TargetClient targetClient;
  Logger log = LoggerFactory.getLogger(BatchSyncer.class);

  public BatchSyncer(TargetClient targetClient) {
    this.targetClient = targetClient;
  }


  /**
   * Syncs payloads
   *
   * @param payloads request payloads to sync
   */
  @SneakyThrows
  public void sync(List<Payload> payloads) {

    LocalTime start = LocalTime.now();

    for (int i = 0; i < 3; i++) {
      try {
        HttpResponse<String> res = targetClient.sync(payloads);

        // Request was successful
        log.info("Synced Batch of Size {} which returned status code {} and took {}",
            payloads.size(), res.status().getCode(), Duration.between(start, LocalTime.now()));

        return;
      } catch (Exception e) {
        // Request failed wait for 2 seconds and re-try
        Thread.sleep(2000);
      }
    }

    // Sync failed for 3 times in a row, exit the application
    log.error("Could not sync payloads to target endpoint");
    System.exit(0);

  }


  /**
   * Declarative client for syncing to target endpoint
   */
  @Client("${batch.logger.target}")
  interface TargetClient {

    @Post("/")
    HttpResponse<String> sync(@Body List<Payload> payloads);

  }
}
