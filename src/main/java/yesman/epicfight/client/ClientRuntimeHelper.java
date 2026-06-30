package yesman.epicfight.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.api.data.reloader.SkillManager;

public final class ClientRuntimeHelper {
	private ClientRuntimeHelper() {
	}
	
	public static float getFrameTime() {
		return Minecraft.getInstance().getFrameTime();
	}
	
	public static ResourceManager getResourceManager() {
		return Minecraft.getInstance().getResourceManager();
	}
	
	public static void stopSound(ResourceLocation location, SoundSource source) {
		Minecraft.getInstance().getSoundManager().stop(location, source);
	}
	
	public static void registerCustomEntityRenderer(EntityType<?> entityType, String renderer, CompoundTag tag) {
		ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, renderer, tag);
	}
	
	public static void refreshLocalPlayerSkillContainers() {
		LocalPlayerPatch localplayerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (localplayerpatch != null) {
			CapabilitySkill skillCapability = localplayerpatch.getSkillCapability();
			
			skillCapability.listSkillContainers().forEach(skillContainer -> {
				if (skillContainer.getSkill() != null) {
					skillContainer.setSkill(SkillManager.getSkill(skillContainer.getSkill().toString()), true);
				}
			});
			
			skillCapability.getSkillContainerFor(SkillSlots.BASIC_ATTACK).setSkill(EpicFightSkills.BASIC_ATTACK);
			skillCapability.getSkillContainerFor(SkillSlots.KNOCKDOWN_WAKEUP).setSkill(EpicFightSkills.KNOCKDOWN_WAKEUP);
		}
	}
	
	public static boolean sendSkillCastRequest(SkillContainer container) {
		return container.sendCastRequest((LocalPlayerPatch)container.getExecutor(), ClientEngine.getInstance().controlEngine).isExecutable();
	}
	
	public static void releaseAllServedKeys() {
		ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
	}
	
	public static float getMainCameraYRot() {
		return Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
	}
	
	public static void setOverlayMessage(Component component) {
		Minecraft.getInstance().gui.setOverlayMessage(component, false);
	}
	
	public static void processBossEventOwner(Object owner, UUID eventUUID, boolean addOperation) {
		if (owner instanceof BossPatch<?> bossPatch) {
			if (addOperation) {
				ClientEngine.getInstance().renderEngine.addBossEventOwner(eventUUID, bossPatch);
			} else {
				ClientEngine.getInstance().renderEngine.removeBossEventOwner(eventUUID, bossPatch);
			}
		}
	}
	
	public static TrailInfo overwriteTrailInfoFromItemRenderer(TrailInfo trailInfo, LivingEntityPatch<?> entitypatch) {
		if (trailInfo.hand() != null) {
			RenderItemBase renderitembase = ClientEngine.getInstance().renderEngine.getItemRenderer(entitypatch.getAdvancedHoldingItemStack(trailInfo.hand()));
			
			if (renderitembase != null && renderitembase.trailInfo() != null) {
				return renderitembase.trailInfo().overwrite(trailInfo);
			}
		}
		
		return trailInfo;
	}
	
	public static void spawnEndermanDeathParticles(EnderMan enderman) {
		Minecraft minecraft = Minecraft.getInstance();
		
		for (int i = 0; i < 100; i++) {
			RandomSource rand = enderman.getRandom();
			Vec3f vec = new Vec3f(rand.nextInt(), rand.nextInt(), rand.nextInt());
			vec.normalize().scale(0.5F);
			minecraft.particleEngine.createParticle(
				EpicFightParticles.ENDERMAN_DEATH_EMIT.get(),
				enderman.getX(),
				enderman.getY() + enderman.getDimensions(net.minecraft.world.entity.Pose.STANDING).height / 2,
				enderman.getZ(),
				vec.x,
				vec.y,
				vec.z
			);
		}
	}
}
