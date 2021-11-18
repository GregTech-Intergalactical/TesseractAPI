package tesseract.graph;

import net.minecraft.util.Direction;

public class NodeCache<T> {
    private byte bitMap;
    private final T value;

    /**
     * Creates a cache instance.
     */
    public NodeCache(T value, Direction side) {
        this.value = value;
        this.bitMap = 0;
        setSide(side);
    }

    public NodeCache(T value) {
        this.value = value;
        this.bitMap = 0;
        bitMap |= 1 << 7;
    }

    public boolean connects(Direction side) {
        return ((bitMap & (1 << side.get3DDataValue())) > 0);
    }

    public boolean setSide(Direction side) {
        byte old = bitMap;
        this.bitMap |= 1 << side.get3DDataValue();
        return old != bitMap;
    }

    public boolean clearSide(Direction side) {
        this.bitMap &= ~(1 << (side.get3DDataValue()));
        return bitMap != 0;
    }

    public boolean isPipe() {
        return (bitMap & (1 << 7)) != 0;
    }

    public T value() {
        return value;
    }


    public int count() {
        return Integer.bitCount(bitMap);
    }
}
