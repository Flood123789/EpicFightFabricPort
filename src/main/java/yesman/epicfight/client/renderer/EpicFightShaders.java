package yesman.epicfight.client.renderer;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.client.event.RegisterShadersEvent;
import yesman.epicfight.forgecompat.eventbus.api.SubscribeEvent;
import yesman.epicfight.forgecompat.fml.common.Mod;
import yesman.epicfight.forgecompat.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class EpicFightShaders {
	public static ShaderInstance positionColorNormalShader;
	
	@Nullable
	public static ShaderInstance getPositionColorNormalShader() {
		return positionColorNormalShader;
	}
	
	@SubscribeEvent
	public static void registerShadersEvent(RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceProvider(), EpicFightMod.prefix("solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL), reloadedShader -> {
			positionColorNormalShader = reloadedShader;
		});
	}
}
