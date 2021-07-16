package batch.logger;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import logger.Payload;


/**
 * Transmits payloads and flush events with the help of Publisher Subject
 */
class PayloadsTransmitter {

  private static final PublishSubject<Payload> payloadsObservable = PublishSubject.create();
  private static final PublishSubject<String> flushObservable = PublishSubject.create();

  private PayloadsTransmitter() {
    // Private constructor so class can not be initialized
  }

  public static Disposable subscribeToPayloadsPublisher(Consumer<Payload> onNext) {
    return payloadsObservable.subscribe(onNext);
  }

  public static Disposable subscribeToFlushPublisher(Consumer<String> onFlush) {
    return flushObservable.subscribe(onFlush);
  }

  public static void transmit(Payload payload) {
    payloadsObservable.onNext(payload);
  }

  public static void flush(String flushMessage) {
    flushObservable.onNext(flushMessage);
  }

}
