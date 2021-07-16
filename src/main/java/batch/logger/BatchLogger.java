package batch.logger;

import io.micronaut.context.annotation.Property;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.inject.Singleton;
import logger.Payload;

/**
 * Logs payloads when payloads reach specified batch size or at specific intervals
 */
@Singleton
public class BatchLogger {

  private final BatchSyncer syncer;
  // Create a thread safe collection object
  private Collection<Payload> payloads = Collections.synchronizedCollection(new ArrayList<>());

  @Property(name = "batch.logger.size")
  private String batchSize;

  public BatchLogger(BatchSyncer syncer) {
    this.syncer = syncer;
  }


  /**
   * Adds payloads to collection object
   *
   * @param payload Payload from request
   */
  public void log(Payload payload) {
    payloads.add(payload);

    // If payloads size has exceeded batch size then sync it
    if (payloads.size() >= Integer.parseInt(batchSize)) {
      sync();
    }
  }


  /*
   * Sync payloads at fixed interval
   * */
  @Scheduled(
      initialDelay = "${batch.logger.interval}",
      fixedDelay = "${batch.logger.interval}"
  )
  protected void startInterval() {
    if (!payloads.isEmpty()) {
      sync();
    }
  }

  /*
   * Send payloads to syncer and clear collections object
   * */
  private synchronized void sync() { // Synchronized so only one execution happns at a time
    if (payloads.isEmpty()) {
      return;
    }

    // Assign current payloads object to a new variable
    var payloadsCopy = payloads;

    // Create a new payloads object
    payloads = Collections.synchronizedCollection(new ArrayList<>());

    // Start syncing of payloads on a new thread so that it does not block any request
    syncer.sync(payloadsCopy);

  }

}
