package dev.bohush.mapmarkers.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.bohush.mapmarkers.util.MapDecorationTypeUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class MapDecorationTypeSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {

		for (var entry : Registries.MAP_DECORATION_TYPE.getEntrySet()) {
			var mapDecorationType = entry.getValue();
			var identifier = entry.getKey().getValue();

			if (MapDecorationTypeUtils.isValidDecorationType(mapDecorationType) && shouldSuggest(builder, identifier))
				builder.suggest(identifier.toString());
		}

		return builder.buildFuture();
	}

	private boolean shouldSuggest(SuggestionsBuilder builder, Identifier identifier) {
		var remaining = builder.getRemaining();

		return CommandSource.shouldSuggest(remaining, identifier.toString())
			|| CommandSource.shouldSuggest(remaining, identifier.getPath());
	}

}
