package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.util.ID;

import java.util.ArrayDeque;
import java.util.Optional;

/**
 * Class provides the functionality of any set of nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Graph<C extends IConnectable, N extends IConnectable> implements INode  {

	private Int2ObjectMap<Group<C, N>> groups;
	private Long2IntMap positions; // group positions

	public Graph() {
		groups = new Int2ObjectLinkedOpenHashMap<>();
		positions = new Long2IntLinkedOpenHashMap();
		positions.defaultReturnValue(ID.INVALID);
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
	 * @return Gets the size of the groups map.
	 */
	public int countGroups() {
		return groups.size();
	}

	/**
	 * @return Gets the groups map.
	 */
	public Int2ObjectMap<Group<C, N>> getGroups() {
		return groups;
	}

	/**
	 * Adds a node to the graph at the specified position.
	 *
	 * @param pos The position at which the node will be added.
	 * @param node The node to add.
	 * @return True on success or false otherwise.
	 */
	public boolean addNode(long pos, Connectivity.Cache<N> node) {
		if (!contains(pos)) {

			Group<C, N> group = add(pos, Group.singleNode(pos, node));
			if (group != null) {
				group.addNode(pos, node);
			}

			return true;
		}

		return false;
	}

	/**
	 * Adds a connector to the graph at the specified position.
	 *
	 * @param pos The position at which the node will be added.
	 * @param connector The connector to add.
	 * @return True on success or false otherwise.
	 */
	public boolean addConnector(long pos, Connectivity.Cache<C> connector) {
		if (!contains(pos)) {

			Group<C, N> group = add(pos, Group.singleConnector(pos, connector));
			if (group != null) {
				group.addConnector(pos, connector);
			}

			return true;
		}

		return false;
	}

	/**
	 * Adds an item to the Graph, in a manner generic across nodes and connectors.
	 *
	 * @param pos The position at which the item will be added.
	 * @param single A group containing a single entry, if the position is not touching any existing positions.
	 * @return An existing group, that the caller should add the entry to.
	 */
	private Group<C, N> add(long pos, Group<C, N> single) {
		int id;
		ArrayDeque<Integer> mergers = getNeighborsGroups(pos);
		switch (mergers.size()) {
			case 0:
				id = ID.getNewId();
				positions.put(pos, id);
				groups.put(id, single);
				return null;

			case 1:
				id = mergers.peek();
				positions.put(pos, id);
				return groups.get(id);

			default:
				Merged<C, N> data = beginMerge(mergers);
				positions.put(pos, data.bestId);
				for (Group<C, N> other : data.merged) {
					data.best.mergeWith(other, pos);
				}
				return data.best;
		}
	}

	/**
	 * Removes an entry from the Group, potentially splitting it if needed. By calling this function, the caller asserts
	 * that this group contains the specified position; the function may misbehave if the group does not actually contain
	 * the specified position.
	 *
	 * @param pos The position of the entry to remove.
	 * @return The removed entry, guaranteed to not be null.
	 */
	public Entry<C, N> removeAt(long pos) {
		int id = positions.remove(pos);

		if (id == ID.INVALID) {
			return Entry.empty();
		}

		Group<C, N> group = groups.get(id);

		Entry<C, N> entry = group.removeAt(pos, newGroup -> {
			int newId = ID.getNewId();
			groups.put(newId, newGroup);

			// Mark the nodes as pointing at the new group
			for (long part : newGroup.getNodes().keySet()) {
				positions.put(part, newId);
			}

			// Mark the connectors as pointing at the new group
			for (Grid<C> grid : newGroup.getGrids().values()) {
				for (long part : grid.getConnectors().keySet()) {
					positions.put(part, newId);
				}
			}
		});

		if (group.countBlocks() == 0) {
			groups.remove(id);
		}

		return entry;
	}

	/**
	 * Finds an entry by a given position.
	 *
	 * @param pos The position of the cache to find.
	 * @return The found cache, guaranteed to not be null.
	 */
	public Entry<C, N> findAt(long pos) {
		int id = positions.get(pos);

		if (id == ID.INVALID) {
			return Entry.empty();
		}

		Group<C, N> group = groups.get(id);

		if (group != null) {
			Connectivity.Cache<N> node = group.getNodes().get(pos);

			if (node != null) {
				return Entry.node(node.value());
			} else {
				for (Grid<C> grid : group.getGrids().values()) {
					Connectivity.Cache<C> cable = grid.getConnectors().get(pos);
					if (cable != null) {
						return Entry.connector(cable.value());
					}
				}
			}
		}

		return Entry.empty();
	}

	/**
	 * Finds the group by a given position.
	 *
	 * @param pos The position of the group.
	 * @return The group, guaranteed to not be null.
	 */
	public Optional<Group<C, N>> findGroup(long pos) {
		int id = positions.get(pos);

		if (id == ID.INVALID) {
			return Optional.empty();
		}

		return Optional.of(groups.get(id));
	}

	/**
	 * Starts a merging process for a given groups.
	 *
	 * @param mergers An array of neighbors groups id.
	 * @return The wrapper with groups which should be merged.
	 */
	private Merged<C, N> beginMerge(ArrayDeque<Integer> mergers) {
		int bestId = mergers.peek();
		Group<C, N> best = groups.get(bestId);
		int bestSize = best.countBlocks();

		for (int id : mergers) {
			Group<C, N> candidate = groups.get(id);
			int size = candidate.countBlocks();

			if (size > bestSize) {
				best = candidate;
				bestId = id;
				bestSize = size;
			}
		}

		ObjectList<Group<C, N>> mergeGroups = new ObjectArrayList<>(mergers.size() - 1);

		for (int id : mergers) {
			if (id == bestId) {
				continue;
			}

			Group<C, N> removed = groups.remove(id);

			// Remap each position to point to the correct group.
			for (long position : removed.getBlocks()) {
				positions.put(position, bestId);
			}

			mergeGroups.add(removed);
		}

		Merged<C, N> data = new Merged<>();
		data.best = best;
		data.bestId = bestId;
		data.merged = mergeGroups;
		return data;
	}

	/**
	 * Lookups for neighbors groups around given position.
	 *
	 * @param pos The search position.
	 * @return The array of the groups which are neighbors to each other.
	 */
	private ArrayDeque<Integer> getNeighborsGroups(long pos) {
		ArrayDeque<Integer> neighbors = new ArrayDeque<>(6);

		Pos position = new Pos(pos);
		for (Dir direction : Dir.VALUES) {
			long face = position.offset(direction).get();
			int id = positions.get(face);

			if (id == ID.INVALID) {
				continue;
			}

			if (!neighbors.contains(id)) {
				neighbors.add(id);
			}
		}

		return neighbors;
	}

	/**
	 * @apiNote Wrapper for merged groups.
	 */
	private static class Merged<C extends IConnectable, N extends IConnectable> {
		int bestId;
		Group<C, N> best;
		ObjectList<Group<C, N>> merged;
	}
}
