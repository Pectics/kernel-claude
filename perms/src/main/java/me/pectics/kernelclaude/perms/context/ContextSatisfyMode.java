/*
 * Based on LuckPerms' ContextSatisfyMode implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

/**
 * Mode for determining whether a {@link ContextSet} satisfies another.
 */
public enum ContextSatisfyMode {

    /**
     * Mode where a context set A will be satisfied by another set B,
     * if all key-value entries in A are also in B.
     *
     * <p>For example, given A = {platform=discord, channel=moderation},
     * another set X will satisfy A if X contains
     * platform=discord AND channel=moderation.</p>
     */
    ALL_VALUE_MATCH_PER_KEY,

    /**
     * Mode where a context set A will be satisfied by another set B,
     * if at least one of the key-value entries per key in A are also in B.
     *
     * <p>For example, given A = {platform=discord, platform=telegram},
     * another set X will satisfy A if X contains
     * platform=discord OR platform=telegram.</p>
     */
    ANY_VALUE_MATCH_PER_KEY,
}
