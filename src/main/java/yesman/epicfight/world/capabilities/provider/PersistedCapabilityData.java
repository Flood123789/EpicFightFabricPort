package yesman.epicfight.world.capabilities.provider;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.forgecompat.common.capabilities.ICapabilitySerializable;

/**
 * Carries the serialized {@link ICapabilitySerializable} providers of a player between
 * {@link Player#readAdditionalSaveData} and the tick where the server player patch is initialized.
 *
 * <p>Forge deserializes attached capabilities inside {@code Entity#load}. On Fabric the patch that owns
 * {@link yesman.epicfight.world.capabilities.skill.CapabilitySkill} may still be re-attached after the player
 * NBT was read (see {@code EpicFightFabricInitializer#initializeServerPlayer}), which would drop anything
 * restored that early. So the tag is parked on the player and applied once the patch is final.</p>
 *
 * <p>Implemented on {@link Player} by {@code MixinPlayer}.</p>
 */
public interface PersistedCapabilityData {
	/** Same key Forge writes its capability data under, so Forge-made player files keep working. */
	String NBT_KEY = "ForgeCaps";

	@Nullable
	CompoundTag epicfight$getPersistedCapabilities();

	void epicfight$setPersistedCapabilities(@Nullable CompoundTag compoundTag);

	/**
	 * Reads the parked tag, if any, into the capabilities currently attached to @param player.
	 * Must run before the skill capability is synchronized to the client.
	 */
	static void restore(Player player) {
		if (player instanceof PersistedCapabilityData holder) {
			CompoundTag persisted = holder.epicfight$getPersistedCapabilities();

			if (persisted != null) {
				CapabilityManager.deserializeCapabilities(player, persisted);
			}
		}
	}

	/**
	 * Parks the {@link #NBT_KEY} tag of @param compoundTag on @param player, if it has one.
	 */
	static void park(Player player, CompoundTag compoundTag) {
		if (player instanceof PersistedCapabilityData holder && compoundTag.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
			holder.epicfight$setPersistedCapabilities(compoundTag.getCompound(NBT_KEY));
		}
	}
}
