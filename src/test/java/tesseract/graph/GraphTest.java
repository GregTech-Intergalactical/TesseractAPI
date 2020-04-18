package tesseract.graph;

import org.junit.Test;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static tesseract.util.Pos.packAll;
import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void system() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        graph.addConnector(packAll(1, 0, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 1, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 2, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 3, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 5, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 6, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 0, 1), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 0, -1), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, -1, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(-1, 0, 0), new Cache<>(new TestConnector()));
        assertEquals(6, graph.countGroups());
        graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector()));
        assertEquals(1, graph.countGroups());
        graph.removeAt(packAll(0, 0, 0));
        assertEquals(6, graph.countGroups());
        graph.removeAt(packAll(0, 4, 0));
        assertEquals(7, graph.countGroups());
        graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector()));
        graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector()));
        assertEquals(1, graph.countGroups());
        ArrayDeque<Node> set1 = new ArrayDeque<>();
        for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
            for (Grid<TestConnector> grid : group.getGrids().values()) {
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
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(1, 1, 1);
        assertFalse(graph.contains(pos));
        graph.addNode(pos, new Cache<>(new GraphTest.TestNode()));
        assertTrue(graph.contains(pos));
    }

    @Test
    public void linked() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos1 = packAll(0, 0, 0);
        long pos2 = packAll(0, 1, 0);
        graph.addNode(pos1, new Cache<>(new GraphTest.TestNode()));
        graph.addNode(pos2, new Cache<>(new GraphTest.TestNode()));
        assertTrue(graph.linked(pos1, null, pos2));
    }

    @Test
    public void connects() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(0, 0, 0);
        graph.addNode(pos, new Cache<>(new GraphTest.TestNode()));
        assertTrue(graph.connects(pos, null));
    }

    @Test
    public void visit() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(packAll(5, 5, 5), new Cache<>(new GraphTest.TestNode()));
        for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
            assertEquals(1, group.countBlocks());
        }
    }

    @Test
    public void countGroups() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(packAll(0, 0, 0), new Cache<>(new GraphTest.TestNode()));
        graph.addNode(packAll(1, 1, 1), new Cache<>(new GraphTest.TestNode()));
        graph.addNode(packAll(2, 2, 2), new Cache<>(new GraphTest.TestNode()));
        assertEquals(3, graph.countGroups());
    }

    @Test
    public void addNode() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(5, 5, 5);
        graph.addNode(pos, new Cache<>(new GraphTest.TestNode()));
        for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
            for (long position : group.getNodes().keySet()) {
                assertEquals(position, pos);
            }
        }
    }

    @Test
    public void addConnector() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(2, 2, 2);
        graph.addConnector(pos, new Cache<>(new TestConnector()));
        for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
            for (Grid<TestConnector> grid : group.getGrids().values()) {
                for (long position : grid.getConnectors().keySet()) {
                    assertEquals(position, pos);
                }
            }
        }
    }

    @Test
    public void remove() {
        Graph<TestConnector, GraphTest.TestNode> graph = new Graph<>();
        long pos = packAll(0, 0, 0);
        graph.addNode(pos, new Cache<>(new GraphTest.TestNode()));
        assertEquals(1, graph.countGroups());
        graph.removeAt(pos);
        assertEquals(0, graph.countGroups());
    }

    public static class TestConnector implements IElectricCable, IConnectable {

        @Override
        public String toString() {
            return "TestCable";
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

    public static class TestNode implements IElectricNode, IConnectable {

        @Override
        public String toString() {
            return "TestNode";
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
        }
    }
}