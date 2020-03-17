package tesseract.graph;

import org.junit.Test;
import tesseract.electric.api.*;
import tesseract.electric.base.*;
import tesseract.util.*;

import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void contains() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos = new Pos(1, 1, 1);
        assertFalse(graph.contains(pos));
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.contains(pos));
    }

    @Test
    public void linked() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos1 = new Pos(0, 0, 0);
        Pos pos2 = new Pos(0, 1, 0);
        graph.addNode(pos1, Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(pos2, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.linked(pos1, null, pos2));
    }

    @Test
    public void connects() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos = new Pos(0, 0, 0);
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        assertTrue(graph.connects(pos, null));
    }

    @Test
    public void visit() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(new Pos(5, 5, 5), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.visit((groupId, group) -> {
            assertEquals(1, group.countBlocks());
        });
    }

    @Test
    public void countGroups() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(new Pos(0, 0, 0), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(new Pos(1, 1, 1), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addNode(new Pos(2, 2, 2), Connectivity.Cache.of(new GraphTest.TestNode()));
        assertEquals(3, graph.countGroups());
    }

    @Test
    public void addNode() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos = new Pos(5, 5, 5);
        graph.addNode(pos, Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.visit((groupId, group) -> {
            group.visitNodes((position, node) -> {
                assertEquals(position, pos);
            });
        });
    }

    @Test
    public void addConnector() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos = new Pos(2, 2, 2);
        graph.addConnector(pos, Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.visit((groupId, group) -> {
            group.visitGrids(grid -> {
                grid.visitConnectors((position, connector) -> {
                    assertEquals(position, pos);
                });
            });
        });
    }

    @Test
    public void remove() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        Pos pos = new Pos(0, 0, 0);
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