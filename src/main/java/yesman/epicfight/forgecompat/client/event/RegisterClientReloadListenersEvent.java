package yesman.epicfight.forgecompat.client.event;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterClientReloadListenersEvent extends Event {
	public void registerReloadListener(PreparableReloadListener listener) {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener() {
			@Override
			public net.minecraft.resources.ResourceLocation getFabricId() {
				return new net.minecraft.resources.ResourceLocation("epicfight", "client_reload_" + listener.getClass().getSimpleName().toLowerCase());
			}

			@Override
			public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller preparationsProfiler, net.minecraft.util.profiling.ProfilerFiller reloadProfiler, java.util.concurrent.Executor backgroundExecutor, java.util.concurrent.Executor gameExecutor) {
				return listener.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
			}
		});
	}
}
