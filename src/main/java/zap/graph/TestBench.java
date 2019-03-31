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
		IElectricCable superCable = new IElectricCable() {
			@Override
			public long getLossPerBlock() {
				return 0;
			}

			@Override
			public IElectricLimits getPassageLimits() {
				return ElectricLimits.UNLIMITED;
			}
		};

		Graph<IElectricCable, IElectricNode> graph = new Graph<>();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		while(true) {
			System.out.print("> ");
			String line = input.readLine().trim();
			System.out.println(line);

			if(line.startsWith("add")) {
				String[] adds = line.split(" ");
				if(adds.length < 4) {
					System.out.println("Usage: add <x> <y> <z>");
				}

				BlockPos pos = new BlockPos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));

				if(!graph.contains(pos)) {
					graph.addEndpoint(pos, new ExampleNode());
					System.out.println("Added "+pos+" to the graph");
				} else {
					System.out.println("Error: "+pos+" already exists in the graph");
				}

			} else if(line.startsWith("exit")) {
				return;
			}

			System.out.println("Graph contains "+graph.groups.size()+" groups:");
			for(Map.Entry<UUID, Group<IElectricCable, IElectricNode>> entry: graph.groups.entrySet()) {
				Group<IElectricCable, IElectricNode> group = entry.getValue();
				System.out.println("  Group "+entry.getKey()+" contains "+group.endpoints.size()+" endpoints:");

				for(Map.Entry<BlockPos, IElectricNode> nodeEntry: group.endpoints.entrySet()) {
					System.out.println("    Node at "+nodeEntry.getKey()+": "+nodeEntry.getValue());
				}
			}
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
