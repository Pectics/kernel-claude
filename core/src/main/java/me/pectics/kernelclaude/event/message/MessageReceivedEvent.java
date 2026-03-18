package me.pectics.kernelclaude.event.message;

import lombok.Getter;
import me.pectics.kernelclaude.event.Event;
import me.pectics.kernelclaude.message.Message;

@Getter
public class MessageReceivedEvent extends Event {

    private final Message message;

    public MessageReceivedEvent(Message message) {
        super();
        this.message = message;
    }

}
