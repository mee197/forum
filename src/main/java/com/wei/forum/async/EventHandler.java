package com.wei.forum.async;

import java.util.List;


public interface EventHandler {
    void doHandle(EventModel model);

    List<EventType> getSupportEventTypes();
}
