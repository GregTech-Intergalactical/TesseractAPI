package zap.graph;

import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.INodeContainer;

import javax.annotation.Nullable;
import java.util.HashMap;

public class Group<C, E> implements INodeContainer {
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

	public static <C> Group<C, ?> singleCable(BlockPos at, C cable) {
		Group<C, ?> group = new Group<>();

		group.cables.put(at, cable);

		return group;
	}

	public int countBlocks() {
		return endpoints.size() + cables.size();
	}

	@Override
	public boolean contains(BlockPos at) {
		return endpoints.containsKey(at) || cables.containsKey(at);
	}

	public void addEntry(BlockPos at, Entry<C, E> entry) {
		entry.apply(cable -> cables.put(at, cable), endpoint -> endpoints.put(at, endpoint));
	}

	public void addEndpoint(BlockPos at, E endpoint) {
		endpoints.put(at, endpoint);
	}

	public void addCable(BlockPos at, C cable) {
		cables.put(at, cable);
	}

	@Nullable
	public Entry<C, E> remove(BlockPos pos) {
		E endpoint = endpoints.remove(pos);
		C cable = cables.remove(pos);

		if(endpoint != null) {
			return Entry.endpoint(endpoint);
		} else if(cable != null) {
			return Entry.cable(cable);
		} else {
			return null;
		}
	}

	public void addMergingEndpoint(BlockPos at, E endpoint, Group<C, E>[] merged) {
		addEndpoint(at, endpoint);

		for(Group<C, E> other: merged) {
			endpoints.putAll(other.endpoints);
			cables.putAll(other.cables);
		}
	}

	public void addMergingCable(BlockPos at, C cable, Group<C, E>[] merged) {
		addCable(at, cable);

		for(Group<C, E> other: merged) {
			endpoints.putAll(other.endpoints);
			cables.putAll(other.cables);
		}
	}
}
