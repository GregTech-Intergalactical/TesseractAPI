package zap.graph;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.INodeContainer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Grid<C extends IConnectable> implements INodeContainer {
	HashMap<BlockPos, Connectivity.Cache<C>> connectors;

	private Grid() {
		connectors = new HashMap<>();
	}

	public static <C extends IConnectable> Grid<C> singleConnector(BlockPos pos, C connector) {
		Grid<C> grid = new Grid<>();

		grid.connectors.put(pos, Connectivity.Cache.of(connector));

		return grid;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return connectors.containsKey(pos);
	}

	@Override
	public boolean connects(BlockPos pos, EnumFacing towards) {
		Connectivity.Cache<C> cache = connectors.get(pos);
		
		if(cache == null) {
			return false;
		}

		return cache.connects(towards);
	}
	
	public void addConnector(BlockPos pos, Connectivity.Cache<C> connector) {
		connectors.put(pos, connector);
	}
}