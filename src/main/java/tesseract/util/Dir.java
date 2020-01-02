package tesseract.util;

public enum Dir {

    DOWN(0, 1, -1),
    UP(1, 0, 1),
    NORTH(2, 3, -1),
    SOUTH(3, 2, 1),
    WEST(4, 5, -1),
    EAST(5, 4, 1);

    public static final Dir[] VALUES;

    static {
        VALUES = values();
    }

    private int index, opposite, offset;

    Dir(int index, int opposite, int offset) {
        this.index = index;
        this.opposite = opposite;
        this.offset = offset;
    }

    public int getIndex() {
        return index;
    }

    public int getOffset() {
        return offset;
    }

    public Dir invert() {
        return VALUES[opposite];
    }
}
