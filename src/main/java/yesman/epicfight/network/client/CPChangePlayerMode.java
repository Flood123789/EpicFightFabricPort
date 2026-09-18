package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.main.EpicFightFabricInitializer;
import yesman.epicfight.main.EpicFightMod;

public class CPChangePlayerMode {
	private final PlayerPatch.PlayerMode mode;
	
	public CPChangePlayerMode(PlayerPatch.PlayerMode mode) {
		this.mode = mode;
	}

	public static CPChangePlayerMode fromBytes(FriendlyByteBuf buf) {
		return new CPChangePlayerMode(buf.readEnum(PlayerPatch.PlayerMode.class));
	}

	public static void toBytes(CPChangePlayerMode msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.mode);
	}
	
	public static void handle(CPChangePlayerMode msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerPatch playerpatch = EpicFightFabricInitializer.initializeServerPlayer(ctx.get().getSender());
			if (playerpatch == null) {
				EpicFightMod.LOGGER.error("Rejected Epic Fight mode change because the server player patch is unavailable");
				return;
			}

			playerpatch.toMode(msg.mode, false);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
				SPModifyPlayerData.setPlayerMode(playerpatch.getOriginal().getId(), playerpatch.getPlayerMode()),
				playerpatch.getOriginal()
			);
			EpicFightMod.LOGGER.info(
				"Server accepted Epic Fight mode {} for {} (stamina={}/{})",
				playerpatch.getPlayerMode(), playerpatch.getOriginal().getScoreboardName(), playerpatch.getStamina(), playerpatch.getMaxStamina()
			);
		});
		ctx.get().setPacketHandled(true);
	}
}
