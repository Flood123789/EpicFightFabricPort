package yesman.epicfight.forgecompat.client.event;

import java.io.IOException;
import java.util.function.Consumer;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterShadersEvent extends Event {
	private final ResourceProvider resourceProvider;

	public RegisterShadersEvent(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}

	public ResourceProvider getResourceProvider() {
		return this.resourceProvider;
	}

	public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> consumer) throws IOException {
		consumer.accept(shaderInstance);
	}
}
