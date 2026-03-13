/*
 * Based on LuckPerms' Group
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Represents a permission group that can be inherited by users or other groups.
 */
public interface Group extends PermissionHolder {

    /**
     * Gets the name of this group.
     *
     * @return the group name
     */
    @NotNull String getName();

    /**
     * Gets the display name of this group, if set.
     *
     * @return the display name, or null if not set
     */
    @Nullable String getDisplayName();

    /**
     * Sets the display name.
     *
     * @param displayName the display name, or null to clear
     */
    void setDisplayName(@Nullable String displayName);

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
     * Gets the friendly name for this group.
     *
     * <p>Returns the display name if set, otherwise the group name.</p>
     *
     * @return the friendly name
     */
    @Override
    default @NotNull String getFriendlyName() {
        String displayName = getDisplayName();
        return displayName != null ? displayName : getName();
    }

    /**
     * Gets the unique identifier for this group.
     *
     * @return the group name
     */
    @Override
    default @NotNull String getIdentifier() {
        return getName();
    }
}
