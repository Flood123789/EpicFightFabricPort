package yesman.epicfight.forgecompat.registries;

import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.event.lifecycle.IModBusEvent;

public class RegisterEvent extends Event implements IModBusEvent {
	private final ResourceKey<? extends Registry<?>> registryKey;

	public RegisterEvent(ResourceKey<? extends Registry<?>> registryKey) {
		this.registryKey = registryKey;
	}

	public ResourceKey<? extends Registry<?>> getRegistryKey() {
		return this.registryKey;
	}

	@SuppressWarnings("unchecked")
	public <T> void register(ResourceKey<? extends Registry<T>> key, Consumer<RegisterHelper<T>> consumer) {
		if (this.registryKey.equals(key) && consumer != null) {
			consumer.accept(new RegisterHelper<T>() {
				@Override
				public void register(ResourceLocation name, T object) {
					// Register callback handling if needed
				}
			});
		}
	}

	public interface RegisterHelper<T> {
		void register(ResourceLocation name, T object);
	}
}
