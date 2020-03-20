package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.UUID;

/**
 * Class provides the functionality of any set of nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Graph<C extends IConnectable, N extends IConnectable> implements INode {

	private Object2ObjectMap<UUID, Group<C, N>> groups;
	private Long2ObjectMap<UUID> positions; // group positions

	public Graph() {
		groups = new Object2ObjectLinkedOpenHashMap<>();
		positions = new Long2ObjectLinkedOpenHashMap<>();
	}

	@Override
	public boolean contains(long pos) {
		return positions.containsKey(pos);
	}

	@Override
	public boolean linked(long from, Dir towards, long to) {
		return positions.containsKey(from) && positions.containsKey(to);
	}

	@Override
	public boolean connects(long position, Dir towards) {
		return contains(position);
	}

	/**
	 *
	 * @return
	 */
	public Object2ObjectMap<UUID, Group<C, N>> getGroups() {
		return groups;
	}

	/**
	 *
	 * @return
	 */
	public int countGroups() {
		return groups.size();
	}

	/**
	 * Adds a node to the graph at the specified position.
	 *
	 * @param pos The position at which the node will be added
	 * @param node The node to add
	 */
	public void addNode(long pos, Connectivity.Cache<N> node) {
		Group<C, N> group = add(pos, Group.singleNode(pos, node));
		if (group != null) {
			group.addNode(pos, node);
		}
	}

	/**
	 * Adds a connector to the graph at the specified position.
	 *
	 * @param pos The position at which the node will be added
	 * @param connector The connector to add
	 */
	public void addConnector(long pos, Connectivity.Cache<C> connector) {
		Group<C, N> group = add(pos, Group.singleConnector(pos, connector));
		if (group != null) {
			group.addConnector(pos, connector);
		}
	}

	/**
	 * Adds an item to the Graph, in a manner generic across nodes and connectors.
	 *
	 * @param pos The position at which the item will be added
	 * @param single A group containing a single entry, if the position is not touching any existing positions.
	 * @return An existing group, that the caller should add the entry to.
	 */
	private Group<C, N> add(long pos, Group<C, N> single) {
		UUID uuid;
		ArrayDeque<UUID> mergers = getNeighborsGroups(pos);
		switch (mergers.size()) {
			case 0:
				uuid = getNewId();
				positions.put(pos, uuid);
				groups.put(uuid, single);
				return null;

			case 1:
				uuid = mergers.peek();
				positions.put(pos, uuid);
				return groups.get(uuid);

			default:
				MergeData<C, N> data = beginMerge(mergers);
				positions.put(pos, data.bestId);
				for (Group<C, N> other: data.mergeGroups) {
					data.best.mergeWith(other, pos);
				}
				return data.best;
		}
	}

	/**
	 *
	 * @param pos
	 * @return
	 */
	public Optional<Entry<C, N>> remove(long pos) {
		UUID uuid = positions.remove(pos);

		if (uuid == null) {
			return Optional.empty();
		}

		Group<C, N> group = groups.get(uuid);

		Entry<C, N> entry = group.remove(pos, newGroup -> {
			UUID newUuid = getNewId();
			groups.put(newUuid, newGroup);

			// Mark the nodes as pointing at the new group
			for (long part : newGroup.getNodes().keySet()) {
				positions.put(part, newUuid);
			}

			// Mark the connectors as pointing at the new group
			for (IGrid<C> grid : newGroup.getGrids().values()) {
				for (long part : grid.getConnectors().keySet()) {
					positions.put(part, newUuid);
				}
			}
		});

		if (group.countBlocks() == 0) {
			groups.remove(uuid);
		}

		return Optional.of(entry);
	}

	/**
	 *
	 * @param mergers
	 * @return
	 */
	private MergeData<C, N> beginMerge(ArrayDeque<UUID> mergers) {
		UUID bestId = mergers.peek();
		Group<C, N> best = groups.get(bestId);
		int bestSize = best.countBlocks();

		for (UUID id: mergers) {
			Group<C, N> candidate = groups.get(id);
			int size = candidate.countBlocks();

			if (size > bestSize) {
				best = candidate;
				bestId = id;
				bestSize = size;
			}
		}

		ObjectLinkedOpenHashSet<Group<C, N>> mergeGroups = new ObjectLinkedOpenHashSet<>(mergers.size() - 1);

		for (UUID id: mergers) {
			if (id.equals(bestId)) {
				continue;
			}

			Group<C, N> removed = groups.remove(id);

			// Remap each position to point to the correct group.
			for (long position : removed.getBlocks()) {
				positions.put(position, bestId);
			}

			mergeGroups.add(removed);
		}

		MergeData<C, N> data = new MergeData<>();
		data.best = best;
		data.bestId = bestId;
		data.mergeGroups = mergeGroups;
		return data;
	}

	/**
	 * Lookups for neighbors groups around given position.
	 *
	 * @param pos The search position.
	 * @return The array of the groups which are neighbors to each other.
	 */
	private ArrayDeque<UUID> getNeighborsGroups(long pos) {
		ArrayDeque<UUID> neighbors = new ArrayDeque<>(6);

		Pos position = new Pos(pos);
		for (Dir direction : Dir.VALUES) {
			long face = position.offset(direction).get();
			UUID group = positions.get(face);

			if (group == null) {
				continue;
			}

			if (!neighbors.contains(group)) {
				neighbors.add(group);
			}
		}

		return neighbors;
	}

	/**
	 * @return Pseudo randomly generates an immutable universally unique identifier.
	 */
	private UUID getNewId() {
		UUID uuid = UUID.randomUUID();

		while (groups.containsKey(uuid)) {
			// Should never be called, but whatever.
			uuid = UUID.randomUUID();
		}

		return uuid;
	}

	/**
	 * @apiNote Wrapper for merged groups.
	 */
	private static class MergeData<C extends IConnectable, N extends IConnectable> {
		UUID bestId;
		Group<C, N> best;
		ObjectLinkedOpenHashSet<Group<C, N>> mergeGroups;
	}
}
