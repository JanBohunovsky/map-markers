package dev.bohush.mapmarkers.mixin;

import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapState.class)
public interface MapStateInvoker {

	@Invoker("removeDecoration")
	void invokeRemoveDecoration(String id);

	@Invoker("isInBounds")
	static boolean invokeIsInBounds(float dx, float dz) {
		throw new AssertionError();
	}

}
