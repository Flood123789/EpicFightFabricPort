package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.platform.InputConstants;

import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.compat.controlify.EpicFightControlifyControllerMod;

import java.util.*;

/// Represents a default set of input actions used in the Epic Fight mod.
///
/// Each action is linked to a corresponding Minecraft vanilla [KeyMapping] or
/// an Epic Fight custom key mapping.
///
/// These mappings only support keyboard and mouse input.
/// Controller input is not directly supported to avoid dependencies on third-party controller mods.
///
/// For implementation details, refer to [#keyMapping()].
public enum EpicFightInputAction implements InputAction {
    ATTACK,
    MOBILITY,
    GUARD,
    DODGE,
    LOCK_ON,
    LOCK_ON_SHIFT_LEFT,
    LOCK_ON_SHIFT_RIGHT,
    LOCK_ON_SHIFT_FREELY,
    SWITCH_MODE,
    WEAPON_INNATE_SKILL,
    WEAPON_INNATE_SKILL_TOOLTIP,
    OPEN_SKILL_SCREEN,
    OPEN_CONFIG_SCREEN,
    SWITCH_VANILLA_MODEL_DEBUGGING;

    final private int id;

    EpicFightInputAction() {
        this.id = InputAction.ENUM_MANAGER.assign(this);
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }

    @Override
    @NotNull
    public KeyMapping keyMapping() {
        return switch (this) {
            case ATTACK -> fallbackToVanilla(EpicFightKeyMappings.ATTACK, MinecraftInputAction.ATTACK_DESTROY);
            case MOBILITY -> fallbackToVanilla(EpicFightKeyMappings.MOVER_SKILL, MinecraftInputAction.JUMP);
            case GUARD -> fallbackToVanilla(EpicFightKeyMappings.GUARD, MinecraftInputAction.USE);
            case DODGE -> EpicFightKeyMappings.DODGE;
            case LOCK_ON -> EpicFightKeyMappings.LOCK_ON;
            case LOCK_ON_SHIFT_LEFT -> EpicFightKeyMappings.LOCK_ON_SHIFT_LEFT;
            case LOCK_ON_SHIFT_RIGHT -> EpicFightKeyMappings.LOCK_ON_SHIFT_RIGHT;
            case LOCK_ON_SHIFT_FREELY -> EpicFightKeyMappings.LOCK_ON_SHIFT_FREELY;
            case SWITCH_MODE -> EpicFightKeyMappings.SWITCH_MODE;
            case WEAPON_INNATE_SKILL -> fallbackToVanilla(EpicFightKeyMappings.WEAPON_INNATE_SKILL, MinecraftInputAction.ATTACK_DESTROY);
            // This is a modifier gesture, not a separately registered key. A second
            // KeyMapping on Left Shift replaces vanilla crouch in Fabric's key table.
            case WEAPON_INNATE_SKILL_TOOLTIP -> MinecraftInputAction.SNEAK.keyMapping();
            case OPEN_SKILL_SCREEN -> EpicFightKeyMappings.SKILL_EDIT;
            case OPEN_CONFIG_SCREEN -> EpicFightKeyMappings.OPEN_CONFIG_SCREEN;
            case SWITCH_VANILLA_MODEL_DEBUGGING -> EpicFightKeyMappings.SWITCH_VANILLA_MODEL_DEBUGGING;
        };
    }

    private static KeyMapping fallbackToVanilla(KeyMapping combatKey, MinecraftInputAction vanillaAction) {
        KeyMapping vanillaKey = vanillaAction.keyMapping();
        return combatKey.key.getValue() == InputConstants.UNKNOWN.getValue() || combatKey.key.equals(vanillaKey.key) ? vanillaKey : combatKey;
    }

    @Override
    public @NotNull Optional<@NotNull ControllerBinding> controllerBinding() {
        if (EpicFightControllerModProvider.get() == null) {
            throw new IllegalStateException("controllerBinding() must not be called when the controller mod is not installed");
        }
        return Optional.ofNullable(EpicFightControlifyControllerMod.getBindingOrNull(this));
    }
}
