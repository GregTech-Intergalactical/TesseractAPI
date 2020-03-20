package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.ConcurrentModificationException;

/**
 * A Star Algorithm implementation for converting a graph – consisting of the grid – into a route through the grid.
 */
public class ASFinder {

    private ArrayDeque<Node> open;
    private ObjectOpenHashSet<Node> closed;
    private LongLinkedOpenHashSet path;
    private INode container;

    /**
     * Creates a reusable AStarPathfinder instance that will search the provided container.
     *
     * @param container The container to use for search operations
     */
    public ASFinder(INode container) {
        open = new ArrayDeque<>();
        closed = new ObjectOpenHashSet<>();
        path = new LongLinkedOpenHashSet();
        this.container = container;
    }

    /**
     * Begins a find operation from the specified start position to the end position.
     *
     * @param origin    The start position of the finds operation.
     * @param target    The end position of the finds operation.
     * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
     * @return          An set of the points calculated by the A Star algorithm.
     */
    public LongLinkedOpenHashSet find(long origin, long target, boolean crossroad) {
        if (!closed.isEmpty() || !open.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same ASFinder instance");
        }

        if (origin == target) {
            throw new ConcurrentModificationException("Attempted to run find operation with invalid positions");
        }

        path.clear();

        try {
            Node start = new Node(origin);
            Node end = new Node(target);

            open.add(start);

            while (!open.isEmpty()) {
                Node current = getLowestF();

                if (current.equals(end)) {
                    retracePath(current, crossroad);
                }

                open.remove(current);
                closed.add(current);

                for (Node n : getNeighborsNodes(current)) {

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

        return path;
    }

    /**
     * Adds all nodes to the path set.
     *
     * @param current   The current node
     * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
     */
    public void retracePath(Node current, boolean crossroad) {
        Node temp = current;
        path.add(current.get());

        while (temp.getParent() != null) {
            Node parent = temp.getParent();
            if (crossroad) {
                if (isCrossroad(parent)) {
                    path.add(parent.get());
                }
            } else {
                path.add(parent.get());
            }
            temp = parent;
        }

        path.add(temp.get());
    }

    /**
     * Returns a node with the lowest function which exists the open list.
     *
     * @return The found node.
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
     * Lookups for a set of neighbors of a given node.
     *
     * @param current The given node.
     * @return The set of nodes.
     */
    public ObjectLinkedOpenHashSet<Node> getNeighborsNodes(Node current) {
        ObjectLinkedOpenHashSet<Node> neighbors = new ObjectLinkedOpenHashSet<>(6);

        for (Dir direction : Dir.VALUES) {
            long pos = current.offset(direction).get();

            if (container.contains(pos)) {
                neighbors.add(new Node(pos));
            }
        }

        return neighbors;
    }

    /**
     * Validates the crossroads state of the node.
     *
     * @param current The given node.
     * @return True if node has more then 2 connections, false otherwise.
     */
    public boolean isCrossroad(Node current) {
        int connections = 0;

        for (Dir direction : Dir.VALUES) {
            long pos = current.offset(direction).get();

            if (container.connects(pos, direction)) {
                connections++;
            }
        }

        return connections > 2;
    }

    /**
     * The Node is a pretty straightforward class resembling regular nodes.
     */
    private static class Node extends Pos {

        private Node parent;
        private int cost, heuristic, function;

        public Node() {
            super();
        }

        public Node(int x, int y, int z) {
            super(x, y, z);
        }

        public Node(long value) {
            super(value);
        }

        public Node(Pos pos) {
            super(pos);
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public int getHeuristic() {
            return heuristic;
        }

        public void setHeuristic(int heuristic) {
            this.heuristic = heuristic;
        }

        public int getFunction() {
            return function;
        }

        public void setFunction(int function) {
            this.function = function;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public int heuristic(Node dest) {
            return distanceTo(dest);
        }

        public int distanceTo(Node dest) {
            return Math.abs(getX() - dest.getX()) + Math.abs(getY() - dest.getY()) + Math.abs(getZ() - dest.getZ());
        }

        @Override
        public String toString() {
            return "(" + getX() + ", " + getY() + ", " + getZ() + ")" + " [Cost: " + getCost() + " | Heuristic: " + getHeuristic() + " | Function: " + getFunction() + "]";
        }
    }
}