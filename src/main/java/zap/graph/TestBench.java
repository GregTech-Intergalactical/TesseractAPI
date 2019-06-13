package zap.graph;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import zap.electric.api.IElectricCable;
import zap.electric.api.IElectricLimits;
import zap.electric.api.IElectricNode;
import zap.electric.api.IElectricStorage;
import zap.electric.base.ElectricLimits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TestBench {
	public static void main(String[] args) throws Exception {
		Graph<ExampleCable, ExampleNode> graph = new Graph<>();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		while(true) {
			System.out.print("> ");
			String line = input.readLine().trim();
			System.out.println(line);

			if(line.startsWith("add")) {
				String[] adds = line.split(" ");
				if(adds.length < 4) {
					System.out.println("Usage: add <x> <y> <z> [node or connector]");
					continue;
				}

				BlockPos pos = new BlockPos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));

				if(!graph.contains(pos)) {
					if(adds.length == 5 && adds[4].startsWith("c")) {
						graph.addConnector(pos, new ExampleCable());
					} else {
						graph.addNode(pos, new ExampleNode());
					}

					System.out.println("Added "+pos+" to the graph");
				} else {
					System.out.println("Error: "+pos+" already exists in the graph");
				}

			} else if(line.startsWith("remove")) {
				String[] adds = line.split(" ");
				if(adds.length < 4) {
					System.out.println("Usage: remove <x> <y> <z>");
					continue;
				}

				BlockPos pos = new BlockPos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));

				Optional<Entry<ExampleCable, ExampleNode>> entry = graph.remove(pos);

				if(entry.isPresent()) {
					entry.get().apply(connector -> {
						System.out.println("Removed connector "+pos+" from the graph: "+connector);
					}, node -> {
						System.out.println("Removed node "+pos+" from the graph: "+node);
					});
				} else {
					System.out.println("Error: "+pos+" doesn't exist in the graph");
				}

			} else if(line.startsWith("exit")) {
				return;
			}

			System.out.println("Graph contains "+graph.groups.size()+" groups:");
			for(Map.Entry<UUID, Group<ExampleCable, ExampleNode>> entry: graph.groups.entrySet()) {
				Group<ExampleCable, ExampleNode> group = entry.getValue();
				System.out.println("  Group "+entry.getKey()+" contains "+group.nodes.size()+" nodes and "+group.grids.size()+" grids:");

				for(Map.Entry<BlockPos, Connectivity.Cache<ExampleNode>> nodeEntry: group.nodes.entrySet()) {
					System.out.println("    Node at "+nodeEntry.getKey()+": "+nodeEntry.getValue().value());
				}

				for(Map.Entry<UUID, Grid<ExampleCable>> gridEntry: group.grids.entrySet()) {
					Grid<ExampleCable> grid = gridEntry.getValue();
					System.out.println("    Grid "+gridEntry.getKey()+" contains "+grid.connectors.size());

					for(Map.Entry<BlockPos, Connectivity.Cache<ExampleCable>> connectorEntry: grid.connectors.entrySet()) {
						System.out.println("      Connector at "+connectorEntry.getKey()+": "+connectorEntry.getValue().value());
					}
				}
			}
		}
	}

	private static class ExampleCable implements IElectricCable, IConnectable {
		@Override
		public long getLossPerBlock() {
			return 0;
		}

		@Override
		public IElectricLimits getPassageLimits() {
			return ElectricLimits.UNLIMITED;
		}

		@Override
		public String toString() {
			return "ExampleCable";
		}

		@Override
		public boolean connects(Direction direction) {
			return true;
		}
	}

	private static class ExampleNode implements IElectricNode, IConnectable {
		public IElectricStorage getStorage(Direction direction) {
			return null;
		}

		public IElectricLimits getReceiverLimits(Direction direction) {
			return null;
		}

		public int getOfferedPackets() {
			return 0;
		}

		@Override
		public String toString() {
			return "ExampleNode";
		}

		@Override
		public boolean connects(Direction direction) {
			return true;
		}
	}
}
