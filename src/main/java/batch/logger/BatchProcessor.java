package batch.logger;

import io.micronaut.context.annotation.Property;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import logger.Payload;
import lombok.SneakyThrows;

/**
 * Holds the payloads received in a list and syncs them on intervals or when batch size has
 * exceeded.
 */
@Singleton
public class BatchProcessor {

  // Using PublishSubject so that we have incoming of log data in a non blocking manner and
  // when sync to target is going on it stores the logs and emits them once the syncing is done
  private final PublishSubject<LogEvent> logEventPublishSubject;
  private final List<Payload> payloads = new ArrayList<>();

  private final LogSyncer logSyncer;

  @Property(name = "batch.logger.size")
  private Integer batchSize;

  public BatchProcessor(LogSyncer logSyncer) {
    this.logSyncer = logSyncer;
    logEventPublishSubject = PublishSubject.create();
  }

  /**
   * Subscribe to log events publisher for processing of incoming logs and flush events
   */
  @PostConstruct
  void subscribeToPayloadsPublisher() {
    logEventPublishSubject.subscribe(this::onPayload);
  }

  /*
   * Sync payloads at fixed interval
   * */
  @Scheduled(
      initialDelay = "${batch.logger.interval}",
      fixedDelay = "${batch.logger.interval}"
  )
  protected void startInterval() {
    logEventPublishSubject.onNext(LogEvent.flush());
  }

  /**
   * Emits data to PublishSubject and is an Asynchronous method so that incoming requests dont get
   * blocked
   *
   * @param payload Payload received from the request
   */
  @SneakyThrows
  @Async
  public void log(Payload payload) {
    logEventPublishSubject.onNext(LogEvent.payload(payload));
  }

  /**
   * Passed as consumer to payloads publisher. and is synchronized so that only one sync to target
   * is happening at a time
   */
  private synchronized void onPayload(LogEvent logEvent) {
    // If log event is for flushing of payloads and payloads is not empty sync them
    if (Boolean.TRUE.equals(logEvent.getFlush())) {
      if (!payloads.isEmpty()) {
        sync();
      }
      return;
    }

    // Add payload to list
    payloads.add(logEvent.getPayload());

    // If batch size exceeded then sync them
    if (payloads.size() >= batchSize) {
      sync();
    }
  }

  /**
   * Sync payloads using LogSyncer
   */
  private void sync() {
    logSyncer.sync(payloads);
    payloads.clear();
  }

}
