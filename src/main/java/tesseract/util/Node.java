package tesseract.util;

import tesseract.graph.traverse.INodeContainer;

import java.util.HashSet;

public class Node extends Pos {

    private Node parent;
    private int cost, heuristic, function;

    public Node() {
        super();
    }

    public Node(int x, int y, int z) {
        super(x, y, z);
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