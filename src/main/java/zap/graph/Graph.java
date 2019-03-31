package zap.graph;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Graph<C, E> {
	HashMap<BlockPos, UUID> posGrouping;
	HashMap<UUID, Group<C, E>> groups;

	public Graph() {
		posGrouping = new HashMap<>();
		groups = new HashMap<>();
	}

	public boolean contains(BlockPos pos) {
		return posGrouping.containsKey(pos);
	}

	@SuppressWarnings("unchecked")
	public void addEndpoint(BlockPos pos, E endpoint) {
		ArrayList<UUID> mergers = getNeighboringGroups(pos);

		if(mergers.size()==0) {
			UUID uuid = getNewId();

			posGrouping.put(pos, uuid);
			groups.put(uuid, (Group<C, E>)Group.singleEndpoint(pos, endpoint));
		} else if(mergers.size()==1) {
			UUID uuid = mergers.get(0);

			posGrouping.put(pos, uuid);
			groups.get(uuid).addEndpoint(pos, endpoint);
		} else {
			UUID bestId = mergers.get(0);
			Group<C, E> best = groups.get(bestId);
			int bestSize = best.countBlocks();

			for(UUID id: mergers) {
				Group<C, E> candidate = groups.get(id);
				int size = candidate.countBlocks();

				if(size > bestSize) {
					best = candidate;
					bestId = id;
					bestSize = size;
				}
			}

			Group<C, E>[] mergeGroups = new Group[mergers.size() - 1];
			int i = 0;
			for(UUID id: mergers) {
				if(id.equals(bestId)) {
					continue;
				}

				for(Map.Entry<BlockPos, UUID> posGroup: posGrouping.entrySet()) {
					if(posGroup.getValue().equals(id)) {
						posGroup.setValue(bestId);
					}
				}

				mergeGroups[i++] = groups.remove(id);
			}

			posGrouping.put(pos, bestId);
			best.addMergingEndpoint(pos, endpoint, mergeGroups);
		}
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
}
