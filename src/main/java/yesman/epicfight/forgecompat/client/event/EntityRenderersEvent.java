package yesman.epicfight.forgecompat.client.event;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class EntityRenderersEvent extends Event {
	public static class RegisterRenderers extends EntityRenderersEvent {
		public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> provider) {
			EntityRendererRegistry.register(entityType, provider);
		}

		public <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<? extends T> blockEntityType, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T> provider) {
			net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(blockEntityType, provider);
		}
	}

	public static class RegisterLayerDefinitions extends EntityRenderersEvent {
		public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
			// Layer definitions in Fabric can be registered via EntityModelLayerRegistry
		}
	}

	public static class AddLayers extends EntityRenderersEvent {
		private final EntityRendererProvider.Context context;

		public AddLayers(EntityRendererProvider.Context context) {
			this.context = context;
		}

		public EntityRendererProvider.Context getContext() {
			return this.context;
		}
	}
}
