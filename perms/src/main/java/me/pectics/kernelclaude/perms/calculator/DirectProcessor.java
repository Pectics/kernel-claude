/*
 * Based on LuckPerms' DirectProcessor
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.calculator;

import lombok.Getter;
import lombok.Setter;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Processor that handles direct permission matches.
 *
 * <p>This is the simplest processor - it just maps permission keys
 * to their Tristate values directly.</p>
 */
public class DirectProcessor implements PermissionProcessor {

    private @Setter @NotNull Map<String, Tristate> source = Map.of();
    private @Getter @NotNull Map<String, Tristate> result = Map.of();

    @Override
    public void process() {
        // Direct processor just copies the source to result
        this.result = new HashMap<>(source);
    }

    @Override
    public void onNodeAdd(@NotNull Node node) {
        // Will be recalculated on next process()
    }

    @Override
    public void onNodeRemove(@NotNull Node node) {
        // Will be recalculated on next process()
    }

    @Override
    public void invalidate() {
        this.result = Map.of();
    }

}
