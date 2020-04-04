package tesseract.graph;

import org.junit.Test;
import tesseract.api.IConnectable;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static tesseract.util.Pos.packAll;
import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void system() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addConnector(packAll(1, 0, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 1, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 2, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 3, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 4, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 5, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 6, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 0, 1), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 0, -1), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, -1, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(-1, 0, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(6, graph.countGroups());
        graph.addConnector(packAll(0, 0, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(1, graph.countGroups());
        graph.remove(packAll(0, 0, 0));
        assertEquals(6, graph.countGroups());
        graph.remove(packAll(0, 4, 0));
        assertEquals(7, graph.countGroups());
        graph.addConnector(packAll(0, 0, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(0, 4, 0), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(1, graph.countGroups());
        ArrayDeque<Node> set1 = new ArrayDeque<>();
        for (Group<TestCable, TestNode> group : graph.getGroups().values()) {
            for (Grid<GraphTest.TestCable> grid : group.getGrids().values()) {
                set1 = grid.getPath(packAll(0, -1, 0), packAll(0, 6, 0));
            }
        }
        List<Pos> set2 = new ArrayList<>();
        set2.add(new Pos(0, -1, 0));
        set2.add(new Pos(0, 0, 0));
        set2.add(new Pos(0, 1, 0));
        set2.add(new Pos(0, 2, 0));
        set2.add(new Pos(0, 3, 0));
        set2.add(new Pos(0, 4, 0));
        set2.add(new Pos(0, 5, 0));
        set2.add(new Pos(0, 6, 0));
        Iterator<Node> iterator = set1.descendingIterator();
        for (Pos pos : set2) {
            assertEquals(pos, iterator.next());
        }
    }

    @Test
    public void contains() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(1, 1, 1);
        assertFalse(graph.contains(pos));
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.contains(pos));
    }

    @Test
    public void linked() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos1 = packAll(0, 0, 0);
        long pos2 = packAll(0, 1, 0);
        graph.addNode(pos1, Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(pos2, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.linked(pos1, null, pos2));
    }

    @Test
    public void connects() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(0, 0, 0);
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.connects(pos, null));
    }

    @Test
    public void visit() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(packAll(5, 5, 5), Connectivity.Cache.of(new GraphTest.TestNode()));
        for (Group<TestCable, TestNode> group : graph.getGroups().values()) {
            assertEquals(1, group.countBlocks());
        }
    }

    @Test
    public void countGroups() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(packAll(0, 0, 0), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(packAll(1, 1, 1), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(packAll(2, 2, 2), Connectivity.Cache.of(new GraphTest.TestNode()));
        assertEquals(3, graph.countGroups());
    }

    @Test
    public void addNode() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(5, 5, 5);
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        for (Group<TestCable, TestNode> group : graph.getGroups().values()) {
            for (long position : group.getNodes().keySet()) {
                assertEquals(position, pos);
            }
        }
    }

    @Test
    public void addConnector() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(2, 2, 2);
        graph.addConnector(pos, Connectivity.Cache.of(new GraphTest.TestCable()));
        for (Group<TestCable, TestNode> group : graph.getGroups().values()) {
            for (Grid<GraphTest.TestCable> grid : group.getGrids().values()) {
                for (long position : grid.getConnectors().keySet()) {
                    assertEquals(position, pos);
                }
            }
        }
    }

    @Test
    public void remove() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(0, 0, 0);
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertEquals(1, graph.countGroups());
        graph.remove(pos);
        assertEquals(0, graph.countGroups());
    }

    public static class TestCable implements IElectricCable, IConnectable {

        @Override
        public String toString() {
            return "TestCable";
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
            return 0L;
        }
    }

    public static class TestNode implements IElectricNode, IConnectable {

        @Override
        public String toString() {
            return "TestNode";
        }

        @Override
        public boolean connects(Dir direction) {
            return true;
        }

        @Override
        public long insert(long maxReceive, boolean simulate) {
            return 0L;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            return 0L;
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
        public long getOutputAmperage() {
            return 0L;
        }

        @Override
        public long getOutputVoltage() {
            return 0L;
        }

        @Override
        public long getInputAmperage() {
            return 0L;
        }

        @Override
        public long getInputVoltage() {
            return 0L;
        }

        @Override
        public boolean canInput() {
            return false;
        }

        @Override
        public boolean canOutput() {
            return false;
        }
    }
}