package yesman.epicfight.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.api.distmarker.OnlyIn;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.forgecompat.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib.event.GeoRenderEvent;
import yesman.epicfight.api.client.model.transformer.GeoModelTransformer;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class GeckolibCompat implements ICompatModule {
	@Override
	@OnlyIn(Dist.CLIENT)
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> HumanoidModelBaker.registerNewTransformer(new GeoModelTransformer())));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void onForgeEventBusClient(IEventBus eventBus) {
		eventBus.addListener(GeoModelTransformer::getGeoArmorTexturePath);
		GeoRenderEvent.Entity.Pre.EVENT.register(this::geoEntityRenderPreEvent);
		GeoRenderEvent.Entity.Post.EVENT.register(this::geoEntityRenderPostEvent);
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean geoEntityRenderPreEvent(GeoRenderEvent.Entity.Pre event) {
		Entity entity = event.getEntity();
		
		if (entity.level() == null) {
			return true;
		}
		
		if (entity instanceof LivingEntity livingentity) {
			RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
			
			if (renderEngine.hasRendererFor(livingentity)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				LocalPlayerPatch playerpatch = null;
				float originalYRot = 0.0F;
				
				if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
					playerpatch = localPlayerPatch;
					originalYRot = playerpatch.getModelYRot();
					playerpatch.setModelYRotInGui(livingentity.getYRot());
					event.getPoseStack().translate(0, 0.1D, 0);
				}
				
				if (entitypatch != null && entitypatch.overrideRender()) {
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
					
					if (ClientEngine.getInstance().getPlayerPatch() != null && !renderEngine.minecraft.options.hideGui && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.level())) {
						for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
							if (entityIndicator.shouldDraw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPartialTick())) {
								entityIndicator.draw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPoseStack(), event.getBufferSource(), event.getPartialTick());
							}
						}
					}
				}
				
				if (playerpatch != null) {
					playerpatch.disableModelYRotInGui(originalYRot);
				}
				
				if (entitypatch != null && entitypatch.overrideRender()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void geoEntityRenderPostEvent(GeoRenderEvent.Entity.Post event) {
		Entity entity = event.getEntity();
		
		if (entity.level() == null) {
			return;
		}
		
		if (entity instanceof LivingEntity livingentity) {
			RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
			
			if (ClientEngine.getInstance().getPlayerPatch() != null && !renderEngine.minecraft.options.hideGui && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.level())) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				
				for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
					if (entityIndicator.shouldDraw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPartialTick())) {
						entityIndicator.draw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPoseStack(), event.getBufferSource(), event.getPartialTick());
					}
				}
			}
		}
	}
}
