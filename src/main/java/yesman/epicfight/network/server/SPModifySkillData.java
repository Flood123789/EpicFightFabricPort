package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.forgecompat.network.BufUtil;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPModifySkillData(SkillDataKey<?> dataKey, SkillSlot slot, Object value, int entityId) {
	public static SPModifySkillData fromBytes(FriendlyByteBuf buf) {
		SkillDataKey<?> datakey = BufUtil.readRegistryId(buf);
		SkillSlot slot = SkillSlot.ENUM_MANAGER.get(buf.readInt());
		int entityId = buf.readInt();
		Object value = datakey.readFromBuffer(buf);
		
		return new SPModifySkillData(datakey, slot, value, entityId);
	}
	
	@SuppressWarnings("unchecked")
	public static void toBytes(SPModifySkillData msg, FriendlyByteBuf buf) {
		BufUtil.writeRegistryId(buf, SkillDataKeys.REGISTRY.get(), msg.dataKey);
		buf.writeInt(msg.slot.universalOrdinal());
		buf.writeInt(msg.entityId);
		((SkillDataKey<Object>)msg.dataKey).writeToBuffer(buf, msg.value);
	}
	
	@SuppressWarnings("deprecation")
	public static void handle(SPModifySkillData msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		
		ctx.get().setPacketHandled(true);
	}
}
