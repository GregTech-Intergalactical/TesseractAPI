package zap.graph;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zap.electric.api.IElectricCable;
import zap.electric.api.IElectricNode;
import zap.graph.traverse.BFSearcher;
import zap.graph.traverse.INodeContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Represents a set of endpoints connected by connectors, where every endpoint can reach another through a connector path.
 */
public class Grid implements INodeContainer {
	HashMap<BlockPos, ConnectorEntry> connectors;
	HashMap<BlockPos, IElectricNode> nodes;

	public Grid(BlockPos single, IElectricNode node) {
		connectors = new HashMap<>();
		nodes = new HashMap<>();

		nodes.put(single, node);
	}

	public void addConnector(BlockPos at, IElectricCable connector, byte connectivity) {
		ConnectorEntry entry = new ConnectorEntry();

		entry.connector = connector;
		entry.connectivity = connectivity;

		connectors.put(at, entry);
	}

	public void addNode(BlockPos pos, IElectricNode node) {
		nodes.put(pos, node);
	}

	@Override
	public boolean contains(BlockPos pos) {
		return connectors.containsKey(pos) || nodes.containsKey(pos);
	}

	public ArrayList<Grid> removeConnector(BlockPos connector) {
		if(connectors.remove(connector) == null) {
			return null;
		}

		/*HashSet<EnumFacing> importantSides = new HashSet<>();
		ArrayList<Grid> grids = new ArrayList<>();

		for(EnumFacing facing: EnumFacing.VALUES) {
			BlockPos pos = connector.offset(facing);

			IElectricNode node = nodes.remove(pos);
			if(node != null) {
				grids.add(new Grid(pos, node));
			} else if(connectors.containsKey(pos)) {
				importantSides.add(facing);
			}
		}

		if(importantSides.isEmpty())
			if (nodes.isEmpty() && !grids.isEmpty()) {
				Grid grid = grids.remove(grids.size() - 1);
				nodes.putAll(grid.nodes);
			}

			return grids;
		}*/

		TObjectIntHashMap<BlockPos> colors = new TObjectIntHashMap<>();
		TObjectIntHashMap<EnumFacing> coloredSides = new TObjectIntHashMap<>();
		int color = 0;

		BFSearcher searcher = new BFSearcher(this);

		for(EnumFacing facing: EnumFacing.VALUES) {
			BlockPos pos = connector.offset(facing);

			if(coloredSides.containsKey(facing)) {
				// Already colored!
				continue;
			} else if(!connectors.containsKey(pos)) {
				// Can't start from here.
				continue;
			}

			coloredSides.put(facing, color);

			final int targetColor = color;
			searcher.search(pos, reached -> colors.put(reached, targetColor));

			color++;
		}

		throw new UnsupportedOperationException("not implemented yet");
	}



	private static class ConnectorEntry {
		IElectricCable connector;
		byte connectivity;
	}
}
