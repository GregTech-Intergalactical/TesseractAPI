package tesseract.util;

import javax.annotation.Nonnull;

/**
 * @see net.minecraft.util.math.BlockPos
 */
public class Pos {

    protected long value;
    protected int x, y, z;

    /**
     * Though it looks like an array, this is really more like a mapping. Key (index of this array) is the upper 5 bits
     * of the result of multiplying a 32-bit unsigned integer by the B(2, 5) De Bruijn sequence 0x077CB531. Value (value
     * stored in the array) is the unique index (from the right) of the leftmo
     */
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};

    /**
     * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
     */
    private static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    /**
     * Returns the input value rounded up to the next highest power of two.
     */
    private static int smallestEncompassingPowerOfTwo(int value) {
        int i = value - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }

    /**
     * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given value.
     * Optimized for cases where the input value is a power-of-two. If the input value is not a power-of-two, then
     * subtract 1 from the return value.
     */
    private static int log2DeBruijn(int value) {
        value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)value * 125613361L >> 27) & 31];
    }

    /**
     * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
     * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
     */
    private static int log2(int value) {
        return log2DeBruijn(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    /**
     * Bits needed to convert vector into long value.
     */
    private static final int NUM_X_BITS = 1 + log2(smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final int Z_FIELD = NUM_Y_BITS;
    private static final int Y_FIELD = 0;
    private static final int X_FIELD = NUM_Y_BITS + NUM_Z_BITS;

    /**
     * Creates a position instance.
     */
    public Pos() {
    }

    /**
     * Creates a position instance.
     *
     * @param value The compressed position.
     */
    public Pos(long value) {
        this.x = unpackX(value);
        this.y = unpackY(value);
        this.z = unpackZ(value);
        this.value = value;
    }

    /**
     * Creates a position instance.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @param z coordinate.
     */
    public Pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = packAll(x, y, z);
    }

    /**
     * Creates a position instance.
     *
     * @param pos The position to duplicate.
     */
    public Pos(@Nonnull Pos pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.value = pos.value;
    }

    /**
     * Updates the position.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @param z coordinate.
     * @return self
     */
    @Nonnull
    public Pos set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Updates the position.
     *
     * @param pos The position to update.
     * @return self
     */
    @Nonnull
    public Pos set(@Nonnull Pos pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        return this;
    }

    /**
     * Adds to the position.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @param z coordinate.
     * @return self
     */
    @Nonnull
    public Pos add(int x, int y, int z) {
        return set(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds to the position.
     *
     * @param pos The position to add.
     * @return self
     */
    @Nonnull
    public Pos add(@Nonnull Pos pos) {
        return set(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    /**
     * Subtract from the position.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @param z coordinate.
     * @return self
     */
    @Nonnull
    public Pos sub(int x, int y, int z) {
        return set(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract from the position.
     *
     * @param pos The position to subtract.
     * @return self
     */
    @Nonnull
    public Pos sub(@Nonnull Pos pos) {
        return set(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    /**
     * @return Gets the x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * @return Gets the y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * @return Gets the z coordinate.
     */
    public int getZ() {
        return z;
    }

    /**
     * @return Gets the compressed coordinate.
     */
    public long asLong() {
        return value;
    }

    /**
     * Moves the position in the provided direction.
     *
     * @param dir The moving direction.
     * @return The new instance of object.
     */
    @Nonnull
    public Pos offset(@Nonnull Dir dir) {
        return new Pos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset());
    }

    /**
     * Moves the position in the provided direction.
     *
     * @param dir The moving direction.
     * @param n The moving distance.
     * @return The new instance of object.
     */
    @Nonnull
    public Pos offset(@Nonnull Dir dir, int n) {
        return n == 0 ? this : new Pos(x + dir.getXOffset() * n, y + dir.getYOffset() * n, z + dir.getZOffset() * n);
    }

    /**
     * Unpacks given compressed coordinate to the X.
     *
     * @param value The compress value.
     * @return x coordinate.
     */
    public static int unpackX(long value) {
        return (int)(value << 64 - X_FIELD - NUM_X_BITS >> 64 - NUM_X_BITS);
    }

    /**
     * Unpacks given compressed coordinate to the Y.
     *
     * @param value The compress value.
     * @return y coordinate.
     */
    public static int unpackY(long value) {
        return (int)(value << 64 - NUM_Y_BITS >> 64 - NUM_Y_BITS);
    }

    /**
     * Unpacks given compressed coordinate to the Z.
     *
     * @param value The compress value.
     * @return z coordinate.
     */
    public static int unpackZ(long value) {
        return (int)(value << 64 - Z_FIELD - NUM_Z_BITS >> 64 - NUM_Z_BITS);
    }

    /**
     * Packs all given coordinates.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @param z coordinate.
     * @return compressed coordinate.
     */
    public static long packAll(int x, int y, int z) {
        long i = 0L;
        i = i | ((long) x & X_MASK) << X_FIELD;
        i = i | ((long) y & Y_MASK) << Y_FIELD;
        i = i | ((long) z & Z_MASK) << Z_FIELD;
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pos) {
            Pos obj = (Pos) o;
            return (value == obj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int)(value ^ value >>> 32);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
