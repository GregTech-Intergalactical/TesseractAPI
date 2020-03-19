package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricLimits;
import tesseract.electric.api.IElectricNode;
import tesseract.electric.api.IElectricStorage;
import tesseract.electric.base.ElectricLimits;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;

/**
 * Testing purpose class
 */
class TestBench {

    public static void main(String[] args) throws Exception {

        Graph<ExampleCable, ExampleNode> graph = new Graph<>();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = input.readLine().trim();
            System.out.println(line);

            if (line.startsWith("add")) {
                String[] adds = line.split(" ");
                if (adds.length < 4) {
                    System.out.println("Usage: add <x> <y> <z> [node or connector]");
                    continue;
                }

                Pos pos = new Pos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));
                long position = pos.get();

                if (!graph.contains(position)) {
                    if (adds.length == 5 && adds[4].startsWith("c")) {
                        graph.addConnector(position, Connectivity.Cache.of(new ExampleCable()));
                    } else {
                        graph.addNode(position, Connectivity.Cache.of(new ExampleNode()));
                    }

                    System.out.println("Added " + pos + " to the graph");
                } else {
                    System.out.println("Error: " + pos + " already exists in the graph");
                }

            } else if (line.startsWith("remove")) {
                String[] adds = line.split(" ");
                if (adds.length < 4) {
                    System.out.println("Usage: remove <x> <y> <z>");
                    continue;
                }

                Pos pos = new Pos(Integer.parseInt(adds[1]), Integer.parseInt(adds[2]), Integer.parseInt(adds[3]));
                long position = pos.get();

                Optional<Entry<ExampleCable, ExampleNode>> entry = graph.remove(position);

                if (entry.isPresent()) {
                    entry.get().apply(
                        connector -> System.out.println("Removed connector " + pos + " from the graph: " + connector),
                        node -> System.out.println("Removed node " + pos + " from the graph: " + node)
                    );
                } else {
                    System.out.println("Error: " + pos + " doesn't exist in the graph");
                }
            } else if (line.startsWith("a*")) {
                String[] star = line.split(" ");
                if (star.length < 7) {
                    System.out.println("Usage: a* <x1> <y1> <z1> <x2> <y2> <z2> [crossroad]");
                    continue;
                }

                Pos start = new Pos(Integer.parseInt(star[1]), Integer.parseInt(star[2]), Integer.parseInt(star[3]));
                Pos end = new Pos(Integer.parseInt(star[4]), Integer.parseInt(star[5]), Integer.parseInt(star[6]));

                System.out.println("findPath ->");
                for (Object2ObjectMap.Entry<UUID, Group<ExampleCable, ExampleNode>> group : graph.getGroups().object2ObjectEntrySet()) {
                    for (IGrid<ExampleCable> grid : group.getValue().getGrids()) {
                        for (long pos : grid.getPath(start.get(), end.get(), star.length == 8 && star[7].startsWith("x"))) {
                            System.out.println(new Pos(pos));
                        }
                    }
                }
                continue;
            } else if (line.startsWith("exit")) {
                return;
            }

            System.out.println("Graph contains " + graph.countGroups() + " groups:");

            for (Object2ObjectMap.Entry<UUID, Group<ExampleCable, ExampleNode>> group : graph.getGroups().object2ObjectEntrySet()) {
                System.out.println("  Group " + group.getKey() + " contains " + group.getValue().countBlocks() + " blocks: ");

                for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleNode>> node : group.getValue().getNodes().long2ObjectEntrySet()) {
                    System.out.println("    Node at " +  new Pos(node.getLongKey()) + ": " + node.getValue().value());
                }

                for (IGrid<ExampleCable> grid : group.getValue().getGrids()) {
                    System.out.println("    Grid contains " + grid.countConnectors() + " connectors:");
                    for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleCable>> connector : grid.getConnectors().long2ObjectEntrySet()) {
                        System.out.println("      Connector at " + new Pos(connector.getLongKey()) + ": " + connector.getValue().value());
                    }
                }
            };
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
        public boolean connects(Dir direction) {
            return true;
        }
    }

    private static class ExampleNode implements IElectricNode, IConnectable {

        public IElectricStorage getStorage(Dir direction) {
            return null;
        }

        public IElectricLimits getReceiverLimits(Dir direction) {
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
        public boolean connects(Dir direction) {
            return true;
        }
    }
}
