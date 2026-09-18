package yesman.epicfight.api.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;
import yesman.epicfight.client.input.DiscreteInputActionTrigger;

import java.util.Optional;
import java.util.function.Function;

/// High-level input API that abstracts direct interactions with [KeyMapping]
/// and supports controllers if an Epic Fight controller mod implementation is present
/// (see [EpicFightControllerModProvider]).
///
/// Use this class whenever possible to ensure input works consistently across
/// keyboard/mouse and supported controllers.
///
/// **Warning:** This API is currently marked as experimental.
/// This designation does not imply that the implementation is of an **'experimental'** quality,
/// but rather indicates that classes, methods, and fields may be subject to renaming, relocation, or removal.
/// The Epic Fight team reserves the right to modify or completely remove any components of the API at any time,
/// without prior notice.
@ApiStatus.Experimental
public final class InputManager {
    private InputManager() {
    }

    @Nullable
    private static IEpicFightControllerMod getControllerModApi() {
        return EpicFightControllerModProvider.get();
    }

    /// Returns the current input mode (keyboard/mouse or controller).
    /// Equivalent to [IEpicFightControllerMod#getInputMode()], but guaranteed
    /// to return a non-null value even if no controller mod is present.
    @NotNull
    public static InputMode getInputMode() {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        return controllerMod == null ? InputMode.KEYBOARD_MOUSE : controllerMod.getInputMode();
    }

    /// Checks if controller or gamepad input is currently supported.
    ///
    /// **Note:** The [InputMode#MIXED] mode supports both controller and keyboard/mouse input at the same time.
    /// Returning `true` here does not necessarily mean the input mode is exclusively [InputMode#CONTROLLER].
    ///
    /// @return `true` if a controller mod is present and the current input mode allows controller input.
    /// @see InputMode
    public static boolean supportsControllerInput() {
        return getInputMode().supportsController();
    }

    /// Returns whether the given input action is active during this tick.
    ///
    /// The behavior differs depending on the input source:
    ///
    /// - **Keyboard/Mouse:** Follows Minecraft's internal behavior.
    ///   May return `false` while a screen is open, even if the physical key is held down.
    /// - **Controller:** The behavior is handled externally and is irrelevant to this method.
    ///   It is usually determined by an input context during the
    ///   controller binding registration (third-party API),
    ///   which decides whether to return `false` or `true` when the physical input is down.
    ///
    /// If no controller mod is present, only the [KeyMapping] (Keyboard/Mouse) is checked.
    /// This is usually useful for in-game continuous actions.
    /// It should not be used while a screen is open.
    ///
    /// @param action the input action to check
    /// @see ControllerBinding
    public static boolean isActionActive(@NotNull InputAction action) {
        return checkAction(action, InputManager::isKeyDown);
    }

    /// Returns whether the given input action is currently physically active this tick.
    ///
    /// The behavior differs depending on the input source:
    ///  - **Keyboard/Mouse:** Always checks the physical key state, ignoring vanilla GUI filtering
    ///    and bypassing the [mouse multiple-keybind sharing bug](https://github.com/Epic-Fight/epicfight/issues/2174)
    ///    (present in versions before 1.21.10).
    /// - **Controller:** Similarly to [#isActionActive], the behavior is handled externally
    ///   and is irrelevant to this method.
    ///
    /// If no controller mod is present, only the [KeyMapping] (Keyboard/Mouse) is checked.
    /// This is usually useful for GUI continuous actions.
    /// It should not be used in-game with no screens.
    ///
    /// @param action the input action to check
    /// @see #isActionActive
    public static boolean isActionPhysicallyActive(@NotNull InputAction action) {
        return checkAction(action, InputManager::isPhysicalKeyDown);
    }

    /// Shared internal utility between [#isActionActive] and [#isActionPhysicallyActive] to handle the differences.
    private static boolean checkAction(@NotNull InputAction action, @NotNull Function<KeyMapping, Boolean> keyboardCheck) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod == null) {
            return keyboardCheck.apply(action.keyMapping());
        }

        return switch (controllerMod.getInputMode()) {
            case KEYBOARD_MOUSE -> keyboardCheck.apply(action.keyMapping());
            case CONTROLLER -> assignedControllerBinding(action)
                    .map(ControllerBinding::isDigitalActiveNow)
                    .orElse(keyboardCheck.apply(action.keyMapping()));
            case MIXED -> keyboardCheck.apply(action.keyMapping())
                    || assignedControllerBinding(action)
                    .map(ControllerBinding::isDigitalActiveNow)
                    .orElse(false);
        };
    }

    /// The action's controller binding, but only while a physical control is assigned to it.
    ///
    /// An action the player never bound on the controller has to keep working from the keyboard,
    /// otherwise plugging in a controller silently disables every Epic Fight key that has no
    /// controller default, such as the mode switch or the skill screen.
    ///
    /// @see ControllerBinding#isBound
    @ApiStatus.Internal
    public static Optional<@NotNull ControllerBinding> assignedControllerBinding(@NotNull InputAction action) {
        return action.controllerBinding().filter(ControllerBinding::isBound);
    }

    /// Called on every client tick to potentially trigger the provided callback for a given input action.
    ///
    /// @param action  The input action to monitor and trigger.
    /// @param handler The callback to invoke when the action triggers.
    /// @see DiscreteInputActionTrigger#triggerOnPress Internal implementation details.
    public static void triggerOnPress(@NotNull InputAction action, @NotNull DiscreteActionHandler handler) {
        DiscreteInputActionTrigger.triggerOnPress(action, handler);
    }

    /// Convenience overload of [#triggerOnPress(InputAction, DiscreteActionHandler)]
    /// for callbacks that do not require the [DiscreteActionHandler.Context].
    public static void triggerOnPress(@NotNull InputAction action, @NotNull Runnable runnable) {
        triggerOnPress(action, (context) -> runnable.run());
    }

    /// Checks whether the given input action is assigned to the same key / button as another action.
    ///
    /// For keyboard/mouse, this compares the key codes; for controllers, it compares the digital button.
    /// **Note:** [InputMode#MIXED] is currently unsupported and its behavior is undefined.
    ///
    /// @param action  the first input action
    /// @param action2 the second input action
    /// @return `true` if both actions are triggered by the same key or controller button; `false` otherwise
    public static boolean isBoundToSamePhysicalInput(@NotNull InputAction action, @NotNull InputAction action2) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER) {
            // Unassigned bindings share "no physical input", which is not the same physical input.
            final Optional<ControllerBinding> optionalControllerBinding = assignedControllerBinding(action);
            final Optional<ControllerBinding> optionalControllerBinding2 = assignedControllerBinding(action2);
            if (optionalControllerBinding.isPresent() && optionalControllerBinding2.isPresent()) {
                return optionalControllerBinding.get().isBoundToSamePhysicalInput(optionalControllerBinding2.get());
            }
        }

        final KeyMapping keyMapping1 = action.keyMapping();
        final KeyMapping keyMapping2 = action2.keyMapping();
        return keyMapping1.key == keyMapping2.key;
    }

    /// Retrieves the current input state for the current player (client-side).
    ///
    /// You should use this method instead of depending on the vanilla [Input] directly support controllers.
    ///
    /// The [PlayerInputState] is immutable, so properties cannot be updated directly, for that,
    /// use [InputManager#setInputState].
    ///
    /// **Note:** [InputMode#MIXED] is currently unsupported and its behavior is undefined.
    ///
    /// @param vanillaInput the Minecraft vanilla [Input] which will be mapped to a [PlayerInputState];
    /// @return an immutable [PlayerInputState] representing the current input state.
    /// @see InputManager#setInputState
    @NotNull
    public static PlayerInputState getInputState(@NotNull Input vanillaInput) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER) {
            return controllerMod.getInputState();
        }

        return PlayerInputState.fromVanillaInput(vanillaInput);
    }

    /// Convenience overload of [#getInputState(Input)] that requires the full [LocalPlayer],
    /// which is needed to read the vanilla [Input] used for non-controller inputs.
    ///
    /// @param localPlayer the player whose vanilla [Input] will be read; ignored when using a controller.
    /// @return an immutable [PlayerInputState] representing the current input state.
    @NotNull
    public static PlayerInputState getInputState(@NotNull LocalPlayer localPlayer) {
        return getInputState(localPlayer.input);
    }

    /// Updates the current input state for the current player (client-side).
    /// Consider using this instead of modifying fields in the vanilla [Input] directly
    /// to avoid direct dependency on Minecraft.
    ///
    /// @param inputState the updated input state.
    /// @see InputManager#getInputState
    public static void setInputState(@NotNull PlayerInputState inputState) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            final Input input = player.input;
            PlayerInputState.applyToVanillaInput(inputState, input);
        }
    }

    /// Checks whether the vanilla [KeyMapping] is down.
    ///
    /// **Note:** This may report `false` if a Minecraft screen is open, so it respects
    /// Minecraft internals.
    /// The exact behavior varied from one Minecraft version to another.
    private static boolean isKeyDown(@NotNull KeyMapping keyMapping) {
        final boolean isDown = keyMapping.isDown();
        if (!isDown && keyMapping.key.getType() == InputConstants.Type.MOUSE) {
            return isPhysicalKeyDown(keyMapping);
        }
        return isDown;
    }

    /// Checks whether the physical key is actually pressed, regardless of Minecraft's internal state.
    ///
    /// This method does not respect any Minecraft behavior and may return `true` even
    /// if a screen is open, for example.
    @ApiStatus.Internal
    private static boolean isPhysicalKeyDown(@NotNull KeyMapping keyMapping) {
        final InputConstants.Key key = keyMapping.key;
        final int keyValue = key.getValue();

        if (keyValue == InputConstants.UNKNOWN.getValue()) {
            return false;
        }

        final long windowPointer = Minecraft.getInstance().getWindow().getWindow();

        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(windowPointer, keyValue) > 0;
        } else if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(windowPointer, keyValue) > 0;
        }
        return false;
    }
}
