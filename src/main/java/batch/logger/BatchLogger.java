package batch.logger;

import io.micronaut.scheduling.annotation.Scheduled;
import javax.inject.Singleton;
import logger.Payload;

@Singleton
public class BatchLogger {

  /**
   * Adds payloads to collection object
   *
   * @param payload Payload from request
   */
  public void log(Payload payload) {
    PayloadsTransmitter.transmit(payload);
  }

  /*
   * Sync payloads at fixed interval
   * */
  @Scheduled(
      initialDelay = "${batch.logger.interval}",
      fixedDelay = "${batch.logger.interval}"
  )
  protected void startInterval() {
    PayloadsTransmitter.flush("timeout occurred");
  }

}
