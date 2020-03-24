package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Iterator;

import static tesseract.util.Pos.packAll;

/**
 * Testing purpose class.
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
                String[] points = line.split(" ");
                if (points.length < 4) {
                    System.out.println("Usage: add <x> <y> <z> [node or connector]");
                    continue;
                }

                Pos pos = new Pos(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                long position = pos.get();

                if (points.length == 5 && points[4].startsWith("c")) {
                    if (!graph.addConnector(position, Connectivity.Cache.of(new ExampleCable()))) {
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
                long position = pos.get();

                Entry<ExampleCable, ExampleNode> entry = graph.remove(position);

                if (entry != null) {
                    entry.apply(
                        connector -> System.out.println("Removed connector " + pos + " from the graph: " + connector),
                        node -> System.out.println("Removed node " + pos + " from the graph: " + node)
                    );
                } else {
                    System.out.println("Error: " + pos + " doesn't exist in the graph");
                }
            } else if (line.startsWith("a*")) {
                String[] points = line.split(" ");
                if (points.length < 7) {
                    System.out.println("Usage: a* <x1> <y1> <z1> <x2> <y2> <z2> [crossroad]");
                    continue;
                }

                long start = packAll(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                long end = packAll(Integer.parseInt(points[4]), Integer.parseInt(points[5]), Integer.parseInt(points[6]));

                System.out.println("findPath ->");
                for (Int2ObjectMap.Entry<Group<ExampleCable, ExampleNode>> group : graph.getGroups().int2ObjectEntrySet()) {
                    for (IGrid<ExampleCable> grid : group.getValue().getGrids().values()) {
                        for (Node node : grid.findPath(start, end)) {
                            System.out.println(node);
                        }
                    }
                }
                continue;
            } else if (line.startsWith("path")) {
                String[] points = line.split(" ");
                if (points.length < 3) {
                    System.out.println("Usage: cross <x1> <y1> <z1>");
                    continue;
                }

                long pos = packAll(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                Group<ExampleCable, ExampleNode> group = graph.findGroup(pos);
                if (group != null) {
                    Connectivity.Cache<ExampleNode> node = group.getNodes().get(pos);
                    if (node != null) {

                        for (Grid<ExampleCable> grid : group.findGrids(pos)) {
                            for (ArrayDeque<Node> path : grid.getPath(pos)) {
                                Iterator<Node> iterator = path.descendingIterator();

                                while(iterator.hasNext()) {
                                    System.out.println(iterator.next());
                                }
                                System.out.println("(-)");
                            }
                        }
                    }
                }
            } else if (line.startsWith("cross")) {
                String[] points = line.split(" ");
                if (points.length < 3) {
                    System.out.println("Usage: cross <x1> <y1> <z1>");
                    continue;
                }

                long pos = packAll(Integer.parseInt(points[1]), Integer.parseInt(points[2]), Integer.parseInt(points[3]));
                Group<ExampleCable, ExampleNode> group = graph.findGroup(pos);
                if (group != null) {
                    Connectivity.Cache<ExampleNode> node = group.getNodes().get(pos);
                    if (node != null) {
                        for (Grid<ExampleCable> grid : group.findGrids(pos)) {
                            for (ArrayDeque<Node> path : grid.getPath(pos)) {
                                Iterator<Node> iterator = path.descendingIterator();

                                while(iterator.hasNext()) {
                                    Node current = iterator.next();
                                    if (current.isCrossroad()) {
                                        System.out.println(current);
                                    }
                                }
                                System.out.println("(-)");
                            }
                        }
                    }
                }
            } else if (line.startsWith("exit")) {
                return;
            }

            System.out.println("Graph contains " + graph.countGroups() + " groups:");

            for (Int2ObjectMap.Entry<Group<ExampleCable, ExampleNode>> group : graph.getGroups().int2ObjectEntrySet()) {
                System.out.println("  Group " + group.getIntKey() + " contains " + group.getValue().countBlocks() + " blocks: ");

                for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleNode>> node : group.getValue().getNodes().long2ObjectEntrySet()) {
                    System.out.println("    Node at " +  new Pos(node.getLongKey()) + ": " + node.getValue().value());
                }

                for (IGrid<ExampleCable> grid : group.getValue().getGrids().values()) {
                    System.out.println("    Grid contains " + grid.countConnectors() + " connectors:");

                    for (Long2ObjectMap.Entry<Connectivity.Cache<ExampleCable>> connector : grid.getConnectors().long2ObjectEntrySet()) {
                        System.out.println("      Connector at " + new Pos(connector.getLongKey()) + ": " + connector.getValue().value());
                    }

                    int linked = grid.countNodes();
                    if (linked != 0) {
                        System.out.println("      Grid contains " + linked + " linked nodes:");
                        for (long pos : grid.getNodes().keySet()) {
                            System.out.println("          Node at " + new Pos(pos));
                        }

                        /*for (ObjectSet<LongSet> paths : grid.getCrossroads().values()) {
                            System.out.println("              Start at");
                            for (LongSet longs : paths) {
                                System.out.println("                Group");
                                for (long pos : longs) {
                                    System.out.println("                  Pos at " + new Pos(pos));
                                }
                            }
                            System.out.println("              Ended");
                        }*/
                    }
                }
            }

            System.out.println("_____________________________________________________________________________");
        }
    }

    private static class ExampleCable implements IElectricCable, IConnectable {

        @Override
        public String toString() {
            return "ExampleCable";
        }

        @Override
        public boolean connects(Dir direction) {
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
        public long getVoltage() {
            return 0;
        }
    }

    private static class ExampleNode implements IElectricNode, IConnectable {

        @Override
        public String toString() {
            return "ExampleNode";
        }

        @Override
        public boolean connects(Dir direction) {
            return true;
        }

        @Override
        public long getEnergyStored() {
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            return 0;
        }

        @Override
        public long getOutputAmperage() {
            return 0;
        }

        @Override
        public long getOutputVoltage() {
            return 0;
        }

        @Override
        public long getInputAmperage() {
            return 0;
        }

        @Override
        public long getInputVoltage() {
            return 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public boolean canExtract() {
            return false;
        }
    }
}
