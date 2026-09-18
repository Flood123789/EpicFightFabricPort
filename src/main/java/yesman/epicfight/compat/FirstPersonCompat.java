package yesman.epicfight.compat;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.api.distmarker.OnlyIn;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.forgecompat.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FirstPersonCompat implements ICompatModule {
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> {
			FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
				public boolean preventFirstperson() {
					PlayerPatch<?> playerpatch = ClientEngine.getInstance().getPlayerPatch();
					
					if (playerpatch != null && playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT && ClientConfig.enableAnimatedFirstPersonModel) {
						return true;
					}
					
					return false;
				}
			});
		}));
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
}