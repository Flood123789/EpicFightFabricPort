package yesman.epicfight.api.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public final class ClientOnlyUtils {
	private static final String HELPER_CLASS = "yesman.epicfight.client.ClientRuntimeHelper";
	private static final Map<String, Method> METHODS = new HashMap<>();
	
	private ClientOnlyUtils() {
	}
	
	public static float getFrameTime(float fallback) {
		Object value = invoke("getFrameTime", new Class<?>[0]);
		return value instanceof Number number ? number.floatValue() : fallback;
	}
	
	public static ResourceManager getResourceManager(ResourceManager fallback) {
		Object value = invoke("getResourceManager", new Class<?>[0]);
		return value instanceof ResourceManager resourceManager ? resourceManager : fallback;
	}
	
	public static void stopSound(ResourceLocation location, SoundSource source) {
		invoke("stopSound", new Class<?>[] { ResourceLocation.class, SoundSource.class }, location, source);
	}
	
	public static void registerCustomEntityRenderer(EntityType<?> entityType, String renderer, CompoundTag tag) {
		invoke("registerCustomEntityRenderer", new Class<?>[] { EntityType.class, String.class, CompoundTag.class }, entityType, renderer, tag);
	}
	
	public static void refreshLocalPlayerSkillContainers() {
		invoke("refreshLocalPlayerSkillContainers", new Class<?>[0]);
	}
	
	public static boolean sendSkillCastRequest(SkillContainer container) {
		return Boolean.TRUE.equals(invoke("sendSkillCastRequest", new Class<?>[] { SkillContainer.class }, container));
	}
	
	public static void releaseAllServedKeys() {
		invoke("releaseAllServedKeys", new Class<?>[0]);
	}
	
	public static float getMainCameraYRot(float fallback) {
		Object value = invoke("getMainCameraYRot", new Class<?>[0]);
		return value instanceof Number number ? number.floatValue() : fallback;
	}
	
	public static void setOverlayMessage(Component component) {
		invoke("setOverlayMessage", new Class<?>[] { Component.class }, component);
	}
	
	public static void processBossEventOwner(Object bossPatch, UUID eventUUID, boolean addOperation) {
		invoke("processBossEventOwner", new Class<?>[] { Object.class, UUID.class, boolean.class }, bossPatch, eventUUID, addOperation);
	}
	
	public static TrailInfo overwriteTrailInfoFromItemRenderer(TrailInfo trailInfo, LivingEntityPatch<?> entitypatch) {
		Object value = invoke("overwriteTrailInfoFromItemRenderer", new Class<?>[] { TrailInfo.class, LivingEntityPatch.class }, trailInfo, entitypatch);
		return value instanceof TrailInfo overwritten ? overwritten : trailInfo;
	}
	
	public static void spawnEndermanDeathParticles(EnderMan enderman) {
		invoke("spawnEndermanDeathParticles", new Class<?>[] { EnderMan.class }, enderman);
	}
	
	private static Object invoke(String methodName, Class<?>[] parameterTypes, Object... args) {
		try {
			return method(methodName, parameterTypes).invoke(null, args);
		} catch (ClassNotFoundException ignored) {
			return null;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
			EpicFightMod.LOGGER.debug("Client-only helper call failed: {}", methodName, exception);
			return null;
		}
	}
	
	private static Method method(String methodName, Class<?>[] parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
		StringBuilder keyBuilder = new StringBuilder(methodName);
		
		for (Class<?> parameterType : parameterTypes) {
			keyBuilder.append('#').append(parameterType.getName());
		}
		
		String key = keyBuilder.toString();
		Method cached = METHODS.get(key);
		
		if (cached != null) {
			return cached;
		}
		
		Method method = Class.forName(HELPER_CLASS).getMethod(methodName, parameterTypes);
		METHODS.put(key, method);
		return method;
	}
}
