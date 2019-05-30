package zap.graph;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zap.electric.api.IElectricCable;
import zap.electric.api.IElectricLimits;
import zap.electric.api.IElectricNode;
import zap.electric.api.IElectricStorage;
import zap.electric.base.ElectricLimits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

public class TestBench {
	public static void main(String[] args) throws Exception {
		Graph<IElectricCable, IElectricNode> graph = new Graph<>();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		while(true) {
			System.out.print("> ");
			String line = input.readLine().trim();
			System.out.println(line);

			if(line.startsWith("add")) {
				String[] adds = line.split(" ");
				if(adds.length < 4) {
					System.out.println("Usage: add <x> <y> <z> [endpoint or cable]");
				}

				BlockPos pos = new BlockPos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));

				if(!graph.contains(pos)) {
					if(adds.length == 5 && adds[4].startsWith("c")) {
						graph.addCable(pos, new ExampleCable());
					} else {
						graph.addEndpoint(pos, new ExampleNode());
					}

					System.out.println("Added "+pos+" to the graph");
				} else {
					System.out.println("Error: "+pos+" already exists in the graph");
				}

			} else if(line.startsWith("remove")) {
				String[] adds = line.split(" ");
				if(adds.length < 4) {
					System.out.println("Usage: remove <x> <y> <z>");
				}

				BlockPos pos = new BlockPos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));

				Entry<IElectricCable, IElectricNode> entry = graph.remove(pos);

				if(entry != null) {
					entry.apply(cable -> {
						System.out.println("Removed cable "+pos+" from the graph: "+cable);
					}, endpoint -> {
						System.out.println("Removed endpoint "+pos+" from the graph: "+endpoint);
					});
				} else {
					System.out.println("Error: "+pos+" doesn't exist in the graph");
				}

			} else if(line.startsWith("exit")) {
				return;
			}

			System.out.println("Graph contains "+graph.groups.size()+" groups:");
			for(Map.Entry<UUID, Group<IElectricCable, IElectricNode>> entry: graph.groups.entrySet()) {
				Group<IElectricCable, IElectricNode> group = entry.getValue();
				System.out.println("  Group "+entry.getKey()+" contains "+group.endpoints.size()+" endpoints and "+group.cables.size()+" cables:");

				for(Map.Entry<BlockPos, IElectricNode> nodeEntry: group.endpoints.entrySet()) {
					System.out.println("    Node at "+nodeEntry.getKey()+": "+nodeEntry.getValue());
				}

				for(Map.Entry<BlockPos, IElectricCable> cableEntry: group.cables.entrySet()) {
					System.out.println("    Cable at "+cableEntry.getKey()+": "+cableEntry.getValue());
				}
			}
		}
	}

	private static class ExampleCable implements IElectricCable {
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
	}

	private static class ExampleNode implements IElectricNode {
		public IElectricStorage getStorage(EnumFacing facing) {
			return null;
		}

		public IElectricLimits getReceiverLimits(EnumFacing facing) {
			return null;
		}

		public int getOfferedPackets() {
			return 0;
		}

		@Override
		public String toString() {
			return "ExampleNode";
		}
	}
}
