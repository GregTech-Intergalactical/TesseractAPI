package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.Direction;
import tesseract.api.IConnectable;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static tesseract.util.Pos.packAll;

/**
 * Testing purpose class.
 */
class TestBench {

    public static void main(String[] args) throws Exception {

        Graph<Integer, ExampleConnector, ExampleNode> graph = new Graph<>();
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
                    if (!graph.addConnector(position, new Cache<>(new ExampleConnector()), null)) {
                        System.out.println("Error: connector at" + pos + " already exists in the graph");
                        continue;
                    }
                } else {
                    for (Direction d : Graph.DIRECTIONS) {
                        long posC = Pos.offset(position, d);
                        Group<Integer, ExampleConnector, ExampleNode> group = graph.getGroupAt(posC);
                        if (group == null)
                            continue;
                        Cache<ExampleConnector> val = group.getConnector(posC);
                        if (val != null) {
                            if (!graph.addNode(position, (a,b) -> new ExampleNode(), Pos.subToDir(posC, position), () -> null, true)) {
                                System.out.println("error");
                            }
                        }

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

                for (Int2ObjectMap.Entry<Group<Integer, ExampleConnector, ExampleNode>> group : graph.getGroups()
                        .int2ObjectEntrySet()) {
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

            for (Int2ObjectMap.Entry<Group<Integer, ExampleConnector, ExampleNode>> group : graph.getGroups()
                    .int2ObjectEntrySet()) {
                System.out
                        .println("  Group " + group.getIntKey() + " contains " + group.getValue().countBlocks() + " blocks: ");

                for (Long2ObjectMap.Entry<NodeCache<ExampleNode>> node : group.getValue().getNodes().long2ObjectEntrySet()) {
                    System.out.println("    Node at " + new Pos(node.getLongKey()) + ": " + node.getValue().value());
                }

                for (Grid<ExampleConnector> grid : group.getValue().getGrids().values()) {
                    System.out.println("    Grid contains " + grid.countConnectors() + " connectors:");

                    for (Long2ObjectMap.Entry<Cache<ExampleConnector>> connector : grid.getConnectors().long2ObjectEntrySet()) {
                        System.out
                                .println("      Connector at " + new Pos(connector.getLongKey()) + ": " + connector.getValue().value());
                    }

                    int linked = grid.countNodes();
                    if (linked != 0) {
                        System.out.println("      Grid contains " + linked + " linked nodes:");
                        // for (long pos : grid.getNodes()) {
                        // System.out.println(" Node at " + new Pos(pos));
                        // }
                    }
                }
            }

            System.out.println("_____________________________________________________________________________");
        }
    }

    private static class ExampleConnector implements IConnectable {

        @Override
        public String toString() {
            return "ExampleCable";
        }

        @Override
        public boolean connects(Direction direction) {
            return true;
        }

        @Override
        public boolean validate(Direction dir) {
            return true;
        }
    }

    private static class ExampleNode {

        @Override
        public String toString() {
            return "ExampleNode";
        }
    }
}
