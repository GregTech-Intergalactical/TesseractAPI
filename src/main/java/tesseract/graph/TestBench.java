package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static tesseract.util.Pos.packAll;

/**
 * Testing purpose class.
 */
class TestBench {

    public static void main(String[] args) throws Exception {

        Graph<ExampleConnector, ExampleNode> graph = new Graph<>();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = input.readLine().trim();
            System.out.println(line);

            if (line.startsWith("add")) {
                String[] points = line.split(" ");
                if (points.length < 4) {
                    System.out.println("Usage: add <x> <y> <z> [node or connector]");
                    continue;
                }

                Pos pos = new Pos(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                long position = pos.asLong();

                if (points.length == 5 && points[4].startsWith("c")) {
                    if (!graph.addConnector(position, Connectivity.Cache.of(new ExampleConnector()))) {
                        System.out.println("Error: connector at" + pos + " already exists in the graph");
                        continue;
                    }
                } else {
                    if (!graph.addNode(position, Connectivity.Cache.of(new ExampleNode()))) {
                        System.out.println("Error: node at" + pos + " already exists in the graph");
                        continue;
                    }
                }
                System.out.println("Added " + pos + " to the graph");

            } else if (line.startsWith("remove")) {
                String[] points = line.split(" ");
                if (points.length < 4) {
                    System.out.println("Usage: remove <x> <y> <z>");
                    continue;
                }

                Pos pos = new Pos(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));

                graph.removeAt(pos.asLong());

                System.out.println("Removed " + pos + " from the graph");

            } else if (line.startsWith("a*")) {
                String[] points = line.split(" ");
                if (points.length < 7) {
                    System.out.println("Usage: a* <x1> <y1> <z1> <x2> <y2> <z2> [crossroad]");
                    continue;
                }

                long origin = packAll(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                long target = packAll(Integer.parseInt(points[4]), Integer.parseInt(points[5]), Integer.parseInt(points[6]));

                for (Int2ObjectMap.Entry<Group<ExampleConnector, ExampleNode>> group : graph.getGroups().int2ObjectEntrySet()) {
                    for (Grid<ExampleConnector> grid : group.getValue().getGrids().values()) {
                        for (Node node : grid.getPath(origin, target)) {
                            System.out.println(node);
                        }
                    }
                }
                continue;
            } else if (line.startsWith("exit")) {
                return;
            }

            System.out.println("Graph contains " + graph.countGroups() + " groups:");

            for (Int2ObjectMap.Entry<Group<ExampleConnector, ExampleNode>> group : graph.getGroups().int2ObjectEntrySet()) {
                System.out.println("  Group " + group.getIntKey() + " contains " + group.getValue().countBlocks() + " blocks: ");

                for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleNode>> node : group.getValue().getNodes().long2ObjectEntrySet()) {
                    System.out.println("    Node at " +  new Pos(node.getLongKey()) + ": " + node.getValue().value());
                }

                for (Grid<ExampleConnector> grid : group.getValue().getGrids().values()) {
                    System.out.println("    Grid contains " + grid.countConnectors() + " connectors:");

                    for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleConnector>> connector : grid.getConnectors().long2ObjectEntrySet()) {
                        System.out.println("      Connector at " + new Pos(connector.getLongKey()) + ": " + connector.getValue().value());
                    }

                    int linked = grid.countNodes();
                    if (linked != 0) {
                        System.out.println("      Grid contains " + linked + " linked nodes:");
                        for (long pos : grid.getNodes().keySet()) {
                            System.out.println("          Node at " + new Pos(pos));
                        }
                    }
                }
            }

            System.out.println("_____________________________________________________________________________");
        }
    }

    private static class ExampleConnector implements IElectricCable, IConnectable {

        @Override
        public String toString() {
            return "ExampleCable";
        }

        @Override
        public boolean connects(@Nonnull Dir direction) {
            return true;
        }

        @Override
        public int getLoss() {
            return 0;
        }

        @Override
        public int getAmps() {
            return 0;
        }

        @Override
        public int getVoltage() {
            return 0;
        }
    }

    private static class ExampleNode implements IElectricNode, IConnectable {

        @Override
        public String toString() {
            return "ExampleNode";
        }

        @Override
        public boolean connects(@Nonnull Dir direction) {
            return true;
        }

        @Override
        public long insert(long maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public long getPower() {
            return 0L;
        }

        @Override
        public long getCapacity() {
            return 0L;
        }

        @Override
        public int getOutputAmperage() {
            return 0;
        }

        @Override
        public int getOutputVoltage() {
            return 0;
        }

        @Override
        public int getInputAmperage() {
            return 0;
        }

        @Override
        public int getInputVoltage() {
            return 0;
        }

        @Override
        public boolean canInput() {
            return false;
        }

        @Override
        public boolean canOutput(@Nonnull Dir direction) {
            return false;
        }

        @Override
        public boolean canOutput() {
            return false;
        }

        @Override
        public void reset(@Nullable ITickingController oldController, @Nullable ITickingController newController) {
            System.out.println("oldController: " + oldController + "| newController: " + newController);
        }
    }
}
