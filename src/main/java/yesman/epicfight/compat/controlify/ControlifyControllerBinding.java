package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.controller.ControllerBinding;

@ApiStatus.Internal
public record ControlifyControllerBinding(@NotNull InputBinding inputBinding) implements ControllerBinding {

    @Override
    @NotNull
    public ResourceLocation id() {
        return inputBinding.id();
    }

    @Override
    public boolean isBound() {
        // Controlify models "unbound" as an input that reports no physical controls.
        return !inputBinding.boundInput().getRelevantInputs().isEmpty();
    }

    @Override
    public boolean isDigitalActiveNow() {
        return inputBinding.digitalNow();
    }

    @Override
    public boolean wasDigitalActivePreviously() {
        return inputBinding.digitalPrev();
    }

    @Override
    public boolean isDigitalJustPressed() {
        return inputBinding.justPressed();
    }

    @Override
    public boolean isDigitalJustReleased() {
        return inputBinding.justReleased();
    }

    @Override
    public float getAnalogueNow() {
        return inputBinding.analogueNow();
    }

    @Override
    public void emulatePress() {
        inputBinding.fakePress();
    }

    @Override
    @NotNull
    public Object physicalInputId() {
        return inputBinding.boundInput().getRelevantInputs();
    }
}
