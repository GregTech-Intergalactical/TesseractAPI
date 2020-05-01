package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.objects.*;
import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A Star Algorithm implementation for converting a graph – consisting of the grid – into a route through the grid.
 */
public class ASFinder {

    private Deque<Node> path;
    private final INode container;
    private final Deque<Node> open = new ArrayDeque<>();
    private final Set<Node> closed = new ObjectOpenHashSet<>();

    /**
     * Creates a reusable AStarPathfinder instance that will search the provided container.
     *
     * @param container The container to use for find operations.
     */
    public ASFinder(@Nonnull INode container) {
        this.container = container;
    }

    /**
     * Begins a traverse operation from the specified start position to the end position.
     *
     * @param origin The start position of the traverse operation.
     * @param target The end position of the traverse operation.
     * @return An set of the points calculated by the A Star algorithm.
     */
    @Nonnull
    public Deque<Node> traverse(long origin, long target) {
        if (!closed.isEmpty() || !open.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same ASFinder instance");
        }

        if (origin == target) {
            throw new IllegalStateException("ASFinder::traverse: Attempted to run traverse operation with invalid positions");
        }

        path = new ArrayDeque<>();

        try {
            Node start = new Node(origin, true);
            Node end = new Node(target,false);

            open.add(start);

            while (!open.isEmpty()) {
                Node current = getLowestF();

                if (current.equals(end)) {
                    retracePath(current);
                    break;
                }

                open.remove(current);
                closed.add(current);

                for (Node n : getNeighboringNodes(current)) {

                    if (n == null) {
                        break;
                    }

                    if (closed.contains(n)) {
                        continue;
                    }

                    int score = current.getCost() + current.distanceTo(n);

                    if (open.contains(n)) {
                        if (score < n.getCost()) {
                            n.setCost(score);
                            n.setParent(current);
                        }
                    } else {
                        n.setCost(score);
                        n.setParent(current);
                        open.add(n);
                    }

                    n.setHeuristic(n.heuristic(end));
                    n.setFunction(n.getCost() + n.getHeuristic());
                }
            }
        } finally {
            // Clean up the open/closed sets
            closed.clear();
            open.clear();
        }

        return path;
    }

    /**
     * Adds all nodes to the path set.
     *
     * @param current The current node.
     */
    public void retracePath(@Nonnull Node current) {
        Node temp = current;
        temp.setCrossroad(true); // Consider tail as a part of the crossroad
        path.add(temp);

        while ((temp = current.getParent()) != null) {
            temp.setCrossroad(temp.isValid() || retraceNode(temp));
            path.add(temp);
            current = temp;
        }
    }

    /**
     * Validates the crossroads state of the node.
     *
     * @param current The current node.
     * @return True or false.
     */
    public boolean retraceNode(@Nonnull Node current) {
        int connections = 0;

        for (Dir direction : Dir.VALUES) {
            long pos = current.offset(direction).asLong();

            if (container.connects(pos, direction)) {
                connections++;
            }
        }

        return connections > 2;
    }

    /**
     * Returns a node with the lowest function which exists the open list.
     *
     * @return The found node.
     */
    @Nonnull
    private Node getLowestF() {
        Node lowest = open.peek();
        for (Node n : open) {
            if (n.getFunction() < lowest.getFunction()) {
                lowest = n;
            }
        }
        assert lowest != null;
        return lowest;
    }

    /**
     * Lookups for a set of neighbors of a given node.
     *
     * @param current The given node.
     * @return The list of nodes.
     */
    @Nonnull
    public Node[] getNeighboringNodes(@Nonnull Node current) {
        Node[] neighbors = new Node[6]; int i = 0;

        for (Dir direction : Dir.VALUES) {
            Pos pos = current.offset(direction);
            long side = pos.asLong();

            if (container.contains(side)) {
                neighbors[i++] = new Node(pos, direction.invert());
            }
        }

        return neighbors;
    }
}