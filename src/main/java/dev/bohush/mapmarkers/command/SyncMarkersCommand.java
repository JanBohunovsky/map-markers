package dev.bohush.mapmarkers.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.bohush.mapmarkers.FilledMapItemWrapper;
import dev.bohush.mapmarkers.util.CommandUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class SyncMarkersCommand implements Command<ServerCommandSource> {

	public static final String NAME = "sync";

	private static final SimpleCommandExceptionType DIFFERENT_ID
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.sync.different_id"));

	public static LiteralCommandNode<ServerCommandSource> build() {
		return literal(NAME)
			.requires(ServerCommandSource::isExecutedByPlayer)
			.executes(new SyncMarkersCommand())
			.build();
	}

	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var source = context.getSource();
		var player = source.getPlayerOrThrow();
		var world = source.getWorld();

		var mainHandMap = CommandUtils.getMapOrThrow(player, world);
		var offHandMap = CommandUtils.getOffHandMapOrThrow(player, world);

		if (mainHandMap.getId() != offHandMap.getId()) {
			throw DIFFERENT_ID.create();
		}

		FilledMapItemWrapper.synchroniseMarkers(mainHandMap, offHandMap);

		return SINGLE_SUCCESS;
	}
}
