package tesseract.util;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * Direction enum
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

    public static final Map<Pos,Dir> POS_TO_DIR;

    static {
        POS_TO_DIR = new Object2ObjectOpenHashMap<>();
        POS_TO_DIR.put(new Pos(0,-1,0), DOWN);
        POS_TO_DIR.put(new Pos(0,1,0), UP);
        POS_TO_DIR.put(new Pos(0,0,-1), NORTH);
        POS_TO_DIR.put(new Pos(0,0,1), SOUTH);
        POS_TO_DIR.put(new Pos(-1,0,0), WEST);
        POS_TO_DIR.put(new Pos(1,0,0), EAST);
    }

    static {
        VALUES = values();
    }

    /**
     * Create a direction instance.
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
    public Dir getOpposite() {
        return VALUES[opposite];
    }
}
