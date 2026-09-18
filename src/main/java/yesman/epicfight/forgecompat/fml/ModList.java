package yesman.epicfight.forgecompat.fml;

import java.nio.file.Path;
import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ModList {
	private static final ModList INSTANCE = new ModList();

	private ModList() {
	}

	public static ModList get() {
		return INSTANCE;
	}

	public boolean isLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public ModFileInfo getModFileById(String modid) {
		return new ModFileInfo(modid);
	}

	public Optional<ModContainerWrapper> getModContainerById(String modid) {
		return FabricLoader.getInstance().getModContainer(modid).map(ModContainerWrapper::new);
	}

	public static class ModFileInfo {
		private final String modid;

		public ModFileInfo(String modid) {
			this.modid = modid;
		}

		public String versionString() {
			return FabricLoader.getInstance().getModContainer(modid)
				.map(c -> c.getMetadata().getVersion().getFriendlyString())
				.orElse("1.0.0");
		}

		public ModFile getFile() {
			return new ModFile(modid);
		}
	}

	public static class ModFile {
		private final String modid;

		public ModFile(String modid) {
			this.modid = modid;
		}

		public Path findResource(String path) {
			return FabricLoader.getInstance().getModContainer(modid)
				.flatMap(c -> c.findPath(path))
				.orElse(null);
		}

		public String getFileName() {
			return modid + ".jar";
		}
	}

	public static class ModContainerWrapper {
		private final ModContainer container;

		public ModContainerWrapper(ModContainer container) {
			this.container = container;
		}

		public <T> Optional<T> getCustomExtension(Class<T> clazz) {
			return Optional.empty();
		}
	}
}
