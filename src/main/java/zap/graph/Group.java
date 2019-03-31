package zap.graph;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class Group<C, E> {
	HashMap<BlockPos, E> endpoints;
	HashMap<BlockPos, C> cables;

	public Group() {
		endpoints = new HashMap<>();
		cables = new HashMap<>();
	}

	public static <E> Group<?, E> singleEndpoint(BlockPos at, E endpoint) {
		Group<?, E> group = new Group<>();

		group.addEndpoint(at, endpoint);

		return group;
	}

	public static <C, E> Group<C, E> singleCable(BlockPos at, C cable, Class<E> type) {
		Group<C, E> group = new Group<>();

		group.cables.put(at, cable);

		return group;
	}

	public int countBlocks() {
		return endpoints.size() + cables.size();
	}

	public void addEndpoint(BlockPos at, E endpoint) {
		endpoints.put(at, endpoint);
	}

	public void addMergingEndpoint(BlockPos at, E endpoint, Group<C, E>[] merged) {
		addEndpoint(at, endpoint);

		for(Group<C, E> other: merged) {
			endpoints.putAll(other.endpoints);
			cables.putAll(other.cables);
		}
	}
}
