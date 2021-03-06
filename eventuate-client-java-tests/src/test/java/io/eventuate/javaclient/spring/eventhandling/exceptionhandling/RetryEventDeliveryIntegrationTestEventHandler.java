package io.eventuate.javaclient.spring.eventhandling.exceptionhandling;


import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.SubscriberInitialPosition;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.services.EventTracker;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ConcurrentHashMap;

@EventSubscriber(id="eventHandlerRetryEventHandler2", readFrom = SubscriberInitialPosition.END)
public class RetryEventDeliveryIntegrationTestEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventTracker<EventHandlerContext<?>> events = EventTracker.create();

  public EventTracker<EventHandlerContext<?>> getEvents() {
    return events;
  }

  private ConcurrentHashMap<Integer, Boolean> tracking = new ConcurrentHashMap<>();

  @Autowired
  private EventDeliveryExceptionHandler eventDeliveryExceptionHandler;

  @EventHandlerMethod
  @Qualifier("forEventHandlerRetryEventHandler")
  public void accountCreated(EventHandlerContext<AccountCreatedEvent> ctx) {
    if (tracking.compute(ctx.getSwimlane(), (Integer k, Boolean value) -> value == null || !value)) {
      // was false
      logger.info("throwing RetryEventHandlerException {}", ctx.getSwimlane());
      throw new RetryEventDeliveryIntegrationTestException();
    } else {
      logger.info("processing {}", ctx.getSwimlane());
      events.onNext(ctx);
    }
  }

}

