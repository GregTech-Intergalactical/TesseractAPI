package tesseract.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Node is a pretty straightforward class resembling regular nodes.
 */
public class Node extends Pos {

    private Node parent;
    private Dir direction;
    private int cost, heuristic, function;
    private boolean valid;
    private boolean crossroad;

    /**
     * Creates a node instance.
     *
     * @param value The compressed position.
     * @param direction The direction to the parent.
     */
    public Node(long value, @Nonnull Dir direction) {
        super(value);
        setDirection(direction);
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
    @Nullable
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent node.
     * @param parent The parent node.
     */
    public void setParent(@Nonnull Node parent) {
        this.parent = parent;
    }

    /**
     * @return Gets the direction to the parent node.
     */
    @Nonnull
    public Dir getDirection() {
        return direction;
    }

    /**
     * Sets the direction to the parent node.
     * @param direction The direction.
     */
    public void setDirection(@Nonnull Dir direction) {
        this.direction = direction;
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
    public int heuristic(@Nonnull Node dest) {
        return distanceTo(dest);
    }

    /**
     * @param dest Calculates the heuristic to the node.
     * @return The distance value.
     */
    public int distanceTo(@Nonnull Node dest) {
        return Math.abs(x - dest.x) + Math.abs(y - dest.y) + Math.abs(z - dest.z);
    }

    @Override
    public String toString() {
        return super.toString() + " [Cost: " + cost + " | Heuristic: " + heuristic + " | Function: " + function + "]";
    }
}