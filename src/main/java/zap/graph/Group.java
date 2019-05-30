package zap.graph;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.INodeContainer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Group<C extends IConnectable, N extends IConnectable> implements INodeContainer {
	HashMap<BlockPos, N> nodes;
	HashMap<BlockPos, C> connectors;

	// Prevent the creation of empty groups, a caller needs to use singleNode/singleConnector.
	private Group() {
		nodes = new HashMap<>();
		connectors = new HashMap<>();
	}

	public static <N extends IConnectable> Group<?, N> singleNode(BlockPos at, N node) {
		Group<?, N> group = new Group<>();

		group.addNode(at, node);

		return group;
	}

	public static <C extends IConnectable> Group<C, ?> singleConnector(BlockPos at, C connector) {
		Group<C, ?> group = new Group<>();

		group.addConnector(at, connector);

		return group;
	}

	public int countBlocks() {
		return nodes.size() + connectors.size();
	}

	@Override
	public boolean contains(BlockPos at) {
		Objects.requireNonNull(at);

		return nodes.containsKey(at) || connectors.containsKey(at);
	}

	// TODO: The Graph may add/remove entries that do not connect at the time of the method call.
	// TODO: This should be avoided if at all possible.
	public void addEntry(BlockPos at, Entry<C, N> entry) {
		entry.apply(
				connector -> connectors.put(Objects.requireNonNull(at), connector),
				node -> nodes.put(Objects.requireNonNull(at), node)
		);
	}

	public void addNode(BlockPos at, N node) {
		nodes.put(Objects.requireNonNull(at), Objects.requireNonNull(node));
	}

	public void addConnector(BlockPos at, C connector) {
		connectors.put(Objects.requireNonNull(at), Objects.requireNonNull(connector));
	}

	@Nullable
	public Entry<C, N> remove(BlockPos pos) {
		N node = nodes.remove(pos);
		C connector = connectors.remove(pos);

		if(node != null) {
			return Entry.node(node);
		} else if(connector != null) {
			return Entry.connector(connector);
		} else {
			return null;
		}
	}

	// Graph controlled interface
	void mergeWith(Group<C, N> other) {
		nodes.putAll(other.nodes);
		connectors.putAll(other.connectors);
	}
}
