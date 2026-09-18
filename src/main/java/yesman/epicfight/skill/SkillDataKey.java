package yesman.epicfight.skill;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.registries.IForgeRegistry;
import yesman.epicfight.forgecompat.registries.IForgeRegistryInternal;
import yesman.epicfight.forgecompat.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.api.utils.datastruct.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;

public class SkillDataKey<T> {
	private static final HashMultimap<Class<?>, SkillDataKey<?>> SKILL_DATA_KEYS = HashMultimap.create();
    private static final ResourceLocation CLASS_TO_DATA_KEYS = EpicFightMod.identifier("classtodatakeys");
    private static final ResourceLocation DATA_KEY_TO_ID = EpicFightMod.identifier("datakeytoid");
	
	private static class SkillDataKeyCallbacks implements IForgeRegistry.BakeCallback<SkillDataKey<?>>, IForgeRegistry.CreateCallback<SkillDataKey<?>>, IForgeRegistry.ClearCallback<SkillDataKey<?>> {
		static final SkillDataKeyCallbacks INSTANCE = new SkillDataKeyCallbacks();
		
		@Override
		@SuppressWarnings("unchecked")
        public void onBake(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			final ClearableIdMapper<SkillDataKey<?>> skillDataKeyMap = owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class);

			// Skill data ids are synchronized, so they are ordered by name rather than by
			// the registry's HashSet iteration order, which varies between JVMs.
			skillDataKeyMap.clear();
			owner.getKeys().stream().sorted().map(owner::getValue).filter(Objects::nonNull).forEach(skillDataKeyMap::add);

			final Map<Class<?>, Set<SkillDataKey<?>>> skillDataKeys = owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
			final IForgeRegistry<Skill> skills = SkillManager.getSkillRegistry();

			// Baking now runs during registration, which on Fabric can precede the skill
			// registry being created. The id map above is the part that has to be ready.
			if (skills == null) {
				return;
			}

			skillDataKeys.clear();

			skills.forEach((skill) -> {
				Class<?> skillClass = skill.getClass();
				Set<SkillDataKey<?>> dataKeySet = Sets.newHashSet();
				skillDataKeys.put(skillClass, dataKeySet);
				
				do {
					if (SKILL_DATA_KEYS.containsKey(skillClass)) {
						dataKeySet.addAll(SKILL_DATA_KEYS.get(skillClass));
					}
					
					skillClass = skillClass.getSuperclass();
				} while (Skill.class.isAssignableFrom(skillClass));
				
				if (!dataKeySet.isEmpty()) {
					EpicFightMod.LOGGER.debug("Data keys "  + dataKeySet.stream().map(SkillDataKeys.REGISTRY.get()::getKey).toList() + " for " + skill.getRegistryName());
				}
			});
        }
		
		@Override
		public void onCreate(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			owner.setSlaveMap(CLASS_TO_DATA_KEYS, Maps.newHashMap());
			owner.setSlaveMap(DATA_KEY_TO_ID, new ClearableIdMapper<SkillDataKey<?>> (owner.getKeys().size()));
		}
		
		@Override
        public void onClear(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class).clear();
            owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class).clear();
        }
	}
	
	public static SkillDataKeyCallbacks getRegistryCallback() {
		return SkillDataKeyCallbacks.INSTANCE;
	}
	
	public static <T> SkillDataKey<T> createSkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, Class<?>... skillClass) {
		return createSkillDataKey(packetCodec, defaultValue, false, skillClass);
	}
	
	public static <T> SkillDataKey<T> createSkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
		SkillDataKey<T> key = new SkillDataKey<T> (packetCodec, defaultValue, syncronizeTrackingPlayers);
		
		for (Class<?> cls : skillClass) {
			SKILL_DATA_KEYS.put(cls, key);
		}
		
		return key;
	}
	
	@SuppressWarnings("unchecked")
	public static IdMapper<SkillDataKey<?>> getIdMap() {
		return SkillDataKeys.REGISTRY.get().getSlaveMap(DATA_KEY_TO_ID, IdMapper.class);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Class<?>, Set<SkillDataKey<?>>> getSkillDataKeyMap() {
		return SkillDataKeys.REGISTRY.get().getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
	}

	/**
	 * Resolves the keys directly from their registrations. Forge normally bakes the
	 * class-to-key slave map after all skills are available, but Fabric can complete
	 * that bake before the skill registry is populated and leave the map empty.
	 */
	public static Set<SkillDataKey<?>> getSkillDataKeysFor(Class<?> skillClass) {
		Set<SkillDataKey<?>> keys = Sets.newHashSet();
		Class<?> currentClass = skillClass;

		while (currentClass != null && Skill.class.isAssignableFrom(currentClass)) {
			if (SKILL_DATA_KEYS.containsKey(currentClass)) {
				keys.addAll(SKILL_DATA_KEYS.get(currentClass));
			}

			currentClass = currentClass.getSuperclass();
		}

		return keys;
	}
	
	private final PacketBufferCodec<T> packetCodec;
	private final T defaultValue;
	private final boolean syncronizeTrackingPlayers;
	
	public SkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers) {
		this.packetCodec = packetCodec;
		this.defaultValue = defaultValue;
		this.syncronizeTrackingPlayers = syncronizeTrackingPlayers;
	}
	
	public T readFromBuffer(FriendlyByteBuf buffer) {
		return this.packetCodec.decode(buffer);
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer, T value) {
		this.packetCodec.encode(value, buffer);
	}
	
	public T defaultValue() {
		return this.defaultValue;
	}
	
	public int getId() {
		return getIdMap().getId(this);
	}
	
	public boolean syncronizeToTrackingPlayers() {
		return this.syncronizeTrackingPlayers;
	}
}
