package com.savdev.sse.consumer;

import com.google.common.collect.Sets;
import com.savdev.sse.rest.jeeasse.SseService;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.Set;

/**
 *
 */
@Singleton
@Startup
public class SseConsumer {

    @Inject
    SseService sseService;

    Set<String> messages = Sets.newHashSet("message1", "message2", "message3", "message4");


    @Schedule(second = "0/5", minute = "*", hour = "*", persistent = false)
    @Lock(LockType.READ)
    public void timedEvent() {
        messages.forEach(m -> sseService.notifySubscribers(m));

    }
}
