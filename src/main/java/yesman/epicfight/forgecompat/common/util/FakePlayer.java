package yesman.epicfight.forgecompat.common.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FakePlayer extends ServerPlayer {
	public FakePlayer(ServerLevel level, GameProfile gameProfile) {
		super(level.getServer(), level, gameProfile);
	}
}
