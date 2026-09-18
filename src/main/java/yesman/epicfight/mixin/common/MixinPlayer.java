package yesman.epicfight.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.compat.bettercombat.BetterCombatCompat;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.provider.PersistedCapabilityData;

@Mixin(value = Player.class, priority = 1500)
public abstract class MixinPlayer implements PersistedCapabilityData {
	/**
	 * Player NBT read before the server player patch is final. See {@link PersistedCapabilityData}.
	 */
	@Unique
	@Nullable
	private CompoundTag epicfight$persistedCapabilities;

	@Override
	@Nullable
	public CompoundTag epicfight$getPersistedCapabilities() {
		return this.epicfight$persistedCapabilities;
	}

	@Override
	public void epicfight$setPersistedCapabilities(@Nullable CompoundTag compoundTag) {
		this.epicfight$persistedCapabilities = compoundTag;
	}

	/**
	 * Forge serializes attached capabilities as part of the entity save; Fabric has no such step, so
	 * the player's skills, learned skill list, and Epic Fight mode were rebuilt from scratch on every
	 * login. Written at RETURN so vanilla data is already in the compound.
	 */
	@Inject(at = @At("RETURN"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private void epicfight$addCapabilityData(CompoundTag compoundTag, CallbackInfo info) {
		Player self = (Player)(Object)this;

		if (self.level().isClientSide()) {
			return;
		}

		CompoundTag capabilities = CapabilityManager.serializeCapabilities(self);

		if (!capabilities.isEmpty()) {
			compoundTag.put(PersistedCapabilityData.NBT_KEY, capabilities);
		} else if (this.epicfight$persistedCapabilities != null) {
			// Nothing is attached yet (the patch failed or the player never finished joining).
			// Write back what was loaded instead of dropping the player's skills.
			compoundTag.put(PersistedCapabilityData.NBT_KEY, this.epicfight$persistedCapabilities);
		}
	}

	@Inject(at = @At("RETURN"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private void epicfight$readCapabilityData(CompoundTag compoundTag, CallbackInfo info) {
		PersistedCapabilityData.park((Player)(Object)this, compoundTag);
	}

	@Inject(at = @At("HEAD"), method = "getItemBySlot", cancellable = true)
	private void epicfight$useActualOffhandInEpicFightMode(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> callbackInfo) {
		Player player = (Player)(Object)this;

		if (slot == EquipmentSlot.OFFHAND && BetterCombatCompat.isEpicFightMode(player)) {
			callbackInfo.setReturnValue(player.getInventory().offhand.get(0));
		}
	}

	@Inject(at = @At(value = "TAIL"), method = "<clinit>")
	private static void epicfight$staticInitialize(CallbackInfo callbackInfo) {
		PlayerPatch.initPlayerDataAccessor();
	}
	
	@Inject(at = @At(value = "TAIL"), method = "defineSynchedData()V", cancellable = true)
	protected void epicfight$defineSynchedData(CallbackInfo info) {
		PlayerPatch.createSyncedEntityData((Player)(Object)this);
	}
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"
		),
		method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
	)
	private void epicfight$recordDamage(CombatTracker self, DamageSource damagesource, float damage) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null) {
			entitypatch.setLastAttackEntity(self.mob);
		}
		
		self.recordDamage(damagesource, damage);
	}
	
	@Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
        ),
        method = "serverAiStep()V"
    )
    private float epicfight$serverAiStep(Player player) {
		if (player.isLocalPlayer()) {
			return EpicFightCameraAPI.getInstance().getYRotForHead(player);
		}
		
		return player.getYRot();
    }
}
