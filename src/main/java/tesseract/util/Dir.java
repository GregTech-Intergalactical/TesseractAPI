package tesseract.util;

/**
 * @see net.minecraft.util.Direction
 */
public enum Dir {
    DOWN(0, 1, new Pos(0, -1, 0)),
    UP(1, 0, new Pos(0, 1, 0)),
    NORTH(2, 3, new Pos(0, 0, -1)),
    SOUTH(3, 2, new Pos(0, 0, 1)),
    WEST(4, 5, new Pos(-1, 0, 0)),
    EAST(5, 4, new Pos(1, 0, 0));

    public static final Dir[] VALUES;
    private final int index, opposite;
    private final Pos direction;

    static {
        VALUES = values();
    }

    /**
     * Create a direction instance.
     *
     * @param index The index of the direction.
     * @param opposite The opposite index of the direction.
     * @param direction The direction vector.
     */
    Dir(int index, int opposite, Pos direction) {
        this.index = index;
        this.opposite = opposite;
        this.direction = direction;
    }

    /**
     * @return Gets the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Gets the X direction offset.
     */
    public int getXOffset() {
        return direction.getX();
    }

    /**
     * @return Gets the Y direction offset.
     */
    public int getYOffset() {
        return direction.getY();
    }

    /**
     * @return Gets the Z direction offset.
     */
    public int getZOffset() {
        return direction.getZ();
    }

    /**
     * @return Return the inverted direction.
     */
    public Dir invert() {
        return VALUES[opposite];
    }
}
