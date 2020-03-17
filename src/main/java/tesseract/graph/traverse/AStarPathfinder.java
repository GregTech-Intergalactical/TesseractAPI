package tesseract.graph.traverse;

import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.ConcurrentModificationException;

/**
 * A Star Algorithm implementation which finds an allowed path from start to goal coordinates on grid.
 * <p>
 * This method uses the A Star algorithm. The costs value is calculated in
 * the given Node implementation.
 * <p>
 * If no allowed path exists, nothing will be returned to the constumer.
 */
public class AStarPathfinder {

    private HashSet<Node> closed;
    private ArrayDeque<Node> open;
    private INodeContainer container;

    /**
     * Creates a reusable AStarPathfinder instance that will search the provided container.
     *
     * @param container The container to use for search operations
     */
    public AStarPathfinder(INodeContainer container) {
        closed = new HashSet<>();
        open = new ArrayDeque<>();
        this.container = container;
    }

    /**
     * Begins a find operation from the specified start position. The finder will report each new reached position of the path
     * to the provided consumer. As a result of the algorithm, each reported position is guaranteed to be connected to
     * an existing position, or in the case of the first reported position, it will be identical to from.
     *
     * @param origin     The start position of the finds operation. This will be the first position reported to the consumer.
     * @param target     The target position of the finds operation. This will be the last position reported to the consumer.
     * @param collector  An acceptor of the points calculated by the A Star algorithm.
     */
    public void find(Pos origin, Pos target,  Consumer<Pos> collector) {
        if (!closed.isEmpty() || !open.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same AStarPathfinder instance");
        }

        if (target == null || origin == null || origin.equals(target)) {
            throw new ConcurrentModificationException("Attempted to run find operation with invalid position");
        }

        try {
            Node start = new Node(origin);
            Node end = new Node(target);

            open.add(start);

            while (!open.isEmpty()) {

                Node current = getLowestF();
                if (current.equals(end)) {
                    //
                    Node temp = current;
                    collector.accept(current);

                    while (temp.getParent() != null) {
                        collector.accept(temp.getParent());
                        temp = temp.getParent();
                    }

                    break;
                }

                open.remove(current);
                closed.add(current);

                for (Node n : getNeighboringNodes(current)) {

                    if (closed.contains(n)) {
                        continue;
                    }

                    int tempScore = current.getCost() + current.distanceTo(n);

                    if (open.contains(n)) {
                        if (tempScore < n.getCost()) {
                            n.setCost(tempScore);
                            n.setParent(current);
                        }
                    } else {
                        n.setCost(tempScore);
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
    }

    /**
     * Returns a node with the lowest function which exists the open list.
     *
     * @return  The found node.
     */
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
     * Returns a set of a neighbors of a given node.
     *
     * @return  The set of nodes.
     */
    public HashSet<Node> getNeighboringNodes(Node current) {
        HashSet<Node> neighbors = new HashSet<>(6);

        for (Dir direction : Dir.VALUES) {
            Pos pos = current.offset(direction);

            if (container.contains(pos)) {
                neighbors.add(new Node(pos));
            }
        }

        return neighbors;
    }
}