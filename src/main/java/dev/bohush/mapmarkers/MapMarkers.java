package dev.bohush.mapmarkers;

import dev.bohush.mapmarkers.command.ModCommands;
import net.fabricmc.api.ModInitializer;

public class MapMarkers implements ModInitializer {
	public static final String MOD_ID = "map-markers";

	@Override
	public void onInitialize() {
		ModCommands.registerCommands();
	}
}