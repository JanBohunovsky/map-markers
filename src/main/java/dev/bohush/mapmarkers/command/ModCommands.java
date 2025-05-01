package dev.bohush.mapmarkers.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {

	public static final String GROUP = "mapmarkers";

	public static void registerCommands() {
		CommandRegistrationCallback.EVENT.register(ModCommands::onRegister);
	}

	private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		var modNode = literal(GROUP).build();

		dispatcher.getRoot().addChild(modNode);
		modNode.addChild(AddMarkerCommand.build());
		modNode.addChild(ListMarkersCommand.build());
		modNode.addChild(RemoveMarkerCommand.build());
	}
}
