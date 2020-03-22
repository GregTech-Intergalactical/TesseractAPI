package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;
import tesseract.electric.api.*;
import tesseract.electric.base.*;
import tesseract.util.Dir;
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
        ArrayDeque<Pos> set1 = new ArrayDeque<>();
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids().values()) {
                set1 = grid.findPath(packAll(0, -1, 0), packAll(0, 6, 0), false);
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
        Iterator<Pos> iterator = set1.descendingIterator();
        for (Pos pos : set2) {
            assertEquals(pos, iterator.next());
        }
    }

    @Test
    public void flat() {
        Graph<GraphTest.TestCable, GraphTest.TestNode> graph = new Graph<>();
        graph.addNode(packAll(1,0,0), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addConnector(packAll(2,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(3,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(4,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(5,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(6,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(7,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(8,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(9,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(10,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(11,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(12,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(13,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(14,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(15,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(16,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(17,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(18,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(19,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(20,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(21,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(22,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(23,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(24,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(25,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(26,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(27,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(28,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(29,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(30,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(31,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(32,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(33,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(34,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(35,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(36,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(37,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(38,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(39,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,1,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,2,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,3,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,3,1), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(40,3,2), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addNode(packAll(40,3,3), Connectivity.Cache.of(new GraphTest.TestNode()));
        graph.addConnector(packAll(41,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(42,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(43,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(44,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(45,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(46,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(47,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(48,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(49,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(50,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(51,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(52,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(53,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(54,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(55,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(56,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(57,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(58,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(59,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(60,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(61,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(62,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(63,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(64,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(65,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(66,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(67,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(68,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(69,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(70,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(71,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(72,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(73,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(74,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(75,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(76,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(77,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(78,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(79,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(80,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(81,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(82,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(83,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(84,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(85,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(86,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(87,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(88,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addConnector(packAll(89,0,0), Connectivity.Cache.of(new GraphTest.TestCable()));
        graph.addNode(packAll(90,0,0), Connectivity.Cache.of(new GraphTest.TestNode()));
        ArrayDeque<Pos> set1 = new ArrayDeque<>();
        for (IGroup<TestCable, TestNode> group : graph.getGroups().values()) {
            for (IGrid<GraphTest.TestCable> grid : group.getGrids().values()) {
                set1 = grid.findPath(packAll(1, 0, 0), packAll(90, 0, 0), true);
            }
        }
        List<Pos> set2 = new ArrayList<>();
        set2.add(new Pos(1, 0, 0));
        set2.add(new Pos(40, 0, 0));
        set2.add(new Pos(90, 0, 0));
        Iterator<Pos> iterator = set1.descendingIterator();
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