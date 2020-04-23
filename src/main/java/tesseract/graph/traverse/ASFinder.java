package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.objects.*;
import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Node;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ConcurrentModificationException;

/**
 * A Star Algorithm implementation for converting a graph – consisting of the grid – into a route through the grid.
 */
public class ASFinder {

    private ArrayDeque<Node> path;
    private final INode container;
    private final ArrayDeque<Node> open = new ArrayDeque<>();
    private final ObjectOpenHashSet<Node> closed = new ObjectOpenHashSet<>();

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
    public ArrayDeque<Node> traverse(long origin, long target) {
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
                        open.add(n);
                        n.setParent(current);
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
        return lowest;
    }

    /**
     * Lookups for a set of neighbors of a given node.
     *
     * @param current The given node.
     * @return The set of nodes.
     */
    @Nonnull
    public ObjectList<Node> getNeighboringNodes(@Nonnull Node current) {
        ObjectList<Node> neighbors = new ObjectArrayList<>(6);

        for (Dir direction : Dir.VALUES) {
            long pos = current.offset(direction).asLong();

            if (container.contains(pos)) {
                neighbors.add(new Node(pos, direction.invert()));
            }
        }

        return neighbors;
    }
}