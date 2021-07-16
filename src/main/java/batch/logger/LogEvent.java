package batch.logger;

import logger.Payload;

/**
 * A POJO which either hold log payload or indicates if we want to flush payloads
 */
public class LogEvent {

  private final Payload payload;
  private final Boolean flush;

  private LogEvent(Payload payload, Boolean flush) {
    this.payload = payload;
    this.flush = flush;
  }

  public static LogEvent payload(Payload payload) {
    return new LogEvent(payload, false);
  }

  public static LogEvent flush() {
    return new LogEvent(null, true);
  }

  public Payload getPayload() {
    return payload;
  }

  public Boolean getFlush() {
    return flush;
  }

}
