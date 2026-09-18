package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.event.entity.living.LivingAttackEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingDamageEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingDeathEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEntityUseItemEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEquipmentChangeEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingHurtEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingKnockBackEvent;
import yesman.epicfight.forgecompat.event.entity.living.MobEffectEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributeSupplier;
import yesman.epicfight.forgecompat.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEvent;

@Mixin(value = LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow
	protected abstract void hurtArmor(DamageSource damageSource, float amount);

	@Inject(at = @At("HEAD"), method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V")
	private void epicfight$clientDeath(DamageSource damageSource, CallbackInfo info) {
		LivingEntity self = (LivingEntity)(Object)this;

		// Fabric's ServerLivingEntityEvents supplies the common/server event, but it
		// has no client counterpart. Without this, client animators never enter their
		// registered death motion and the extended corpse timer displays a frozen pose.
		if (self.level().isClientSide) {
			MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(self, damageSource));
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "<clinit>")
	private static void epicfight$staticInitialize(CallbackInfo callbackInfo) {
		LivingEntityPatch.initLivingEntityDataAccessor();
	}
	
	@Inject(at = @At(value = "TAIL"), method = "defineSynchedData()V", cancellable = true)
	protected void epicfight$defineSynchedData(CallbackInfo info) {
		LivingEntityPatch.createSyncedEntityData((LivingEntity)(Object)this);
	}
	
	@Inject(at = @At(value = "TAIL"), method = "blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V", cancellable = true)
	private void epicfight$blockUsingShield(LivingEntity p_21200_, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> opponentEntitypatch = EpicFightCapabilities.getEntityPatch(p_21200_, LivingEntityPatch.class);
		LivingEntityPatch<?> selfEntitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (opponentEntitypatch != null) {
			opponentEntitypatch.setLastAttackResult(AttackResult.blocked(0.0F));
			
			if (selfEntitypatch != null && opponentEntitypatch.getEpicFightDamageSource() != null) {
				opponentEntitypatch.onAttackBlocked(opponentEntitypatch.getEpicFightDamageSource(), selfEntitypatch);
			}
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "hurt", cancellable = true)
	private void epicfight$hurt(DamageSource damagesource, float amount, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.getEntity(), LivingEntityPatch.class);
		
		if (entitypatch != null) {
			if (info.getReturnValue()) {
				entitypatch.setLastAttackEntity(self);
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "push(Lnet/minecraft/world/entity/Entity;)V", cancellable = true)
	private void epicfight$push(Entity p_20293_, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (entitypatch != null && !entitypatch.canPush(p_20293_)) {
			info.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", cancellable = true)
	private void epicfight$getDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> info) {
		if (source instanceof EpicFightDamageSource epicFightDamageSource && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
			this.hurtArmor(source, amount);
			float armorNegationAmount = amount * Math.min(epicFightDamageSource.calculateArmorNegation() * 0.01F , 1.0F);
			float amountElse = amount - armorNegationAmount;
			LivingEntity self = (LivingEntity)((Object)this);
			amountElse = CombatRules.getDamageAfterAbsorb(amountElse, (float)self.getArmorValue(), (float)self.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
			info.setReturnValue(armorNegationAmount + amountElse);
			info.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private void epicfight$readAdditionalSaveData(CompoundTag compTag, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (entitypatch != null) {
			entitypatch.initAttributesFromCompound(compTag);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private void epicfight$addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (entitypatch != null) {
			entitypatch.saveData(compoundTag);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V")
	private void epicfight$constructor(EntityType<?> entityType, Level level, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, HurtableEntityPatch.class).ifPresent((entitypatch) -> {
			EpicFightAttributeSupplier.ensureEpicFightAttributes(self);
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "hurt", cancellable = true)
	private void epicfight$livingAttackEvent(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity)(Object)this;
		LivingAttackEvent event = new LivingAttackEvent(self, damageSource, amount);

		if (MinecraftForge.EVENT_BUS.post(event)) {
			info.setReturnValue(false);
		}
	}

	@ModifyVariable(method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float epicfight$livingHurtEvent(float amount, DamageSource damageSource) {
		LivingEntity self = (LivingEntity)(Object)this;
		this.epicfight$currentDamageSource = damageSource;
		if (self.level().isClientSide()) {
			return amount;
		}

		LivingHurtEvent event = new LivingHurtEvent(self, damageSource, amount);
		return MinecraftForge.EVENT_BUS.post(event) ? 0.0F : event.getAmount();
	}

	@ModifyArg(
		method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
		index = 0
	)
	private float epicfight$livingDamageEvent(float newHealth) {
		LivingEntity self = (LivingEntity)(Object)this;
		if (self.level().isClientSide()) {
			return newHealth;
		}

		DamageSource damageSource = this.epicfight$currentDamageSource;
		if (damageSource == null) {
			return newHealth;
		}

		LivingDamageEvent event = new LivingDamageEvent(self, damageSource, self.getHealth() - newHealth);
		return MinecraftForge.EVENT_BUS.post(event) ? self.getHealth() : self.getHealth() - event.getAmount();
	}

	@org.spongepowered.asm.mixin.Unique
	private DamageSource epicfight$currentDamageSource;

	@Inject(method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("RETURN"))
	private void epicfight$clearCurrentDamageSource(DamageSource damageSource, float amount, CallbackInfo info) {
		this.epicfight$currentDamageSource = null;
	}

	@Inject(method = "knockback(DDD)V", at = @At("HEAD"), cancellable = true)
	private void epicfight$livingKnockBackEvent(double strength, double ratioX, double ratioZ, CallbackInfo info) {
		LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity)(Object)this, (float)strength, ratioX, ratioZ);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			info.cancel();
		}
	}

	@Inject(method = "jumpFromGround()V", at = @At("TAIL"))
	private void epicfight$livingJumpEvent(CallbackInfo info) {
		MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity)(Object)this));
	}

	@Inject(method = "onEquipItem(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
	private void epicfight$clientEquipmentChangeEvent(EquipmentSlot slot, ItemStack from, ItemStack to, CallbackInfo info) {
		LivingEntity self = (LivingEntity)(Object)this;
		if (self.level().isClientSide()) {
			MinecraftForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(self, slot, from, to));
		}
	}

	@Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
	private void epicfight$mobEffectAdded(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValue()) {
			MinecraftForge.EVENT_BUS.post(new MobEffectEvent.Added((LivingEntity)(Object)this, effect, null));
		}
	}

	@Inject(method = "removeEffectNoUpdate(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;", at = @At("RETURN"))
	private void epicfight$mobEffectRemoved(MobEffect effect, CallbackInfoReturnable<MobEffectInstance> info) {
		if (info.getReturnValue() != null) {
			MinecraftForge.EVENT_BUS.post(new MobEffectEvent.Remove((LivingEntity)(Object)this, info.getReturnValue()));
		}
	}

	@Inject(method = "startUsingItem(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
	private void epicfight$itemUseStart(InteractionHand hand, CallbackInfo info) {
		LivingEntity self = (LivingEntity)(Object)this;
		ItemStack item = self.getItemInHand(hand);
		LivingEntityUseItemEvent.Start event = new LivingEntityUseItemEvent.Start(self, item, item.getUseDuration());
		if (MinecraftForge.EVENT_BUS.post(event)) {
			info.cancel();
		}
	}

	@Inject(method = "stopUsingItem()V", at = @At("HEAD"), cancellable = true)
	private void epicfight$itemUseStop(CallbackInfo info) {
		LivingEntity self = (LivingEntity)(Object)this;
		LivingEntityUseItemEvent.Stop event = new LivingEntityUseItemEvent.Stop(self, self.getUseItem(), self.getUseItemRemainingTicks());
		if (MinecraftForge.EVENT_BUS.post(event)) {
			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	private void epicfight$runFabricLivingLifecycle(CallbackInfo info) {
		LivingEntity self = (LivingEntity)(Object)this;
		// Players are driven by Fabric's dedicated client/server tick callbacks. Running
		// their animation state from LivingEntity#tick happens before keyboard input and
		// can leave movement (including sneak) locked for the rest of the tick.
		if (self instanceof Player) {
			return;
		}

		EpicFightCapabilities.getUnparameterizedEntityPatch(self, HurtableEntityPatch.class).ifPresent(entitypatch -> {
			EpicFightAttributeSupplier.ensureEpicFightAttributes(self);

			if (!entitypatch.isInitialized()) {
				entitypatch.onJoinWorld(self, new EntityJoinLevelEvent(self, self.level()));
			}

			entitypatch.tick(new LivingEvent.LivingTickEvent(self));
		});
	}
	
	@Inject(at = @At(value = "TAIL"), method = "setAbsorptionAmount(F)V", cancellable = true)
	private void epicfight$setAbsorptionAmount(float absorptionAmount, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		
		if (!self.level().isClientSide()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAbsorption(self.getId(), absorptionAmount), self);
		}
	}
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
		),
		method = "jumpFromGround()V"
	)
	private float epicfight$jumpFromGround(LivingEntity livingEntity) {
		if (livingEntity instanceof Player player && player.isLocalPlayer()) {
            EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
            return cameraApi.isTPSMode() ? cameraApi.getCameraYRot() : livingEntity.getYRot();
        }

        return livingEntity.getYRot();
	}
	
	@Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F",
            ordinal = 0
        ),
        method = "tick()V"
    )
    private float epicfight$tick(LivingEntity livingEntity) {
		// returns the basis y rotation as camera in TPS mode
		if (livingEntity instanceof Player player && player.isLocalPlayer()) {
			return EpicFightCameraAPI.getInstance().getYRotForHead(player);
		}
		
		return livingEntity.getYRot();
    }
	
	@Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
        ),
        method = "tickHeadTurn(FF)F"
    )
	protected float epicfight$tickHeadTurn(LivingEntity livingEntity) {
		// returns the basis y rotation as camera in TPS mode
		if (livingEntity instanceof Player player && player.isLocalPlayer()) {
			return EpicFightCameraAPI.getInstance().getYRotForHead(player);
		}
		
		return livingEntity.getYRot();
	}
}
