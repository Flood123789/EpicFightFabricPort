package yesman.epicfight.forgecompat.common.capabilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class CapabilityToken<T> {
	public String getType() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof ParameterizedType parameterizedType) {
			return parameterizedType.getActualTypeArguments()[0].getTypeName();
		}
		return Object.class.getName();
	}
}
