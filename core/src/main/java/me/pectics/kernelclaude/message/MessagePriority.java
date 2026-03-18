package me.pectics.kernelclaude.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagePriority {

    CRITICAL(1),
    HIGH(2),
    NORMAL(3),
    LOW(4);

    private final int level;

}
