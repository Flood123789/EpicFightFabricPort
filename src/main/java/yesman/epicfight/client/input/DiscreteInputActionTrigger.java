package yesman.epicfight.client.input;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;
import yesman.epicfight.api.client.input.DiscreteActionHandler;
import yesman.epicfight.api.client.input.InputManager;

/// Handles triggering of a discrete (one-time) [InputAction]
/// based on the current input state.
///
/// Consumers of this API provide only the "what to do" for each action;
/// this class determines the "when" to trigger it.
///
/// Internally, it supports both vanilla keyboard/mouse input and third-party controllers.
///
/// **Note:** This is an internal API.
/// Consumers should prefer using higher-level components
/// such as [InputManager] unless direct access is truly required.
@ApiStatus.Internal
public final class DiscreteInputActionTrigger {
    private static final Map<InputAction, Boolean> PREVIOUS_ACTIVE = new IdentityHashMap<>();

    private DiscreteInputActionTrigger() {
    }

    @Nullable
    private static IEpicFightControllerMod getControllerModApi() {
        return EpicFightControllerModProvider.get();
    }

    /// Called on every client tick to potentially trigger the provided callback for a given input action.
    ///
    /// Determines **when** to trigger the action; consumers define **how** it executes.
    /// For example, for [EpicFightInputAction#OPEN_SKILL_SCREEN], this method decides when to call
    /// the callback that opens the screen, but not how the screen is opened.
    ///
    /// Consumers do not need to know any keyboard/mouse or controller input internals.
    ///
    /// @param action  The input action to monitor.
    /// @param handler The callback to run when the action triggers.
    public static void triggerOnPress(@NotNull InputAction action, @NotNull DiscreteActionHandler handler) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        final KeyMapping keyMapping = action.keyMapping();
        if (controllerMod == null) {
            handleKeyboardAndMouse(action, keyMapping, handler);
            return;
        }

        // An action with no control assigned on the controller keeps its keyboard behaviour, so
        // plugging in a controller cannot disable the keys that have no controller default.
        switch (controllerMod.getInputMode()) {
            case MIXED -> InputManager.assignedControllerBinding(action)
                    .ifPresentOrElse(
                            controllerBinding -> {
                                final boolean handled = handleController(controllerBinding, handler);
                                if (!handled) {
                                    handleKeyboardAndMouse(action, keyMapping, handler);
                                }
                            },
                            () -> handleKeyboardAndMouse(action, keyMapping, handler)
                    );
            case CONTROLLER -> InputManager.assignedControllerBinding(action)
                    .ifPresentOrElse(
                            controllerBinding -> handleController(controllerBinding, handler),
                            () -> handleKeyboardAndMouse(action, keyMapping, handler)
                    );
            case KEYBOARD_MOUSE -> handleKeyboardAndMouse(action, keyMapping, handler);
        }
    }

    private static void handleKeyboardAndMouse(@NotNull InputAction action, @NotNull KeyMapping keyMapping, @NotNull DiscreteActionHandler handler) {
        boolean handled = false;
        while (keyMapping.consumeClick()) {
            handled = true;
            handler.onAction(createContext(false));
        }

        // KeyMapping#isDown is not updated reliably when several mods bind the same
        // physical key. Use the physical state for discrete actions so mode switching,
        // lock-on, and menus still receive a clean rising-edge event under conflicts.
        final boolean active = InputManager.isActionPhysicallyActive(action);
        final boolean previous = PREVIOUS_ACTIVE.getOrDefault(action, false);
        if (!handled && active && !previous) {
            handler.onAction(createContext(false));
        }

        PREVIOUS_ACTIVE.put(action, active);
    }

    private static boolean handleController(@NotNull ControllerBinding controllerBinding, @NotNull DiscreteActionHandler handler) {
        if (controllerBinding.isDigitalJustPressed()) {
            handler.onAction(createContext(true));
            return true;
        }
        return false;
    }

    @NotNull
    private static DiscreteActionHandler.Context createContext(boolean triggeredByController) {
        return new DiscreteActionHandler.Context(triggeredByController);
    }
}
