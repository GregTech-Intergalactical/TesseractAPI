import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.junit.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.graph.Cache;
import tesseract.graph.Connectivity;
import tesseract.graph.Graph;
import tesseract.graph.INode;
import tesseract.graph.NodeCache;
import tesseract.graph.Graph.INodeGetter;
import tesseract.util.Pos;

public class GraphTest {

  private TestGraph defaultGraph() {
    TestGraph graph = new TestGraph(() -> new TestController(null, null));
    return graph;
  }

  private TestGraph.TestConnector defaultConnector() {
    return new TestGraph.TestConnector();
  }

  @Test
  public void addConnector() {
    var graph = defaultGraph();
    graph.addConnector(0, Cache.of(defaultConnector()));
    assertEquals(graph.size(), 1);
    assertTrue(graph.contains(0));
  }

  @Test
  public void addNodes() {
    var graph = defaultGraph();
    long posOne = Pos.packAll(1, 0, 0);
    long posTwo = Pos.packAll(0, 1, 0);
    graph.addConnector(0, Cache.of(defaultConnector()));
    graph.addNode(NodeCache.of(posOne, new TestGraph.TestNode()));
    assertTrue(graph.contains(posOne));
    assertEquals(graph.size(), 2);
    graph.addNode(NodeCache.of(posTwo, new TestGraph.TestNode()));
    assertTrue(graph.contains(posTwo));
    assertEquals(graph.size(), 3);
    assertEquals(graph.getGroupAt(0).getGrids().size(), 1);
  }

  @Test
  public void twoGroupsAddNearby() {
    var graph = defaultGraph();
    long posOne = Pos.packAll(1, 0, 0);
    long posTwo = Pos.packAll(0, 1, 0);
    graph.addConnector(posOne, Cache.of(defaultConnector()));
    graph.addConnector(posTwo, Cache.of(defaultConnector()));
    assertEquals(graph.countGroups(), 2);
    assertEquals(graph.size(), 2);
    graph.addNode(NodeCache.of(0, new TestGraph.TestNode()));
    assertEquals(graph.countGroups(), 1);
    assertEquals(graph.size(), 3);
  }

  @Test
  public void splitGrids() {
    var graph = defaultGraph();
    long posOne = Pos.packAll(1, 0, 0);
    long posTwo = Pos.packAll(2, 0, 0);
    long posThree = Pos.packAll(3, 0, 0);
    graph.addConnector(posOne, Cache.of(defaultConnector()));
    graph.addConnector(posTwo, Cache.of(defaultConnector()));
    graph.addNode(NodeCache.of(0, new TestGraph.TestNode()));
    graph.addNode(NodeCache.of(posThree, new TestGraph.TestNode()));

    assertEquals(graph.size(), 4);
    assertEquals(graph.getGroupAt(0).getGrids().size(), 1);

    graph.removeAt(posOne);
    var conn = defaultConnector();
    conn.clearSide(Direction.EAST);
    graph.addConnector(posOne, Cache.of(conn));

    assertEquals(graph.size(), 4);
    assertEquals(graph.getGroupAt(0).getGrids().size(), 2);
  }

  public class TestGraph extends Graph<Integer, TestGraph.TestConnector, TestGraph.TestNode> {

    public TestGraph(Supplier<Controller<Integer, TestConnector, TestNode>> controller) {
      super(controller);
    }

    public static class TestNode {

    }

    public static class TestConnector implements IConnectable {


      byte connections = Byte.MAX_VALUE;

      public void setSide(Direction dir) {
      }

      public void clearSide(Direction dir) {
        connections = Connectivity.clear(connections, dir.get3DDataValue());
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
  }

  public class TestController extends Controller<Integer, TestGraph.TestConnector, TestGraph.TestNode> {

    protected TestController(Level supplier, INodeGetter<TestGraph.TestNode> getter) {
      super(supplier, getter);
    }

    @Override
    public void change() {

    }

    @Override
    public ITickingController<Integer, TestGraph.TestConnector, TestGraph.TestNode> clone(INode group) {
      return new TestController(this.dim, this.getter);
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {

    }

    @Override
    public void insert(long producerPos, Direction side, Integer transaction, ITransactionModifier modifier) {

    }

    @Override
    protected void onFrame() {

    }

  }
  /*
   * @Test
   * public void system() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * graph.addConnector(packAll(1, 0, 0), new Cache<>(new TestConnector(packAll(1,
   * 0, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 1, 0), new Cache<>(new TestConnector(packAll(0,
   * 1, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 2, 0), new Cache<>(new TestConnector(packAll(0,
   * 2, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 3, 0), new Cache<>(new TestConnector(packAll(0,
   * 3, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector(packAll(0,
   * 4, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 5, 0), new Cache<>(new TestConnector(packAll(0,
   * 5, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 6, 0), new Cache<>(new TestConnector(packAll(0,
   * 6, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 0, 1), new Cache<>(new TestConnector(packAll(0,
   * 0, 1), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 0, -1), new Cache<>(new
   * TestConnector(packAll(0, 0, -1), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, -1, 0), new Cache<>(new
   * TestConnector(packAll(0, -1, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(-1, 0, 0), new Cache<>(new
   * TestConnector(packAll(-1, 0, 0), map)), () -> null, tester, true);
   * assertEquals(6, graph.countGroups());
   * graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector(packAll(0,
   * 0, 0), map)), () -> null, tester, true);
   * assertEquals(1, graph.countGroups());
   * //graph.removeAt(packAll(0, 0, 0));
   * assertEquals(6, graph.countGroups());
   * //graph.removeAt(packAll(0, 4, 0));
   * assertEquals(7, graph.countGroups());
   * graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector(packAll(0,
   * 0, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(0, 4, 0), new Cache<>(new TestConnector(packAll(0,
   * 4, 0), map)), () -> null, tester, true);
   * 
   * assertEquals(1, graph.countGroups());
   * Deque<Node> set1 = new ArrayDeque<>();
   * for (Group<Object, TestConnector, TestNode> group :
   * graph.getGroups().values()) {
   * for (Grid<TestConnector> grid : group.getGrids().values()) {
   * set1 = grid.getPath(packAll(0, -1, 0), packAll(0, 6, 0));
   * }
   * }
   * List<Pos> set2 = new ObjectArrayList<>();
   * set2.add(new Pos(0, -1, 0));
   * set2.add(new Pos(0, 0, 0));
   * set2.add(new Pos(0, 1, 0));
   * set2.add(new Pos(0, 2, 0));
   * set2.add(new Pos(0, 3, 0));
   * set2.add(new Pos(0, 4, 0));
   * set2.add(new Pos(0, 5, 0));
   * set2.add(new Pos(0, 6, 0));
   * Iterator<Node> it = set1.descendingIterator();
   * for (Pos pos : set2) {
   * assertEquals(pos, it.next());
   * }
   * }
   * 
   * @Test
   * public void contains() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * long pos = packAll(1, 1, 1);
   * long posC = packAll(0, 1, 1);
   * assertFalse(graph.contains(pos));
   * assertFalse(graph.contains(posC));
   * graph.addConnector(pos, new Cache<>(new TestConnector(pos, map)), () -> null,
   * tester, true);
   * graph.addConnector(posC, new Cache<>(new TestConnector(posC, map)), () ->
   * null, tester, true);
   * 
   * assertTrue(graph.contains(posC));
   * //graph.addNode(pos, (a,p) -> new TestNode(), Pos.subToDir(posC, pos), () ->
   * null, true);
   * assertTrue(graph.contains(pos));
   * }
   * 
   * @Test
   * public void linked() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * long pos = packAll(0,0,0);
   * long posC = packAll(0, 1, 0);
   * map.put(posC, new TestNode());
   * 
   * graph.addConnector(pos, new Cache<>(new TestConnector(pos, map)), () -> null,
   * tester, true);
   * 
   * assertTrue(graph.linked(pos, null, posC));
   * }
   * 
   * @Test
   * public void connects() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * long pos = packAll(0,0,0);
   * long posC = packAll(0, 1, 0);
   * map.put(posC, new TestNode());
   * // graph.addNode(pos, new Cache<>(new TestNode()), null);
   * graph.addConnector(pos, new Cache<>(new TestConnector(pos, map)), () -> null,
   * tester, true);
   * assertTrue(graph.connects(pos, null));
   * }
   * 
   * @Test
   * public void visit() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * graph.addConnector(packAll(0,0,0), new Cache<>(new
   * TestConnector(packAll(0,0,0), map)), () -> null, tester, true);
   * for (Group<Object, TestConnector, TestNode> group :
   * graph.getGroups().values()) {
   * assertEquals(1, group.countBlocks());
   * }
   * }
   * 
   * @Test
   * public void countGroups() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector(packAll(0,
   * 0, 0), map)), () -> null, tester, true);
   * graph.addConnector(packAll(1, 1, 1), new Cache<>(new TestConnector(packAll(1,
   * 1, 1), map)), () -> null, tester, true);
   * graph.addConnector(packAll(2, 2, 2), new Cache<>(new TestConnector(packAll(2,
   * 2, 2), map)), () -> null, tester, true);
   * 
   * assertEquals(3, graph.countGroups());
   * }
   * 
   * // @Test
   * // public void addConnector() {
   * // Graph<TestConnector, TestNode> graph = new Graph<>();
   * // long pos = packAll(2, 2, 2);
   * // graph.addConnector(pos, new Cache<>(new TestConnector()), null);
   * // for (Group<TestConnector, TestNode> group : graph.getGroups().values()) {
   * // for (Grid<TestConnector> grid : group.getGrids().values()) {
   * // for (long position : grid.getConnectors().keySet()) {
   * // assertEquals(position, pos);
   * // }
   * // }
   * // }
   * // }
   * 
   * @Test
   * public void remove() {
   * Long2ObjectMap<TestNode> map = new Long2ObjectOpenHashMap<>();
   * INodeGetter<TestNode> tester = getter(map);
   * Graph<Object, TestConnector, TestNode> graph = new Graph<>();
   * long pos = packAll(0, 0, 0);
   * graph.addConnector(packAll(0, 0, 0), new Cache<>(new TestConnector(packAll(0,
   * 0, 0), map)), () -> null, tester, true);
   * 
   * assertEquals(1, graph.countGroups());
   * //graph.removeAt(pos);
   * assertEquals(0, graph.countGroups());
   * }
   * 
   * protected static INodeGetter<TestNode> getter(Long2ObjectMap<TestNode> map) {
   * return (a,b,c) -> {
   * return map.get(a);
   * };
   * }
   * 
   * public static class TestConnector implements IConnectable {
   * 
   * private final Long2ObjectMap<TestNode> nodes;
   * private final long pos;
   * 
   * public TestConnector(long pos, Long2ObjectMap<TestNode> nodes) {
   * this.nodes = nodes;
   * this.pos = pos;
   * }
   * 
   * @Override
   * public String toString() {
   * return "TestCable";
   * }
   * 
   * @Override
   * public boolean connects(Direction direction) {
   * return true;
   * }
   * 
   * @Override
   * public boolean validate(Direction dir) {
   * return nodes.containsKey(Pos.offset(this.pos, dir));
   * }
   * }
   * 
   * private static class TestNode {
   * 
   * @Override
   * public String toString() {
   * return "TestNode";
   * }
   * }
   */
}
