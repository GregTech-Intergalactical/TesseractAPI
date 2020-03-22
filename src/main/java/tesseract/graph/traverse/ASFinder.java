package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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
    private ArrayDeque<Pos> path;
    private ObjectOpenHashSet<Node> closed;
    private INode container;

    /**
     * Creates a reusable AStarPathfinder instance that will search the provided container.
     *
     * @param container The container to use for search operations
     */
    public ASFinder(INode container) {
        open = new ArrayDeque<>();
        closed = new ObjectOpenHashSet<>();
        this.container = container;
    }

    /**
     * Begins a find operation from the specified start position to the end position.
     *
     * @param origin The start position of the finds operation.
     * @param target The end position of the finds operation.
     * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
     * @return An set of the points calculated by the A Star algorithm.
     */
    public ArrayDeque<Pos> find(long origin, long target, boolean crossroad) {
        if (!closed.isEmpty() || !open.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same ASFinder instance");
        }

        if (origin == target) {
            throw new IllegalStateException("ASFinder::find: Attempted to run find operation with invalid positions");
        }

        path = new ArrayDeque<>();

        try {
            Node start = new Node(origin, false);
            Node end = new Node(target, false);

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
     * @param current The current node
     * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
     */
    public void retracePath(Node current, boolean crossroad) {
        Node temp = current;
        path.add(current);

        if (crossroad) {
            while (temp.getParent() != null) {
                Node parent = temp.getParent();
                if (parent.isValid()) {
                    if (isCrossroad(parent)) {
                        path.add(parent);
                    }
                } else {
                    path.add(parent);
                }
                temp = parent;
            }
        } else {
            while (temp.getParent() != null) {
                Node parent = temp.getParent();
                path.add(parent);
                temp = parent;
            }
        }
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
    public ObjectSet<Node> getNeighborsNodes(Node current) {
        ObjectSet<Node> neighbors = new ObjectLinkedOpenHashSet<>(6);

        for (Dir direction : Dir.VALUES) {
            long pos = current.offset(direction).get();

            if (container.contains(pos)) {
                neighbors.add(new Node(pos, true));
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
        byte connections = 0;

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
        private boolean valid;

        public Node(long value, boolean valid) {
            super(value);
            setValid(valid);
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

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
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