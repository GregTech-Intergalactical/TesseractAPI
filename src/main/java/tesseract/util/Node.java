package tesseract.util;

/**
 * The Node is a pretty straightforward class resembling regular nodes.
 */
public class Node extends Pos {

    private Node parent;
    private int cost, heuristic, function;
    private boolean valid;
    private boolean crossroad;

    /**
     * Creates a node instance.
     *
     * @param value The compressed position.
     */
    public Node(long value) {
        super(value);
    }

    /**
     * Creates a node instance.
     *
     * @param value The compressed position.
     * @param valid The validity state.
     */
    public Node(long value, boolean valid) {
        super(value);
        setValid(valid);
    }

    /**
     * @return Gets the cost.
     */
    public int getCost() {
        return cost;
    }

    /**
     * Sets the cost.
     * @param cost The cost value.
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * @return Gets the heuristic.
     */
    public int getHeuristic() {
        return heuristic;
    }

    /**
     * Sets the heuristic.
     * @param heuristic The heuristic value.
     */
    public void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * @return Gets the function.
     */
    public int getFunction() {
        return function;
    }

    /**
     * Sets the function.
     * @param function The function value.
     */
    public void setFunction(int function) {
        this.function = function;
    }

    /**
     * @return Gets the parent node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent node.
     * @param parent The parent node.
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * @return Is the node valid?
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the valid state.
     * @param valid True or false.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return Is the node cross?
     */
    public boolean isCrossroad() {
        return crossroad;
    }

    /**
     * Sets the cross state.
     * @param crossroad True or false.
     */
    public void setCrossroad(boolean crossroad) {
        this.crossroad = crossroad;
    }

    /**
     * @return Calculates the heuristic distance.
     */
    public int heuristic(Node dest) {
        return distanceTo(dest);
    }

    /**
     * @param dest Calculates the heuristic to the node.
     * @return The distance value.
     */
    public int distanceTo(Node dest) {
        return Math.abs(getX() - dest.getX()) + Math.abs(getY() - dest.getY()) + Math.abs(getZ() - dest.getZ());
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")" + " [Cost: " + getCost() + " | Heuristic: " + getHeuristic() + " | Function: " + getFunction() + "]";
    }
}