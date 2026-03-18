package me.pectics.kernelclaude.message;

@FunctionalInterface
public interface MessageRouter {

    void route(Message message);

}
