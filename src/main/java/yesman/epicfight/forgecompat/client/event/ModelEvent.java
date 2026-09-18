package yesman.epicfight.forgecompat.client.event;

import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ModelEvent extends Event {
	public static class RegisterGeometryLoaders extends ModelEvent {
		public void register(String name, Object loader) {
		}
	}

	public static class ModifyBakingResult extends ModelEvent {
		private final Map<ResourceLocation, BakedModel> models;

		public ModifyBakingResult(Map<ResourceLocation, BakedModel> models) {
			this.models = models;
		}

		public Map<ResourceLocation, BakedModel> getModels() {
			return this.models;
		}

		public ModelBakeryShim getModelBakery() {
			return new ModelBakeryShim(this.models);
		}

		public static class ModelBakeryShim {
			private final Map<ResourceLocation, BakedModel> models;
			public ModelBakeryShim(Map<ResourceLocation, BakedModel> models) { this.models = models; }
			public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() { return this.models; }
		}
	}

	public static class RegisterAdditional extends ModelEvent {
		public void register(ModelResourceLocation location) {
		}
	}
}
