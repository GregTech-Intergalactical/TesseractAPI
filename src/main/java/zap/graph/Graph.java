package zap.graph;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.INodeContainer;

import java.util.*;

// default: parameters are nonnull, methods return nonnull
public class Graph<C extends IConnectable, N extends IConnectable> implements INodeContainer {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	private HashMap<BlockPos, UUID> posGrouping;
	HashMap<UUID, Group<C, N>> groups;
	
	public Graph() {
		posGrouping = new HashMap<>();
		groups = new HashMap<>();
	}

	@Override
	public boolean contains(BlockPos pos) {
		return posGrouping.containsKey(pos);
	}

	@Override
	public boolean linked(BlockPos from, Direction towards, BlockPos to) {
		return posGrouping.containsKey(from) && posGrouping.containsKey(to);
	}

	@SuppressWarnings("unchecked")
	public void addNode(BlockPos pos, N node) {
		ArrayList<UUID> mergers = getNeighboringGroups(pos);

		if(mergers.size()==0) {
			UUID uuid = getNewId();

			posGrouping.put(pos, uuid);
			groups.put(uuid, (Group<C, N>)Group.singleNode(pos, node));
		} else if(mergers.size()==1) {
			UUID uuid = mergers.get(0);

			posGrouping.put(pos, uuid);
			groups.get(uuid).addNode(pos, node);
		} else {
			MergeData<C, N> data = beginMerge(mergers);

			posGrouping.put(pos, data.bestId);
			data.best.addNode(pos, node);

			for(Group<C, N> other: data.mergeGroups) {
				data.best.mergeWith(other, pos);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addConnector(BlockPos pos, C connector) {
		ArrayList<UUID> mergers = getNeighboringGroups(pos);

		if(mergers.size()==0) {
			UUID uuid = getNewId();

			posGrouping.put(pos, uuid);
			groups.put(uuid, (Group<C, N>)Group.singleConnector(pos, connector));
		} else if(mergers.size()==1) {
			UUID uuid = mergers.get(0);

			posGrouping.put(pos, uuid);
			groups.get(uuid).addConnector(pos, connector);
		} else {
			MergeData<C, N> data = beginMerge(mergers);

			posGrouping.put(pos, data.bestId);
			data.best.addConnector(pos, connector);

			for(Group<C, N> other: data.mergeGroups) {
				data.best.mergeWith(other, pos);
			}
		}
	}

	public Optional<Entry<C, N>> remove(BlockPos pos) {
		UUID uuid = posGrouping.remove(pos);

		if(uuid == null) {
			return Optional.empty();
		}

		Group<C, N> group = groups.get(uuid);

		Entry<C, N> entry = group.remove(pos, newGroup -> {
			UUID newUuid = getNewId();
			groups.put(newUuid, newGroup);

			for(BlockPos part: newGroup.nodes.keySet()) {
				posGrouping.put(part, newUuid);
			}

			for(Grid<C> grid: newGroup.grids.values()) {
				for(BlockPos part: grid.connectors.keySet()) {
					posGrouping.put(part, newUuid);
				}
			}
		});

		if(group.countBlocks() == 0) {
			groups.remove(uuid);
		}

		return Optional.of(entry);
	}

	private MergeData<C, N> beginMerge(ArrayList<UUID> mergers) {
		UUID bestId = mergers.get(0);
		Group<C, N> best = groups.get(bestId);
		int bestSize = best.countBlocks();

		for(UUID id: mergers) {
			Group<C, N> candidate = groups.get(id);
			int size = candidate.countBlocks();

			if(size > bestSize) {
				best = candidate;
				bestId = id;
				bestSize = size;
			}
		}

		ArrayList<Group<C, N>> mergeGroups = new ArrayList<>(mergers.size() - 1);

		for(UUID id: mergers) {
			if(id.equals(bestId)) {
				continue;
			}

			Group<C, N> removed = groups.remove(id);
			final UUID target = bestId;

			// Remap each position to point to the correct group.
			removed.forEachPosition(position -> posGrouping.put(position, target));

			mergeGroups.add(removed);
		}

		MergeData<C, N> data = new MergeData<>();

		data.best = best;
		data.bestId = bestId;
		data.mergeGroups = mergeGroups;

		return data;
	}

	private ArrayList<UUID> getNeighboringGroups(BlockPos pos) {
		ArrayList<UUID> neighbors = new ArrayList<>(6);

		for(Direction direction: DIRECTIONS) {
			BlockPos face = pos.offset(direction);
			UUID group = posGrouping.get(face);

			if(group == null) {
				continue;
			}

			if(!neighbors.contains(group)) {
				neighbors.add(group);
			}
		}

		return neighbors;
	}

	private UUID getNewId() {
		UUID uuid = UUID.randomUUID();
		while(groups.containsKey(uuid)) {
			// Should never be called, but whatever.
			uuid = UUID.randomUUID();
		}

		return uuid;
	}

	// Wish Java had tuples...
	private static class MergeData<C extends IConnectable, N extends IConnectable> {
		Group<C, N> best;
		UUID bestId;
		ArrayList<Group<C, N>> mergeGroups;
	}
}
