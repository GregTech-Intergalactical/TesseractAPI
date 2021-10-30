package tesseract.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static tesseract.util.Pos.packAll;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import tesseract.api.IConnectable;
import tesseract.util.Node;
import tesseract.util.Pos;

public class GraphTest {
  // @Test
  // public void system() {
  //   Graph<Object, TestConnector, TestNode> graph = new Graph<>();
  //   graph.addConnector(packAll(1, 0, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 1, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 2, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 3, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 5, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 6, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 0, 1), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 0, -1), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, -1, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(-1, 0, 0), new Cache<>(new TestConnector()), null);
  //   assertEquals(6, graph.countGroups());
  //   graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector()), null);
  //   assertEquals(1, graph.countGroups());
  //   graph.removeAt(packAll(0, 0, 0));
  //   assertEquals(6, graph.countGroups());
  //   graph.removeAt(packAll(0, 4, 0));
  //   assertEquals(7, graph.countGroups());
  //   graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector()), null);
  //   graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector()), null);
  //   assertEquals(1, graph.countGroups());
  //   Deque<Node> set1 = new ArrayDeque<>();
  //   for (Group<Object, TestConnector, TestNode> group : graph.getGroups().values()) {
  //     for (Grid<TestConnector> grid : group.getGrids().values()) {
  //       set1 = grid.getPath(packAll(0, -1, 0), packAll(0, 6, 0));
  //     }
  //   }
  //   List<Pos> set2 = new ObjectArrayList<>();
  //   set2.add(new Pos(0, -1, 0));
  //   set2.add(new Pos(0, 0, 0));
  //   set2.add(new Pos(0, 1, 0));
  //   set2.add(new Pos(0, 2, 0));
  //   set2.add(new Pos(0, 3, 0));
  //   set2.add(new Pos(0, 4, 0));
  //   set2.add(new Pos(0, 5, 0));
  //   set2.add(new Pos(0, 6, 0));
  //   Iterator<Node> it = set1.descendingIterator();
  //   for (Pos pos : set2) {
  //     assertEquals(pos, it.next());
  //   }
  // }

  @Test
  public void contains() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    long pos = packAll(1, 1, 1);
    long posC = packAll(0, 1, 1);
    assertFalse(graph.contains(pos));
    assertFalse(graph.contains(posC));
    graph.addConnector(posC, new Cache<>(new TestConnector()), null);
    assertTrue(graph.contains(posC));
    graph.addNode(pos, (a,p) -> new TestNode(), Pos.subToDir(posC, pos), () -> null, true);
    assertTrue(graph.contains(pos));
  }

  @Test
  public void linked() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    long pos1 = packAll(0, 0, 0);
    long pos2 = packAll(0, 1, 0);
    graph.addConnector(pos1, new Cache<>(new TestConnector()), null);
    graph.addNode(pos2, (a,p)-> new TestNode(), Pos.subToDir(pos1, pos2), () -> null, true);
    assertTrue(graph.linked(pos1, null, pos2));
  }

  @Test
  public void connects() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    long pos = packAll(0, 0, 0);
    // graph.addNode(pos, new Cache<>(new TestNode()), null);
    graph.addConnector(pos, new Cache<>(new TestConnector()), null);
    assertTrue(graph.connects(pos, null));
  }

  @Test
  public void visit() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    graph.addConnector(packAll(5, 5, 5), new Cache<>(new TestConnector()), null);
    for (Group<Object, TestConnector, TestNode> group : graph.getGroups().values()) {
      assertEquals(1, group.countBlocks());
    }
  }

  @Test
  public void countGroups() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector()), null);
    graph.addConnector(packAll(1, 1, 1), new Cache<>(new TestConnector()), null);
    graph.addConnector(packAll(2, 2, 2), new Cache<>(new TestConnector()), null);
    assertEquals(3, graph.countGroups());
  }

  // @Test
  // public void addNode() {
  // Graph<TestConnector, TestNode> graph = new Graph<>();
  // long pos = packAll(5, 5, 5);
  // graph.addNode(pos, new Cache<>(new TestNode()), null);
  // for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
  // for (long position : group.getNodes().keySet()) {
  // assertEquals(position, pos);
  // }
  // }
  // }

  // @Test
  // public void addConnector() {
  // Graph<TestConnector, TestNode> graph = new Graph<>();
  // long pos = packAll(2, 2, 2);
  // graph.addConnector(pos, new Cache<>(new TestConnector()), null);
  // for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
  // for (Grid<TestConnector> grid : group.getGrids().values()) {
  // for (long position : grid.getConnectors().keySet()) {
  // assertEquals(position, pos);
  // }
  // }
  // }
  // }

  @Test
  public void remove() {
    Graph<Object, TestConnector, TestNode> graph = new Graph<>();
    long pos = packAll(0, 0, 0);
    graph.addConnector(pos, new Cache<>(new TestConnector()), null);
    assertEquals(1, graph.countGroups());
    graph.removeAt(pos);
    assertEquals(0, graph.countGroups());
  }

  public static class TestConnector implements IConnectable {

    @Override
    public String toString() {
      return "TestCable";
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

  private static class TestNode {

    @Override
    public String toString() {
      return "TestNode";
    }
  }
}
