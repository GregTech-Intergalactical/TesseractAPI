package tesseract.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.util.CID;

import java.util.List;
import java.util.function.Supplier;

/**
 * Class provides the functionality of any set of nodes.
 */
public class Graph<C extends IConnectable, N extends IConnectable> implements INode {

	private final Int2ObjectMap<Group<C, N>> groups = new Int2ObjectLinkedOpenHashMap<>();
	private final Long2IntMap positions = new Long2IntLinkedOpenHashMap(); // group positions

	public Graph() {
		positions.defaultReturnValue(CID.INVALID);
	}

	@Override
	public boolean contains(long pos) {
		return positions.containsKey(pos);
	}

	@Override
	public boolean linked(long from, Dir towards, long to) {
		return positions.containsKey(from) && positions.containsKey(to) && positions.get(from) == positions.get(to);
	}

	@Override
	public boolean connects(long pos, Dir towards) {
		return contains(pos);
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
	 * @param controller The controller to use.
	 * @return True on success or false otherwise.
	 */
	public boolean addNode(long pos, Cache<N> node, Controller<C, N> controller) {
		if (!contains(pos)) {
			Group<C, N> group = add(pos, () -> Group.singleNode(pos, node, controller));
			if (group != null) group.addNode(pos, node, controller);
			return true;
		}

		return false;
	}

	/**
	 * Adds a connector to the graph at the specified position.
	 *
	 * @param pos The position at which the node will be added.
	 * @param connector The connector to add.
	 * @param controller The controller to use.
	 * @return True on success or false otherwise.
	 */
	public boolean addConnector(long pos, Cache<C> connector, Controller<C, N> controller) {
		if (!contains(pos)) {
			Group<C, N> group = add(pos, () -> Group.singleConnector(pos, connector, controller));
			if (group != null) group.addConnector(pos, connector, controller);
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
	private Group<C, N> add(long pos, Supplier<Group<C, N>> single) {
		int id;
		IntSet mergers = getNeighboringGroups(pos);
		switch (mergers.size()) {
			case 0:
				id = CID.nextId();
				positions.put(pos, id);
				groups.put(id, single.get());
				return null;

			case 1:
				id = mergers.iterator().nextInt();
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
	 */
	public void removeAt(long pos) {
		int id = positions.remove(pos);

		if (id == CID.INVALID) {
			return;
		}

		Group<C, N> group = groups.get(id);

		group.removeAt(pos, newGroup -> {
			int newId = CID.nextId();
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
	}

	/**
	 * Gets the group by a given position.
	 *
	 * @param pos The position of the group.
	 * @return The group, guaranteed to not be null.
	 */
	public Group<C, N> getGroupAt(long pos) {
		int id = positions.get(pos);
		return (id != CID.INVALID) ? groups.get(id) : null;
	}

	/**
	 * Starts a merging process for a given groups.
	 *
	 * @param mergers An array of neighbors groups id.
	 * @return The wrapper with groups which should be merged.
	 */
	private Merged<C, N> beginMerge(IntSet mergers) {
		int bestId = mergers.iterator().nextInt();
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

		List<Group<C, N>> mergeGroups = new ObjectArrayList<>(mergers.size() - 1);

		for (int id : mergers) {
			if (id == bestId) {
				continue;
			}

			Group<C, N> removed = groups.remove(id);

			// Remap each position to point to the correct group.
			for (long pos : removed.getBlocks()) {
				positions.put(pos, bestId);
			}

			mergeGroups.add(removed);
		}

		return new Merged<>(bestId, best, mergeGroups);
	}

	/**
	 * Lookups for neighbors groups around given position.
	 *
	 * @param pos The search position.
	 * @return The set of the groups which are neighbors to each other.
	 */
	private IntSet getNeighboringGroups(long pos) {
		IntSet neighbors = new IntLinkedOpenHashSet(6);

		Pos position = new Pos(pos);
		for (Dir direction : Dir.VALUES) {
			long side = position.offset(direction).asLong();
			int id = positions.get(side);

			if (id != CID.INVALID) {
				neighbors.add(id);
			}
		}

		return neighbors;
	}

	/**
	 * @apiNote Wrapper for merged groups.
	 */
	private static class Merged<C extends IConnectable, N extends IConnectable> {

		final int bestId;
		final Group<C, N> best;
		final List<Group<C, N>> merged;

		/**
		 * Constructs a new Merged of the groups.
		 */
		Merged(int bestId, Group<C, N> best, List<Group<C, N>> merged) {
			this.best = best;
			this.bestId = bestId;
			this.merged = merged;
		}
	}
}
