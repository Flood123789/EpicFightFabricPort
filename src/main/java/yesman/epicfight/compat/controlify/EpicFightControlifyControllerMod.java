package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;

/// Allows Epic Fight to communicate with Controlify APIs without depending on their classes directly.
@ApiStatus.Internal
public class EpicFightControlifyControllerMod implements IEpicFightControllerMod {
    @Override
    public String getModName() {
        return "Controlify";
    }

    @Override
    public @NotNull InputMode getInputMode() {
        final InputMode inputMode = switch (EpicFightControlifyEntrypoint.getApi().currentInputMode()) {
            case KEYBOARD_MOUSE -> InputMode.KEYBOARD_MOUSE;
            case CONTROLLER -> InputMode.CONTROLLER;
            case MIXED -> InputMode.MIXED;
        };

        // Controlify keeps reporting a controller input mode for a moment after the controller is gone.
        // Without a controller there is no binding state to read, so keyboard/mouse is the only truth.
        if (inputMode != InputMode.KEYBOARD_MOUSE && EpicFightControlifyEntrypoint.getApi().getCurrentController().isEmpty()) {
            return InputMode.KEYBOARD_MOUSE;
        }

        return inputMode;
    }

    public static @NotNull ControllerBinding getBinding(@NotNull EpicFightInputAction action) {
        return new ControlifyControllerBinding(EpicFightControlifyEntrypoint.getControlifyBinding(action));
    }

    public static @NotNull ControllerBinding getBinding(@NotNull MinecraftInputAction action) {
        return new ControlifyControllerBinding(EpicFightControlifyEntrypoint.getControlifyBinding(action));
    }

    /// Yields `null` when the binding cannot be resolved, letting the caller fall back to the key mapping.
    public static @Nullable ControllerBinding getBindingOrNull(@NotNull EpicFightInputAction action) {
        final InputBinding binding = EpicFightControlifyEntrypoint.getControlifyBindingOrNull(action);
        return binding == null ? null : new ControlifyControllerBinding(binding);
    }

    /// @see #getBindingOrNull(EpicFightInputAction)
    public static @Nullable ControllerBinding getBindingOrNull(@NotNull MinecraftInputAction action) {
        final InputBinding binding = EpicFightControlifyEntrypoint.getControlifyBindingOrNull(action);
        return binding == null ? null : new ControlifyControllerBinding(binding);
    }

    @Override
    public @NotNull PlayerInputState getInputState() {
        ControllerEntity controller = EpicFightControlifyEntrypoint.requireControllerEntity();

        InputBinding forwardBind = ControlifyBindings.WALK_FORWARD.on(controller);
        InputBinding backwardBind = ControlifyBindings.WALK_BACKWARD.on(controller);
        InputBinding leftBind = ControlifyBindings.WALK_LEFT.on(controller);
        InputBinding rightBind = ControlifyBindings.WALK_RIGHT.on(controller);
        InputBinding jumpBind = ControlifyBindings.JUMP.on(controller);
        InputBinding sneakBind = ControlifyBindings.SNEAK.on(controller);

        float forwardImpulse = forwardBind.analogueNow() - backwardBind.analogueNow();
        float leftImpulse = leftBind.analogueNow() - rightBind.analogueNow();

        return new PlayerInputState(
                leftImpulse, forwardImpulse,
                forwardBind.digitalNow(), backwardBind.digitalNow(),
                leftBind.digitalNow(), rightBind.digitalNow(),
                jumpBind.digitalNow(), sneakBind.digitalNow()
        );
    }
}