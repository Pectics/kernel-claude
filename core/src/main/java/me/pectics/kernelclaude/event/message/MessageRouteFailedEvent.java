package me.pectics.kernelclaude.event.message;

import lombok.Getter;
import me.pectics.kernelclaude.event.Event;
import me.pectics.kernelclaude.message.Message;

@Getter
public class MessageRouteFailedEvent extends Event {

    private final Message message;
    private final Throwable cause;

    public MessageRouteFailedEvent(Message message, Throwable cause) {
        super();
        this.message = message;
        this.cause = cause;
    }

}
