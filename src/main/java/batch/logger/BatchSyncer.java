package batch.logger;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.event.annotation.EventListener;
import io.reactivex.disposables.Disposable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

  private final Logger log = LoggerFactory.getLogger(BatchSyncer.class);
  private final TargetClient targetClient;

  // Disposables returned on subscribe
  private Disposable payloadsDisposable;
  private Disposable flushDisposable;

  // Create a thread safe collection object
  private Collection<Payload> payloads = Collections.synchronizedCollection(new ArrayList<>());

  @Property(name = "batch.logger.size")
  private Integer batchSize;

  public BatchSyncer(TargetClient targetClient) {
    this.targetClient = targetClient;
  }

  /**
   * Subscribe to payloads publisher on startup
   */
  @EventListener
  void subscribeToPayloadsPublisher(StartupEvent startupEvent) {
    payloadsDisposable = PayloadsTransmitter.subscribeToPayloadsPublisher(this::onPayload);
  }


  @EventListener
  void subscribeToFlushPublisher(StartupEvent startupEvent) {
    flushDisposable = PayloadsTransmitter.subscribeToFlushPublisher(this::onFlush);
  }


  /**
   * Passed as consumer to payloads publisher
   *
   * @param payload payload emitted from publisher
   */
  private void onPayload(Payload payload) {
    this.payloads.add(payload);

    if (payloads.size() >= batchSize) {
      this.sync();
    }
  }


  /**
   * Passed as consumer to flush publisher
   *
   * @param message Reason for flushing payloads
   */
  private void onFlush(String message) {
    if (!payloads.isEmpty()) {
      this.sync();
    }
  }


  /**
   * Prepares payloads to sync to target endpoint
   */
  private synchronized void sync() {
    // If there are too many parallel threads running, this might get called
    // even tho payloads object was empty because of race condition
    if (payloads.isEmpty()) {
      return;
    }

    // Assign current payloads object to a new variable
    Collection<Payload> oldPayloads = payloads;
    // Create new payloads object and assign it to current one
    payloads = Collections.synchronizedCollection(new ArrayList<>());

    // Start syncing of logs on a new thread
    new Thread(() -> this.sync(oldPayloads)).start();
  }


  /**
   * Sync payloads to target endpoint
   *
   * @param payloads collection of payloads
   */
  @SneakyThrows
  private void sync(Collection<Payload> payloads) {
    // Start time of the request
    LocalTime start = LocalTime.now();

    for (int i = 0; i < 3; i++) {
      try {
        HttpResponse<String> res = targetClient.sync(payloads);

        // Request was successful
        log.info("Synced Batch of Size {} which returned status code {} and took {}",
            payloads.size(), res.status().getCode(), Duration.between(start, LocalTime.now()));

        return;
      } catch (Exception ignored) {
        Thread.sleep(2000);
      }
    }

    // Sync failed for 3 times in a row, exit the application
    log.error("Could not sync payloads to target endpoint");

    // Dispose the subscriptions
    payloadsDisposable.dispose();
    flushDisposable.dispose();

    // Exit the application
    System.exit(0);
  }


  /**
   * Declarative client for syncing to target endpoint
   */
  @Client("${batch.logger.target}")
  interface TargetClient {

    @Post("/")
    HttpResponse<String> sync(@Body Collection<Payload> payloads);

  }
}
