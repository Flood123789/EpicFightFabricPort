package yesman.epicfight.forgecompat.client.event;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterParticleProvidersEvent extends Event {
	public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleProvider.Sprite<T> sprite) {
		ParticleFactoryRegistry.getInstance().register(type, sprites -> (ParticleProvider<T>) sprite);
	}

	public <T extends ParticleOptions, P extends ParticleProvider<T>> void registerSpriteSet(ParticleType<T> type, java.util.function.Function<net.minecraft.client.particle.SpriteSet, P> factory) {
		ParticleFactoryRegistry.getInstance().register(type, factory::apply);
	}

	public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
		ParticleFactoryRegistry.getInstance().register(type, provider);
	}
}
