package yesman.epicfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.exception.DatapackException;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

public final class ClientboundPacketHandlers {
	private ClientboundPacketHandlers() {
	}

	public static void handle(SPAbsorption msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity instanceof LivingEntity livingentity && !(entity instanceof Player)) {
			livingentity.setAbsorptionAmount(msg.amount);
		}
	}

	public static void handle(SPAddLearnedSkill msg) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null) {
			return;
		}

		PlayerPatch<?> playerpatch = (PlayerPatch<?>)minecraft.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

		if (playerpatch == null) {
			return;
		}

		for (String skillName : msg.skillNames) {
			playerpatch.getSkillCapability().addLearnedSkill(SkillManager.getSkill(skillName));
		}
	}

	public static <T> void handle(SPAnimationVariablePacket<T> msg) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(getEntity(msg.getEntityId()), LivingEntityPatch.class).ifPresent(msg::process);
	}

	public static void handle(SPAnimatorControl msg) {
		applyAnimatorControl(msg);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void handle(SPChangeGamerule msg) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.level == null) {
			return;
		}

		GameRules.Value ruleValue = minecraft.level.getGameRules().getRule(msg.gamerule.getRuleKey());
		msg.gamerule.getRuleType().setRule().accept(ruleValue, msg.value);
	}

	public static void handle(SPChangeLivingMotion msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity == null) {
			return;
		}

		if (entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof LivingEntityPatch<?> entitypatch) {
			ClientAnimator animator = entitypatch.getClientAnimator();
			animator.resetLivingAnimations();
			animator.offAllLayers();
			animator.resetMotion(false);
			animator.resetCompositeMotion();

			for (int i = 0; i < msg.count; i++) {
				entitypatch.getClientAnimator().addLivingAnimation(msg.motionList.get(i), msg.animationList.get(i));
			}

			if (msg.setChangesAsDefault) {
				animator.setCurrentMotionsAsDefault();
			}
		}
	}

	public static void handle(SPChangePlayerMode msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity != null) {
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

			if (playerpatch != null) {
				playerpatch.toMode(msg.mode, false);
			}
		}
	}

	public static void handle(SPChangeSkill msg) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(getEntity(msg.entityId()), PlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getSkill(msg.skillSlot()).setSkill(msg.skill());

			if (msg.skill() != null && msg.skillSlot().category().learnable()) {
				playerpatch.getSkillCapability().addLearnedSkill(msg.skill());
			}

			playerpatch.getSkill(msg.skillSlot()).setDisabled(false);
		});
	}

	public static void handle(SPClearSkills msg) {
		EpicFightCapabilities.getPlayerPatchAsOptional(getEntity(msg.entityId())).ifPresent(playerpatch -> {
			playerpatch.getSkillCapability().clearContainersAndLearnedSkills(playerpatch.getOriginal().isLocalPlayer());
		});
	}

	public static void handle(SPDatapackSync msg) {
		try {
			switch (msg.getType()) {
			case MOB -> MobPatchReloadListener.processServerPacket(msg);
			case SKILL_PARAMS -> SkillManager.processServerPacket(msg);
			case WEAPON, ARMOR -> ItemCapabilityReloadListener.processServerPacket(msg);
			case WEAPON_TYPE -> WeaponTypeReloadListener.processServerPacket(msg);
			case ITEM_KEYWORD -> ItemKeywordReloadListener.handleClientBoundSyncPacket(msg);
			case MANDATORY_RESOURCE_PACK_ANIMATION, RESOURCE_PACK_ANIMATION -> AnimationManager.getInstance().processServerPacket(msg, msg.getType() == SPDatapackSync.Type.MANDATORY_RESOURCE_PACK_ANIMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DatapackException(e.getMessage());
		}
	}

	public static void handle(SPEntityPairingPacket msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity != null) {
			EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

			if (entitypatch != null) {
				entitypatch.fireEntityPairingEvent(msg);
			}
		}
	}

	public static void handle(SPFracture msg) {
		LevelUtil.getInstance().handlePacket(msg);
	}

	public static void handle(SPInitSkills msg) {
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();

		if (playerpatch != null) {
			playerpatch.getSkillCapability().deserialize(msg.serializedSkill());
		}
	}

	public static void handle(SPModifyPlayerData msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity != null && entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof PlayerPatch<?> playerpatch) {
			switch (msg.packetType) {
			case SET_MODEL_YROT:
				playerpatch.setModelYRot((float)msg.data.get("yaw"), false);
				break;
			case YROT_TURN_OFF:
				playerpatch.disableModelYRot(false);
			case MODE:
				playerpatch.toMode((PlayerPatch.PlayerMode)msg.data.get("mode"), false);
				break;
			case LAST_ATTACK_RESULT:
				playerpatch.setLastAttackSuccess((boolean)msg.data.get("lastAttackSuccess"));
				break;
			case SET_GRAPPLE_TARGET:
				Entity grapplingTarget = getEntity((int)msg.data.get("grapplingTarget"));

				if (grapplingTarget instanceof LivingEntity livingentity) {
					playerpatch.setGrapplingTarget(livingentity);
				} else {
					playerpatch.setGrapplingTarget(null);
				}

				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void handle(SPModifySkillData msg) {
		Entity entity = getEntity(msg.entityId());

		if (entity != null && entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof PlayerPatch<?> playerpatch) {
			SkillDataManager dataManager = playerpatch.getSkill(msg.slot()).getDataManager();
			dataManager.setDataRawtype(msg.dataKey(), msg.value());
		}
	}

	public static void handle(SPMoveAndPlayAnimation msg) {
		applyAnimationAndTarget(msg);
		Entity entity = getEntity(msg.getEntityId());

		if (entity != null) {
			entity.setPos(msg.posX, msg.posY, msg.posZ);
			entity.setYRot(msg.yRot);
			entity.xo = entity.getX();
			entity.yo = entity.getY();
			entity.zo = entity.getZ();
			entity.xOld = entity.getX();
			entity.yOld = entity.getY();
			entity.zOld = entity.getZ();
			entity.yRotO = msg.yRot;
		}
	}

	public static void handle(SPPlayAnimationAndSetTarget msg) {
		applyAnimationAndTarget(msg);
	}

	public static void handle(SPPlayUISound msg) {
		ClientEngine.getInstance().playUISound(msg);
	}

	public static void handle(SPPotion msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity instanceof LivingEntity livingentity) {
			switch (msg.action) {
			case ACTIVATE -> livingentity.addEffect(msg.effectInstance);
			case REMOVE -> livingentity.removeEffect(msg.effectInstance.getEffect());
			}
		}
	}

	public static void handle(SPRemoveSkillAndLearn msg) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null) {
			return;
		}

		PlayerPatch<?> playerpatch = (PlayerPatch<?>)minecraft.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

		if (playerpatch != null) {
			playerpatch.getSkillCapability().removeLearnedSkill(msg.skill());
			SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot());

			if (skillContainer.getSkill() == msg.skill()) {
				skillContainer.setSkill(null);
			}
		}
	}

	public static void handle(SPSetAttackTarget msg) {
		Entity entity = getEntity(msg.entityId);
		Entity targetEntity = getEntity(msg.targetEntityId);

		if (entity instanceof Mob mob) {
			if (targetEntity instanceof LivingEntity livingentity) {
				mob.setTarget(livingentity);
			} else {
				mob.setTarget(null);
			}
		}
	}

	public static void handle(SPSetRemotePlayerSkill msg) {
		Entity entity = getEntity(msg.entityId());

		EpicFightCapabilities.getUnparameterizedEntityPatch(entity, AbstractClientPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getSkill(msg.slot()).setSkillRemote(msg.skill());
		});
	}

	public static void handle(SPSetSkillContainerValue msg) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(getEntity(msg.entityId()), PlayerPatch.class).ifPresent(playerpatch -> {
			SkillContainer container = playerpatch.getSkill(msg.skillSlot());

			switch (msg.target()) {
			case ENABLE -> container.setDisabled(msg.boolVal());
			case ACTIVATE -> { if (msg.boolVal()) container.activate(); else container.deactivate(); }
			case RESOURCE -> container.setResource(msg.floatVal());
			case DURATION -> container.setDuration((int)msg.floatVal());
			case MAX_DURATION -> container.setMaxDuration((int)msg.floatVal());
			case STACKS -> container.setStack((int)msg.floatVal());
			case MAX_RESOURCE -> container.setMaxResource(msg.floatVal());
			case REPLACE_COOLDOWN -> container.setReplaceCooldown((int)msg.floatVal());
			}
		});
	}

	public static void handle(SPSkillExecutionFeedback msg) {
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();

		if (playerpatch != null) {
			switch(msg.feedbackType) {
			case EXECUTED -> {
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
				skillContainer.getSkill().executeOnClient(skillContainer, msg.getBuffer());
			}
			case HOLDING_START -> {
				SkillContainer container = playerpatch.getSkill(msg.skillSlot);

				if (container.getSkill() instanceof HoldableSkill holdableSkill) {
					playerpatch.startSkillHolding(holdableSkill);
					ClientEngine.getInstance().controlEngine.setHoldingKey(container.getSlot(), holdableSkill.getKeyMapping());
				}
			}
			case EXPIRED -> {
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
				skillContainer.getSkill().cancelOnClient(skillContainer, msg.getBuffer());
			}
			}
		}
	}

	public static void handle(SPSyncAnimationPosition msg) {
		Entity entity = getEntity(msg.getEntityId());

		if (entity instanceof LivingEntity livingentity) {
			livingentity.lerpX = msg.getPosition().x;
			livingentity.lerpY = msg.getPosition().y;
			livingentity.lerpZ = msg.getPosition().z;
			livingentity.lerpSteps = msg.getLerpSteps();
		}
	}

	public static void handle(SPUpdatePlayerInput msg) {
		Entity entity = getEntity(msg.entityId);

		if (entity != null) {
			entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entitypatch -> {
				if (entitypatch instanceof PlayerPatch<?> playerpatch) {
					playerpatch.dx = msg.strafe;
					playerpatch.dz = msg.forward;
				}
			});
		}
	}

	private static void applyAnimationAndTarget(SPPlayAnimationAndSetTarget msg) {
		applyAnimatorControl(msg);
		Entity entity = getEntity(msg.getEntityId());
		Entity target = getEntity(msg.targetId);

		if (entity instanceof Mob mob && target instanceof LivingEntity livingentity) {
			mob.setTarget(livingentity);
		}
	}

	private static void applyAnimatorControl(SPAnimatorControl msg) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(getEntity(msg.getEntityId()), LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (msg.getAction() == SPAnimatorControl.Action.PLAY_CLIENT && msg.getLayer() != SPAnimatorControl.Layer.ANIMATION && msg.getPriority() != SPAnimatorControl.Priority.ANIMATION) {
				entitypatch.getClientAnimator().playAnimationAt(AnimationManager.byId(msg.getAnimationId()), msg.getTransitionTimeModifier(), msg.getLayer(), msg.getPriority());
			} else {
				msg.process(entitypatch);
			}
		});
	}

	private static Entity getEntity(int entityId) {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level == null ? null : minecraft.level.getEntity(entityId);
	}
}
