package yesman.epicfight.forgecompat.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.registries.ForgeRegistry;
import yesman.epicfight.forgecompat.registries.IForgeRegistry;
import yesman.epicfight.forgecompat.registries.RegistryManager;

public class BufUtil {
    @SuppressWarnings("unchecked")
    public static <T> void writeRegistryId(FriendlyByteBuf buf, Object registry, T value) {
        ResourceLocation regName = new ResourceLocation("epicfight", "unknown");
        ResourceLocation valName = new ResourceLocation("epicfight", "empty");
        if (registry instanceof IForgeRegistry<?> forgeReg) {
            regName = forgeReg.getRegistryName();
            ResourceLocation k = ((IForgeRegistry<T>) forgeReg).getKey(value);
            if (k != null) valName = k;
        } else if (registry instanceof Registry<?> mcReg) {
            regName = mcReg.key().location();
            ResourceLocation k = ((Registry<T>) mcReg).getKey(value);
            if (k != null) valName = k;
        }
        buf.writeResourceLocation(regName);
        buf.writeResourceLocation(valName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readRegistryId(FriendlyByteBuf buf) {
        ResourceLocation regName = buf.readResourceLocation();
        ResourceLocation valName = buf.readResourceLocation();
        ForgeRegistry<?> reg = RegistryManager.ACTIVE.getRegistry(regName);
        if (reg != null) {
            return (T) reg.getValue(valName);
        }
        return null;
    }
}
