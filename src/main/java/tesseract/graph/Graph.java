package tesseract.graph;

import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.graph.traverse.INodeContainer;
import tesseract.graph.visit.VisitableGroup;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

// default: parameters are nonnull, methods return nonnull
public class Graph<C extends IConnectable, N extends IConnectable> implements INodeContainer {

	private HashMap<Pos, UUID> posGrouping;
	private HashMap<UUID, Group<C, N>> groups;
	
	public Graph() {
		posGrouping = new HashMap<>();
		groups = new HashMap<>();
	}

	@Override
	public boolean contains(Pos pos) {
		return posGrouping.containsKey(pos);
	}

	@Override
	public boolean linked(Pos from, Dir towards, Pos to) {
		return posGrouping.containsKey(from) && posGrouping.containsKey(to);
	}

	@Override
	public boolean connects(Pos position, Dir towards) {
		return contains(position);
	}

	public void visit(BiConsumer<UUID, VisitableGroup<C, N>> visitor) {
		for (Map.Entry<UUID, Group<C, N>> entry: groups.entrySet()) {
			visitor.accept(entry.getKey(), entry.getValue());
		}
	}

	public int countGroups() {
		return groups.size();
	}

	/**
	 * Adds a node to the graph at the specified position.
	 * @param pos The position at which the node will be added
	 * @param node The node to add
	 */
	public void addNode(Pos pos, Connectivity.Cache<N> node) {
		add(pos, () -> Group.singleNode(pos, node), group -> group.addNode(pos, node));
	}

	/**
	 * Adds a connector to the graph at the specified position.
	 * @param pos The position at which the node will be added
	 * @param connector The connector to add
	 */
	public void addConnector(Pos pos, Connectivity.Cache<C> connector) {
		add(pos, () -> Group.singleConnector(pos, connector), group -> group.addConnector(pos, connector));
	}

	/**
	 * Adds an item to the Graph, in a manner generic across nodes and connectors.
	 * @param pos The position at which the item will be added
	 * @param single A supplier of a group containing a single entry, if the position is not touching any existing positions.
	 * @param multiple An acceptor of an existing group, that the caller should add the entry to.
	 */
	private void add(Pos pos, Supplier<Group<C, N>> single, Consumer<Group<C, N>> multiple) {
		UUID uuid;
		ArrayDeque<UUID> mergers = getNeighboringGroups(pos);
		switch (mergers.size()) {
			case 0:
				uuid = getNewId();
				posGrouping.put(pos, uuid);
				groups.put(uuid, single.get());
				break;
			case 1:
				uuid = mergers.peek();
				posGrouping.put(pos, uuid);
				multiple.accept(groups.get(uuid));
				break;
			default:
				MergeData<C, N> data = beginMerge(mergers);

				posGrouping.put(pos, data.bestId);
				multiple.accept(data.best);

				for (Group<C, N> other: data.mergeGroups) {
					data.best.mergeWith(other, pos);
				}
				break;
		}
	}

	public Optional<Entry<C, N>> remove(Pos pos) {
		UUID uuid = posGrouping.remove(pos);

		if (uuid == null) {
			return Optional.empty();
		}

		Group<C, N> group = groups.get(uuid);

		Entry<C, N> entry = group.remove(pos, newGroup -> {
			UUID newUuid = getNewId();
			groups.put(newUuid, newGroup);

			// Mark the nodes as pointing at the new group
			newGroup.visitNodes((part, node) -> posGrouping.put(part, newUuid));

			// Mark the connectors as pointing at the new group
			newGroup.visitGrids(grid -> grid.visitConnectors((part, connector) -> posGrouping.put(part, newUuid)));
		});

		if (group.countBlocks() == 0) {
			groups.remove(uuid);
		}

		return Optional.of(entry);
	}

	public void remove(UUID id) {
		groups.remove(id);
		posGrouping.entrySet().removeIf(entry -> entry.getValue() == id);
	}

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

		HashSet<Group<C, N>> mergeGroups = new HashSet<>(mergers.size() - 1);

		for (UUID id: mergers) {
			if (id.equals(bestId)) {
				continue;
			}

			Group<C, N> removed = groups.remove(id);
			final UUID target = bestId;

			// Remap each position to point to the correct group.
			removed.visitBlocks(position -> posGrouping.put(position, target));

			mergeGroups.add(removed);
		}

		MergeData<C, N> data = new MergeData<>();

		data.best = best;
		data.bestId = bestId;
		data.mergeGroups = mergeGroups;

		return data;
	}

	private ArrayDeque<UUID> getNeighboringGroups(Pos pos) {
		ArrayDeque<UUID> neighbors = new ArrayDeque<>(6);

		for (Dir direction : Dir.VALUES) {
			Pos face = pos.offset(direction);
			UUID group = posGrouping.get(face);

			if (group == null) {
				continue;
			}

			if (!neighbors.contains(group)) {
				neighbors.add(group);
			}
		}

		return neighbors;
	}

	private UUID getNewId() {
		UUID uuid = UUID.randomUUID();

		while (groups.containsKey(uuid)) {
			// Should never be called, but whatever.
			uuid = UUID.randomUUID();
		}

		return uuid;
	}

	private static class MergeData<C extends IConnectable, N extends IConnectable> {
		UUID bestId;
		Group<C, N> best;
		HashSet<Group<C, N>> mergeGroups;
	}
}
