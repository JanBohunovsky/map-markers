package dev.bohush.mapmarkers.util;

import net.minecraft.item.map.MapDecorationType;

public class MapDecorationTypeUtils {

	public static boolean isValidDecorationType(MapDecorationType decorationType) {
		// Don't allow decorations which are not visible in item frames, as that might be confusing.
		if (!decorationType.showOnItemFrame())
			return false;

		// Don't allow decorations which are tracked, as those have a special purpose (e.g. Player marker), or are banners (those can already be added to maps).
		if (decorationType.trackCount())
			return false;

		return true;
	}

}
