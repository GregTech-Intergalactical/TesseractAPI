package zap.graph;

import gnu.trove.map.hash.TObjectByteHashMap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.BFSearcher;
import zap.graph.traverse.INodeContainer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Graph<C extends IConnectable, N extends IConnectable> implements INodeContainer {
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

	@Nullable
	public Entry<C, N> remove(BlockPos pos) {
		UUID uuid = posGrouping.remove(pos);

		if(uuid == null) {
			return null;
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

		return entry;
	}

	@SuppressWarnings("unchecked")
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

		Group<C, N>[] mergeGroups = new Group[mergers.size() - 1];
		int i = 0;
		for(UUID id: mergers) {
			if(id.equals(bestId)) {
				continue;
			}

			// TODO: This iterates over every entry in the graph.
			// TODO: It would be better to iterate over each entry in the Group instead.
			for(Map.Entry<BlockPos, UUID> posGroup: posGrouping.entrySet()) {
				if(posGroup.getValue().equals(id)) {
					posGroup.setValue(bestId);
				}
			}

			mergeGroups[i++] = groups.remove(id);
		}

		MergeData<C, N> data = new MergeData<>();

		data.best = best;
		data.bestId = bestId;
		data.mergeGroups = mergeGroups;

		return data;
	}

	private ArrayList<UUID> getNeighboringGroups(BlockPos pos) {
		ArrayList<UUID> neighbors = new ArrayList<>(6);

		for(EnumFacing facing: EnumFacing.VALUES) {
			BlockPos face = pos.offset(facing);
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
		Group<C, N>[] mergeGroups;
	}
}
