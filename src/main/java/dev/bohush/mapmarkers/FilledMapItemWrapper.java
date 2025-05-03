package dev.bohush.mapmarkers;

import dev.bohush.mapmarkers.mixin.MapStateInvoker;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class FilledMapItemWrapper {

	private final int id;
	private final ItemStack map;
	private final MapState state;

	private FilledMapItemWrapper(int id, ItemStack map, MapState state) {
		this.id = id;
		this.map = map;
		this.state = state;
	}

	/**
	 * Creates a new FilledMapItemWrapper instance from an ItemStack.
	 * @return A new FilledMapItemWrapper instance, or null if the map state could not be loaded.
	 * @throws IllegalArgumentException if the provided ItemStack is not a filled map.
	 */
	@Nullable
	public static FilledMapItemWrapper create(ItemStack map, ServerWorld world) {
		if (map.isEmpty() || !map.isOf(net.minecraft.item.Items.FILLED_MAP)) {
			throw new IllegalArgumentException("Argument 'map' is not a filled map, got %s.".formatted(map.getItem()));
		}

		var mapId = map.get(DataComponentTypes.MAP_ID);
		var state = world.getMapState(mapId);
		if (mapId == null || state == null) {
			return null;
		}

		return new FilledMapItemWrapper(mapId.id(), map, state);
	}

	/**
	 * Synchronises the markers between two maps with the same ID.
	 * Prioritises markers from the first map in the case of duplicate marker ID.
	 */
	public static void synchroniseMarkers(FilledMapItemWrapper firstMap, FilledMapItemWrapper secondMap) {
		if (firstMap.getId() != secondMap.getId()) {
			throw new IllegalArgumentException("Maps do not have the same ID.");
		}

		var firstDecorations = firstMap.getDecorations();
		var secondDecorations = secondMap.getDecorations();

		var mergedDecorations = new HashMap<>(secondDecorations.decorations());
		// Prefer the first map's decorations over the second map's in case of key collision.
		mergedDecorations.putAll(firstDecorations.decorations());

		var component = new MapDecorationsComponent(mergedDecorations);
		firstMap.setDecorations(component);
		secondMap.setDecorations(component);
	}

	public int getId() {
		return id;
	}

	public Text getName() {
		return map.getName();
	}

	public Text getFormattedName() {
		return map.getFormattedName();
	}

	public Text getHoverableText() {
		return map.toHoverableText();
	}

	public boolean isExplorersMap() {
		// Use the fact that explorer maps use a different translation key to detect them. It feels like a hack, though.
		var translationKey = map.getItem().getTranslationKey();
		var itemName = map.get(DataComponentTypes.ITEM_NAME);

		if (itemName == null)
			return false;

		if (!(itemName.getContent() instanceof TranslatableTextContent translatableContent))
			return false;

		return !translationKey.equals(translatableContent.getKey());
	}

	public boolean isLocked() {
		return state.locked;
	}

	/**
	 * Checks if the map has any markers.
	 *
	 * @return true if the map has any markers, false otherwise.
	 */
	public boolean hasMarkers() {
		return !getDecorations().decorations().isEmpty();
	}

	/**
	 * Gets all markers on the map.
	 *
	 * @return An Iterable collection of MapMarker objects representing all markers on the map.
	 */
	public Iterable<MapMarker> getMarkers() {
		return getDecorations().decorations().entrySet()
			.stream()
			.map(entry -> MapMarker.fromDecoration(entry.getKey(), entry.getValue()))
			.sorted(Comparator.comparingDouble(MapMarker::x))
			.sorted(Comparator.comparingDouble(MapMarker::z))
			.collect(Collectors.toList());
	}

	/**
	 * Adds a new marker to the map with default rotation (180 degrees).
	 *
	 * @param type The type of map decoration to add
	 * @param x    The X coordinate for the marker
	 * @param z    The Z coordinate for the marker
	 * @return The newly added marker, or null if the provided coordinates are outside the map bounds.
	 */
	@Nullable
	public MapMarker addMarker(MapDecorationType type, double x, double z) {
		return addMarker(type, x, z, 180);
	}

	/**
	 * Adds a new marker to the map.
	 *
	 * @param type     The type of map decoration to add
	 * @param x        The X coordinate for the marker
	 * @param z        The Z coordinate for the marker
	 * @param rotation The rotation angle in degrees
	 * @return The newly added marker, or null if the provided coordinates are outside the map bounds.
	 */
	@Nullable
	public MapMarker addMarker(MapDecorationType type, double x, double z, float rotation) {
		var scale = 1 << state.scale;
		float dx = (float)(x - state.centerX) / scale;
		float dz = (float)(z - state.centerZ) / scale;
		if (!MapStateInvoker.invokeIsInBounds(dx, dz))
			return null;

		var decorations = getDecorations();

		var id = UUID.randomUUID().toString();

		var decorationTypeEntry = Registries.MAP_DECORATION_TYPE.getEntry(type);
		var decoration = new MapDecorationsComponent.Decoration(decorationTypeEntry, x, z, rotation);

		setDecorations(decorations.with(id, decoration));

		return MapMarker.fromDecoration(id, decoration);
	}

	/**
	 * Removes a marker from the map.
	 *
	 * @param id The unique identifier of the marker to remove
	 * @return The removed MapMarker object, or null if no marker was found with the given id.
	 */
	@Nullable
	public MapMarker removeMarker(String id) {
		var decorations = getDecorations();

		var decorationToRemove = decorations.decorations().get(id);
		if (decorationToRemove == null)
			return null;

		var updatedDecorations = new HashMap<>(decorations.decorations());
		updatedDecorations.remove(id);
		setDecorations(new MapDecorationsComponent(updatedDecorations));

		((MapStateInvoker)state).invokeRemoveDecoration(id);

		return MapMarker.fromDecoration(id, decorationToRemove);
	}

	private MapDecorationsComponent getDecorations() {
		return map.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
	}

	private void setDecorations(MapDecorationsComponent decorations) {
		map.set(DataComponentTypes.MAP_DECORATIONS, decorations);
	}

}
