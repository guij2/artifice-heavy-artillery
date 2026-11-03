package com.heavyartillery.weapon;

import org.jetbrains.annotations.Nullable;

/**
 * Item-level traits of a heavy weapon: things that apply while the weapon is <i>carried</i>, rather than per shot.
 * <p>
 * Per-shot traits (spin-up, bracing, ...) belong in {@link HeavyShotComponents} instead, so modifiers can change them.
 * These live on the item because they are checked every tick, without composing a shot.
 *
 * @param sprintLockedWhileFiring whether the holder is stopped from sprinting while sustaining fire
 * @param hold                    how it is carried in third person, or null for the parent mod's rifle pose
 */
public record HeavyTraits(boolean sprintLockedWhileFiring, @Nullable HeavyHold hold) {
    public static final HeavyTraits NONE = builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean sprintLockedWhileFiring = false;
        private @Nullable HeavyHold hold = null;

        private Builder() {
        }

        public Builder sprintLockedWhileFiring() {
            this.sprintLockedWhileFiring = true;
            return this;
        }

        public Builder hold(HeavyHold hold) {
            this.hold = hold;
            return this;
        }

        public HeavyTraits build() {
            return new HeavyTraits(sprintLockedWhileFiring, hold);
        }
    }
}
