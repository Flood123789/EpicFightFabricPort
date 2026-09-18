package yesman.epicfight.network.server;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

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
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.forgecompat.common.capabilities.ICapabilityProvider;

public final class ClientboundPacketHandlers {
	private static final int SKILL_PACKET_RETRY_TICKS = 100;
	private static final int MAX_DEFERRED_CHANGE_SKILLS = 64;
	private static final int MAX_DEFERRED_MODIFY_SKILL_DATA = 128;
	private static final int MAX_DEFERRED_SKILL_CONTAINER_VALUES = 128;
	private static final Queue<DeferredInitSkills> DEFERRED_INIT_SKILLS = new ArrayDeque<>();
	private static final Queue<DeferredChangeSkill> DEFERRED_CHANGE_SKILLS = new ArrayDeque<>();
	private static final Queue<DeferredModifySkillData> DEFERRED_MODIFY_SKILL_DATA = new ArrayDeque<>();
	private static final Queue<DeferredSkillContainerValue> DEFERRED_SKILL_CONTAINER_VALUES = new ArrayDeque<>();

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

		PlayerPatch<?> playerpatch = (PlayerPatch<?>)ICapabilityProvider.getCapability(minecraft.player, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

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

		if (ICapabilityProvider.getCapability(entity, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof LivingEntityPatch<?> entitypatch) {
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
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)ICapabilityProvider.getCapability(entity, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

			if (playerpatch != null) {
				playerpatch.toMode(msg.mode, false);
			}
		}
	}

	public static void handle(SPChangeSkill msg) {
		if (!applyChangeSkill(msg)) {
			deferChangeSkill(msg);
		}
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
			EntityPatch<?> entitypatch = ICapabilityProvider.getCapability(entity, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

			if (entitypatch != null) {
				entitypatch.fireEntityPairingEvent(msg);
			}
		}
	}

	public static void handle(SPFracture msg) {
		LevelUtil.getInstance().handlePacket(msg);
	}

	public static void handle(SPInitSkills msg) {
		if (!applyInitSkills(msg)) {
			deferInitSkills(msg);
		}
	}

	public static void handle(SPModifyPlayerData msg) {
		Entity entity = getEntity(msg.entityId);
		EpicFightMod.LOGGER.debug(
			"[EF-DIAG] received player-data packet type={} entityId={} entityPresent={}",
			msg.packetType, msg.entityId, entity != null
		);

		if (entity != null && ICapabilityProvider.getCapability(entity, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof PlayerPatch<?> playerpatch) {
			PlayerPatch.PlayerMode beforeMode = playerpatch.getPlayerMode();
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
			EpicFightMod.LOGGER.debug(
				"[EF-DIAG] applied player-data packet type={} entityId={} patch={} mode={}->{} stamina={}/{}",
				msg.packetType, msg.entityId, Integer.toHexString(System.identityHashCode(playerpatch)),
				beforeMode, playerpatch.getPlayerMode(), playerpatch.getStamina(), playerpatch.getMaxStamina()
			);
		} else {
			EpicFightMod.LOGGER.error(
				"[EF-DIAG] could not apply player-data packet type={} entityId={} because entity/PlayerPatch was unavailable",
				msg.packetType, msg.entityId
			);
		}
	}

	@SuppressWarnings("deprecation")
	public static void handle(SPModifySkillData msg) {
		if (!applyModifySkillData(msg)) {
			deferModifySkillData(msg);
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

		PlayerPatch<?> playerpatch = (PlayerPatch<?>)ICapabilityProvider.getCapability(minecraft.player, EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

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
		if (!applySkillContainerValue(msg)) {
			deferSkillContainerValue(msg);
		}
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
			ICapabilityProvider.getCapability(entity, EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entitypatch -> {
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

	public static void flushDeferredSkillPackets() {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.level == null) {
			DEFERRED_INIT_SKILLS.clear();
			DEFERRED_CHANGE_SKILLS.clear();
			DEFERRED_MODIFY_SKILL_DATA.clear();
			DEFERRED_SKILL_CONTAINER_VALUES.clear();
			return;
		}

		flushDeferredInitSkills();
		flushDeferredChangeSkills();
		flushDeferredModifySkillData();
		flushDeferredSkillContainerValues();
	}

	private static void flushDeferredInitSkills() {
		Iterator<DeferredInitSkills> iterator = DEFERRED_INIT_SKILLS.iterator();

		while (iterator.hasNext()) {
			DeferredInitSkills deferred = iterator.next();

			if (applyInitSkills(deferred.packet) || deferred.tickExpired()) {
				iterator.remove();
			}
		}
	}

	private static void flushDeferredSkillContainerValues() {
		Iterator<DeferredSkillContainerValue> iterator = DEFERRED_SKILL_CONTAINER_VALUES.iterator();

		while (iterator.hasNext()) {
			DeferredSkillContainerValue deferred = iterator.next();

			if (applySkillContainerValue(deferred.packet) || deferred.tickExpired()) {
				iterator.remove();
			}
		}
	}

	private static void flushDeferredChangeSkills() {
		Iterator<DeferredChangeSkill> iterator = DEFERRED_CHANGE_SKILLS.iterator();

		while (iterator.hasNext()) {
			DeferredChangeSkill deferred = iterator.next();

			if (applyChangeSkill(deferred.packet) || deferred.tickExpired()) {
				iterator.remove();
			}
		}
	}

	private static void flushDeferredModifySkillData() {
		Iterator<DeferredModifySkillData> iterator = DEFERRED_MODIFY_SKILL_DATA.iterator();

		while (iterator.hasNext()) {
			DeferredModifySkillData deferred = iterator.next();

			if (applyModifySkillData(deferred.packet) || deferred.tickExpired()) {
				iterator.remove();
			}
		}
	}

	private static boolean applyInitSkills(SPInitSkills msg) {
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();

		if (playerpatch == null || playerpatch.getSkillCapability() == CapabilitySkill.EMPTY) {
			return false;
		}

		playerpatch.getSkillCapability().deserialize(msg.serializedSkill());
		return true;
	}

	private static boolean applyChangeSkill(SPChangeSkill msg) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(getEntity(msg.entityId()), PlayerPatch.class);

		if (playerpatch == null || playerpatch.getSkillCapability() == CapabilitySkill.EMPTY) {
			return false;
		}

		SkillContainer container = playerpatch.getSkill(msg.skillSlot());

		if (container == null) {
			return false;
		}

		container.setSkill(msg.skill());

		if (msg.skill() != null && msg.skillSlot().category().learnable()) {
			playerpatch.getSkillCapability().addLearnedSkill(msg.skill());
		}

		container.setDisabled(false);
		return true;
	}

	@SuppressWarnings("deprecation")
	private static boolean applyModifySkillData(SPModifySkillData msg) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(getEntity(msg.entityId()), PlayerPatch.class);

		if (playerpatch == null || playerpatch.getSkillCapability() == CapabilitySkill.EMPTY) {
			return false;
		}

		SkillContainer container = playerpatch.getSkill(msg.slot());

		if (container == null) {
			return false;
		}

		SkillDataManager dataManager = container.getDataManager();

		if (!dataManager.hasData(msg.dataKey())) {
			return false;
		}

		dataManager.setDataRawtype(msg.dataKey(), msg.value());
		return true;
	}

	private static boolean applySkillContainerValue(SPSetSkillContainerValue msg) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(getEntity(msg.entityId()), PlayerPatch.class);

		if (playerpatch == null || playerpatch.getSkillCapability() == CapabilitySkill.EMPTY) {
			return false;
		}

		SkillContainer container = playerpatch.getSkill(msg.skillSlot());

		if (container == null) {
			return false;
		}

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

		return true;
	}

	private static void deferInitSkills(SPInitSkills packet) {
		DEFERRED_INIT_SKILLS.clear();
		DEFERRED_INIT_SKILLS.add(new DeferredInitSkills(packet));
	}

	private static void deferChangeSkill(SPChangeSkill packet) {
		if (DEFERRED_CHANGE_SKILLS.size() >= MAX_DEFERRED_CHANGE_SKILLS) {
			DEFERRED_CHANGE_SKILLS.poll();
		}

		DEFERRED_CHANGE_SKILLS.add(new DeferredChangeSkill(packet));
	}

	private static void deferModifySkillData(SPModifySkillData packet) {
		if (DEFERRED_MODIFY_SKILL_DATA.size() >= MAX_DEFERRED_MODIFY_SKILL_DATA) {
			DEFERRED_MODIFY_SKILL_DATA.poll();
		}

		DEFERRED_MODIFY_SKILL_DATA.add(new DeferredModifySkillData(packet));
	}

	private static void deferSkillContainerValue(SPSetSkillContainerValue packet) {
		if (DEFERRED_SKILL_CONTAINER_VALUES.size() >= MAX_DEFERRED_SKILL_CONTAINER_VALUES) {
			DEFERRED_SKILL_CONTAINER_VALUES.poll();
		}

		DEFERRED_SKILL_CONTAINER_VALUES.add(new DeferredSkillContainerValue(packet));
	}

	private static final class DeferredInitSkills {
		private final SPInitSkills packet;
		private int remainingTicks = SKILL_PACKET_RETRY_TICKS;

		private DeferredInitSkills(SPInitSkills packet) {
			this.packet = packet;
		}

		private boolean tickExpired() {
			return --this.remainingTicks <= 0;
		}
	}

	private static final class DeferredChangeSkill {
		private final SPChangeSkill packet;
		private int remainingTicks = SKILL_PACKET_RETRY_TICKS;

		private DeferredChangeSkill(SPChangeSkill packet) {
			this.packet = packet;
		}

		private boolean tickExpired() {
			return --this.remainingTicks <= 0;
		}
	}

	private static final class DeferredModifySkillData {
		private final SPModifySkillData packet;
		private int remainingTicks = SKILL_PACKET_RETRY_TICKS;

		private DeferredModifySkillData(SPModifySkillData packet) {
			this.packet = packet;
		}

		private boolean tickExpired() {
			return --this.remainingTicks <= 0;
		}
	}

	private static final class DeferredSkillContainerValue {
		private final SPSetSkillContainerValue packet;
		private int remainingTicks = SKILL_PACKET_RETRY_TICKS;

		private DeferredSkillContainerValue(SPSetSkillContainerValue packet) {
			this.packet = packet;
		}

		private boolean tickExpired() {
			return --this.remainingTicks <= 0;
		}
	}
}
