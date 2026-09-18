package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.client.event.RegisterKeyMappingsEvent;
import yesman.epicfight.forgecompat.eventbus.api.SubscribeEvent;
import yesman.epicfight.forgecompat.fml.common.Mod;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EpicFightKeyMappings {
    private static boolean fabricKeysRegistered;

    // GUI key-mappings
    public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP =
            new KeyMapping(
                    LangKeys.KEY_SHOW_TOOLTIP,
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    EpicFightInputCategories.GUI
            );

    public static final KeyMapping SKILL_EDIT =
            new KeyMapping(
                    LangKeys.KEY_SKILL_GUI,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_K,
                    EpicFightInputCategories.GUI
            );

    public static final KeyMapping OPEN_CONFIG_SCREEN =
            new KeyMapping(
                    LangKeys.KEY_CONFIG,
                    InputConstants.Type.KEYSYM,
                    -1,
                    EpicFightInputCategories.GUI
            );

    // In-game keymappings
    public static final KeyMapping DODGE =
            new CombatKeyMapping(
                    LangKeys.KEY_DODGE,
                    InputConstants.KEY_LALT,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping GUARD =
            new CombatKeyMapping(
                    LangKeys.KEY_GUARD,
                    InputConstants.UNKNOWN.getValue(),
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping ATTACK =
            new CombatKeyMapping(
                    LangKeys.KEY_ATTACK,
                    InputConstants.UNKNOWN.getValue(),
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping WEAPON_INNATE_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_WEAPON_INNATE_SKILL,
                    InputConstants.UNKNOWN.getValue(),
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping MOVER_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_MOVER_SKILL,
                    InputConstants.UNKNOWN.getValue(),
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping SWITCH_MODE =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_MODE,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_R,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping LOCK_ON =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_LEFT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_LEFT,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_LEFT,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_RIGHT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_RIGHT,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_RIGHT,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_FREELY =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_FREELY,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_MIDDLE,
                    EpicFightInputCategories.CAMERA
            );

    // Systemical key mappings especially for debugging
    public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_VANILLA_MODEL_DEBUG,
                    InputConstants.Type.KEYSYM,
                    -1,
                    EpicFightInputCategories.SYSTEM
            );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(WEAPON_INNATE_SKILL_TOOLTIP);
        event.register(SWITCH_MODE);
        event.register(DODGE);
        event.register(GUARD);
        event.register(ATTACK);
        event.register(WEAPON_INNATE_SKILL);
        event.register(MOVER_SKILL);
        event.register(SKILL_EDIT);
        event.register(LOCK_ON);
        event.register(LOCK_ON_SHIFT_LEFT);
        event.register(LOCK_ON_SHIFT_RIGHT);
        event.register(LOCK_ON_SHIFT_FREELY);
        event.register(OPEN_CONFIG_SCREEN);
        event.register(SWITCH_VANILLA_MODEL_DEBUGGING);
    }

    public static void registerFabricKeys() {
        if (fabricKeysRegistered) {
            return;
        }

		fabricKeysRegistered = true;
		// This is a physical Shift modifier, not an independently registered action.
		// Registering it on Fabric replaces vanilla crouch in KeyMapping's one-entry
		// key lookup, so the physical key remains down while options.keyShift never is.
		KeyBindingHelper.registerKeyBinding(SWITCH_MODE);
        KeyBindingHelper.registerKeyBinding(DODGE);
        KeyBindingHelper.registerKeyBinding(GUARD);
        KeyBindingHelper.registerKeyBinding(ATTACK);
        KeyBindingHelper.registerKeyBinding(WEAPON_INNATE_SKILL);
        KeyBindingHelper.registerKeyBinding(MOVER_SKILL);
        KeyBindingHelper.registerKeyBinding(SKILL_EDIT);
        KeyBindingHelper.registerKeyBinding(LOCK_ON);
        KeyBindingHelper.registerKeyBinding(LOCK_ON_SHIFT_LEFT);
        KeyBindingHelper.registerKeyBinding(LOCK_ON_SHIFT_RIGHT);
        KeyBindingHelper.registerKeyBinding(LOCK_ON_SHIFT_FREELY);
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_SCREEN);
        KeyBindingHelper.registerKeyBinding(SWITCH_VANILLA_MODEL_DEBUGGING);
    }

    public static void sanitizeVanillaFallbackKeyConflicts() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft == null || minecraft.options == null) {
            return;
        }

        Options options = minecraft.options;
        boolean changed = false;
        changed |= clearIfSameAsVanilla(ATTACK, options.keyAttack);
        changed |= clearIfSameAsVanilla(WEAPON_INNATE_SKILL, options.keyAttack);
        changed |= clearIfSameAsVanilla(GUARD, options.keyUse);
        changed |= clearIfSameAsVanilla(MOVER_SKILL, options.keyJump);

        if (changed) {
            KeyMapping.resetMapping();
            options.save();
        }
    }

    private static boolean clearIfSameAsVanilla(KeyMapping combatKey, KeyMapping vanillaKey) {
        if (combatKey.key.getValue() == InputConstants.UNKNOWN.getValue() || !combatKey.key.equals(vanillaKey.key)) {
            return false;
        }

        combatKey.setKey(InputConstants.UNKNOWN);
        return true;
    }
}
