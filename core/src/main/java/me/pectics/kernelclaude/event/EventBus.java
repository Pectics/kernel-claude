package me.pectics.kernelclaude.event;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
public class EventBus {

    private final Map<Class<? extends Event>, List<RegisteredHandler>> handlers = new ConcurrentHashMap<>();

    public void register(EventListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                log.warn("Ignoring @EventHandler method {}: must have exactly one parameter extending Event",
                        method.getName());
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) params[0];
            method.setAccessible(true);

            RegisteredHandler handler = new RegisteredHandler(
                    annotation.priority(), listener, method, null
            );
            handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
    }

    public <T extends Event> void register(Class<T> type, EventPriority priority, Consumer<T> handler) {
        RegisteredHandler registered = new RegisteredHandler(priority, null, null, handler);
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(registered);
    }

    public void unregister(EventListener listener) {
        handlers.values().forEach(list ->
                list.removeIf(h -> h.listener == listener)
        );
    }

    public void dispatch(Event event) {
        List<RegisteredHandler> matched = new ArrayList<>();
        Class<?> clazz = event.getClass();
        while (clazz != null && Event.class.isAssignableFrom(clazz)) {
            List<RegisteredHandler> list = handlers.get(clazz);
            if (list != null) {
                matched.addAll(list);
            }
            clazz = clazz.getSuperclass();
        }

        matched.sort(Comparator.comparingInt(h -> h.priority.getLevel()));

        for (RegisteredHandler handler : matched) {
            try {
                if (handler.method != null) {
                    handler.method.invoke(handler.listener, event);
                } else if (handler.consumer != null) {
                    @SuppressWarnings("unchecked")
                    Consumer<Event> consumer = (Consumer<Event>) handler.consumer;
                    consumer.accept(event);
                }
            } catch (Exception e) {
                log.error("Error dispatching event {} to handler", event.getClass().getSimpleName(), e);
            }
        }
    }

    private record RegisteredHandler(
            EventPriority priority,
            EventListener listener,
            Method method,
            Consumer<? extends Event> consumer
    ) { }

}
