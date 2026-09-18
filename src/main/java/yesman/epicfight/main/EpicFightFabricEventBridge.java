package yesman.epicfight.main;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.event.OnDatapackSyncEvent;
import yesman.epicfight.forgecompat.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingDeathEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEquipmentChangeEvent;
import yesman.epicfight.forgecompat.event.entity.player.PlayerEvent;
import yesman.epicfight.forgecompat.event.entity.player.PlayerInteractEvent;

/**
 * Maps Fabric's native lifecycle callbacks onto the Forge-shaped events used by
 * Epic Fight's shared gameplay code.
 *
 * <p>The bridge contains no combat rules of its own. A callback should only
 * translate arguments, post the corresponding event, and convert cancellation
 * back to Fabric's return type. This keeps platform plumbing separate from the
 * event subscribers in {@code yesman.epicfight.events}.</p>
 */
public final class EpicFightFabricEventBridge {
	private static boolean registered;

	private EpicFightFabricEventBridge() {
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;

		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			// Server players are initialized after construction by the dedicated player
			// lifecycle, which also establishes their initial stamina safely.
			if (!(entity instanceof ServerPlayer)) {
				MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level));
			}
		});

		ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) ->
			MinecraftForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(entity, slot, previous, current))
		);

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
			!MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, source))
		);

		EntityTrackingEvents.START_TRACKING.register((entity, player) ->
			MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, entity))
		);

		EntityTrackingEvents.STOP_TRACKING.register((entity, player) ->
			MinecraftForge.EVENT_BUS.post(new PlayerEvent.StopTracking(player, entity))
		);

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			EpicFightFabricInitializer.initializeServerPlayer(oldPlayer);
			EpicFightFabricInitializer.initializeServerPlayer(newPlayer);
			MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive));
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
			MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, origin.dimension(), destination.dimension()))
		);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EpicFightFabricInitializer.initializeServerPlayer(handler.player);
			MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(server.getPlayerList(), handler.player));
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, hand);
			return MinecraftForge.EVENT_BUS.post(event)
				? InteractionResultHolder.fail(player.getItemInHand(hand))
				: InteractionResultHolder.pass(player.getItemInHand(hand));
		});
	}
}
