package dev.bohush.mapmarkers.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.bohush.mapmarkers.FilledMapItemWrapper;
import dev.bohush.mapmarkers.util.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class MarkerIdSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		var source = context.getSource();
		var player = source.getPlayer();
		var world = source.getWorld();

		if (player == null) {
			return Suggestions.empty();
		}

		FilledMapItemWrapper map;
		try {
			map = CommandUtils.getMapOrThrow(player, world);
		} catch (CommandSyntaxException e) {
			return Suggestions.empty();
		}

		for	(var marker : map.getMarkers()) {
			if (CommandSource.shouldSuggest(builder.getRemaining(), marker.id())) {
				builder.suggest(marker.id());
			}
		}

		return builder.buildFuture();
	}
}
