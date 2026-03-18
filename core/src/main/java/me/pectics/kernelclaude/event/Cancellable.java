package me.pectics.kernelclaude.event;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
