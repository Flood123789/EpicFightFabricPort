package yesman.epicfight.client.input;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import yesman.epicfight.client.ClientEngine;

/// A specialized [KeyMapping] used by Epic Fight to represent combat-related key bindings.
///
/// This enforces that all [KeyMapping#isDown()] or related checks to return `false`
/// whenever the player is **not** in Epic Fight mode.
///
/// **Important:** Other mods or consumers should *not* rely on this behavior.
/// They should explicitly check whether the player is in Epic Fight mode through
/// [yesman.epicfight.client.ClientEngine#isEpicFightMode()] instead of depending on
/// this key mapping's conditional logic.
///
/// This class is primarily used as a fallback or metadata reference for compatibility with
/// other mods (hopefully!).
/// Otherwise, it has no meaningful function beyond normal [KeyMapping] behavior.
///
/// Future maintainers should consider refactoring or removing this class
/// if it becomes problematic or a maintenance burden.
public class CombatKeyMapping extends KeyMapping {
    public CombatKeyMapping(String description, int code, String category) {
        this(description, InputConstants.Type.KEYSYM, code, category);
    }

    public CombatKeyMapping(String description, InputConstants.Type type, int code, String category) {
        super(description, type, code, category);
    }

    @Override
    public boolean isActiveAndMatches(@NotNull InputConstants.Key keyCode) {
        return this.isCombatActive() && super.isActiveAndMatches(keyCode);
    }

    @Override
    public boolean isDown() {
        return this.isCombatActive() && super.isDown();
    }

    @Override
    public boolean consumeClick() {
        if (this.isCombatActive()) {
            return super.consumeClick();
        }

        while (super.consumeClick()) {
            // Drain clicks accumulated by duplicate vanilla mouse bindings while battle mode is off.
        }

        return false;
    }

    @Override
    public void setDown(boolean value) {
        super.setDown(this.isCombatActive() && value);
    }

    private boolean isCombatActive() {
        return ClientEngine.getInstance().isEpicFightMode();
    }
}
