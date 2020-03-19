package tesseract.graph;

import org.junit.Test;
import tesseract.electric.api.*;
import tesseract.electric.base.*;
import tesseract.util.Dir;
import tesseract.util.Pos;

import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void system() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addConnector(new Pos(1, 0, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 1, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 2, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 3, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 4, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 5, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 6, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 0, 1).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 0, -1).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, -1, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(-1, 0, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(6, graph.countGroups());
        graph.addConnector(new Pos(0, 0, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(1, graph.countGroups());
        graph.remove(new Pos(0, 0, 0).get());
        assertEquals(6, graph.countGroups());
        graph.remove(new Pos(0, 4, 0).get());
        assertEquals(7, graph.countGroups());
        graph.addConnector(new Pos(0, 0, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(new Pos(0, 4, 0).get(), Connectivity.Cache.of(new GraphTest.TestCable()));
        assertEquals(1, graph.countGroups());
        long[] array = new long[8]; int i = 0;
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids()) {
                for (long pos : grid.getPath(new Pos(0, -1, 0).get(), new Pos(0, 6, 0).get(), false)) {
                    array[i] = pos;
                    i++;
                }
            }
        }
        long[] pos = new long[8];
        pos[0] = new Pos(0, 6, 0).get();
        pos[1] = new Pos(0, 5, 0).get();
        pos[2] = new Pos(0, 4, 0).get();
        pos[3] = new Pos(0, 3, 0).get();
        pos[4] = new Pos(0, 2, 0).get();
        pos[5] = new Pos(0, 1, 0).get();
        pos[6] = new Pos(0, 0, 0).get();
        pos[7] = new Pos(0, -1, 0).get();
        for (int x = 0; x < 8; x++) assertEquals(pos[x], array[x]);
    }

    @Test
    public void contains() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = new Pos(1, 1, 1).get();
        assertFalse(graph.contains(pos));
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.contains(pos));
    }

    @Test
    public void linked() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos1 = new Pos(0, 0, 0).get();
        long pos2 = new Pos(0, 1, 0).get();
        graph.addNode(pos1, Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(pos2, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.linked(pos1, null, pos2));
    }

    @Test
    public void connects() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = new Pos(0, 0, 0).get();
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.connects(pos, null));
    }

    @Test
    public void visit() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(new Pos(5, 5, 5).get(), Connectivity.Cache.of(new GraphTest.TestNode()));
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            assertEquals(1, group.countBlocks());
        }
    }

    @Test
    public void countGroups() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(new Pos(0, 0, 0).get(), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(new Pos(1, 1, 1).get(), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(new Pos(2, 2, 2).get(), Connectivity.Cache.of(new GraphTest.TestNode()));
        assertEquals(3, graph.countGroups());
    }

    @Test
    public void addNode() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = new Pos(5, 5, 5).get();
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
        long pos = new Pos(2, 2, 2).get();
        graph.addConnector(pos, Connectivity.Cache.of(new GraphTest.TestCable()));
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids()) {
                for (long position : grid.getConnectors().keySet()) {
                    assertEquals(position, pos);
                }
            }
        }
    }

    @Test
    public void remove() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        long pos = new Pos(0, 0, 0).get();
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