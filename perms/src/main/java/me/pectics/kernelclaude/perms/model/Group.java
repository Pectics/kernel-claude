/*
 * Based on LuckPerms' Group
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

/**
 * Represents a permission group that can be inherited by users or other groups.
 */
public non-sealed interface Group extends PermissionHolder {

    /**
     * Gets the name of this group.
     *
     * @return the group name
     */
    @NotNull String getGroupId();

    /**
     * Gets the weight/priority of this group.
     *
     * <p>Higher weight = higher priority in inheritance resolution.</p>
     *
     * @return the weight, or empty if not set
     */
    @NotNull OptionalInt getWeight();

    /**
     * Sets the weight of this group.
     *
     * @param weight the weight
     */
    void setWeight(int weight);

    /**
     * Clears the weight setting.
     */
    void clearWeight();

    /**
     * Gets the unique identifier for this group.
     *
     * @return the group name
     */
    @Override
    default @NotNull String getIdentifier() {
        return getGroupId();
    }

}
