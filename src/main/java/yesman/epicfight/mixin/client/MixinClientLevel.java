package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.ClientLevel;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.client.world.util.FakeLevel;

@Mixin(value = ClientLevel.class)
public abstract class MixinClientLevel {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"), require = 0)
    private boolean epicfight$init(IEventBus instance, Event e) {
        if (((ClientLevel)(Object)this) instanceof FakeLevel) {
            return false;
        }
        
        return instance.post(e);
    }
}
