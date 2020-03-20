package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;
import tesseract.electric.api.*;
import tesseract.electric.base.*;
import tesseract.util.Dir;

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
        LongSet set1 = new LongLinkedOpenHashSet();
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids().values()) {
                set1 = grid.getPath(packAll(0, -1, 0), packAll(0, 6, 0), false);
            }
        }
        LongList set2 = new LongArrayList();
        set2.add(packAll(0, 6, 0));
        set2.add(packAll(0, 5, 0));
        set2.add(packAll(0, 4, 0));
        set2.add(packAll(0, 3, 0));
        set2.add(packAll(0, 2, 0));
        set2.add(packAll(0, 1, 0));
        set2.add(packAll(0, 0, 0));
        set2.add(packAll(0, -1, 0));
        LongIterator iterator = set1.iterator();
        for (int i = 0; i < set2.size(); i++) {
            assertEquals(set2.getLong(i),  iterator.nextLong());
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
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
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
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
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
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids().values()) {
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
        public long getLossPerBlock() {
            return 0;
        }

        @Override
        public IElectricLimits getPassageLimits() {
            return ElectricLimits.UNLIMITED;
        }

        @Override
        public String toString() {
            return "TestCable";
        }

        @Override
        public boolean connects(Dir direction) {
            return true;
        }
    }

    public static class TestNode implements IElectricNode, IConnectable {

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
            return "TestNode";
        }

        @Override
        public boolean connects(Dir direction) {
            return true;
        }
    }
}