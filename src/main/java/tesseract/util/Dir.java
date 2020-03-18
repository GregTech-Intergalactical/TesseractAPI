package tesseract.util;

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

    Dir(int index, int opposite, Pos direction) {
        this.index = index;
        this.opposite = opposite;
        this.direction = direction;
    }

    public int getIndex() {
        return index;
    }

    public int getXOffset() {
        return direction.getX();
    }

    public int getYOffset() {
        return direction.getY();
    }

    public int getZOffset() {
        return direction.getZ();
    }

    public Dir invert() {
        return VALUES[opposite];
    }
}
