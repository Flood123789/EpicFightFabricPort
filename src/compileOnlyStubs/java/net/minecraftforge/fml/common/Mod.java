package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.api.distmarker.Dist;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
	String value();

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface EventBusSubscriber {
		String modid() default "";

		Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

		Bus bus() default Bus.FORGE;

		enum Bus {
			FORGE,
			MOD
		}
	}
}
