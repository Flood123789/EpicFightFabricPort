package yesman.epicfight.network.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.forgecompat.network.BufUtil;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.forgecompat.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public record SPSetRemotePlayerSkill(int entityId, SkillSlot slot, @Nullable Skill skill) {
	public static SPSetRemotePlayerSkill fromBytes(FriendlyByteBuf buf) {
		return new SPSetRemotePlayerSkill(buf.readInt(), SkillSlot.ENUM_MANAGER.get(buf.readInt()), buf.isReadable() ? BufUtil.readRegistryId(buf) : null);
	}

	public static void toBytes(SPSetRemotePlayerSkill msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId());
		buf.writeInt(msg.slot().universalOrdinal());
		
		if (msg.skill() != null) {
			BufUtil.writeRegistryId(buf, RegistryManager.ACTIVE.getRegistry(SkillManager.SKILL_REGISTRY_KEY), msg.skill);
		}
	}
	
	public static void handle(SPSetRemotePlayerSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
