package yesman.epicfight.compat;

import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.api.distmarker.OnlyIn;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.api.client.model.transformer.AzureArmorTransformer;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;

public class AzureLibArmorCompat implements ICompatModule {
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		HumanoidModelBaker.registerNewTransformer(new AzureArmorTransformer());
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
		eventBus.addListener(AzureArmorTransformer::getGeoArmorTexturePath);
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
}