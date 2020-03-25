package tesseract.util.fast;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;

/**
 * Wrapper for a Long2ByteMap class and listener for updates.
 */
public class Long2ByteMapListener {

    private Long2ByteMap map;
    private IListener listener;

    /**
     * Constructs a new Long2ByteMap with the same mappings as the specified Map.
     */
    public Long2ByteMapListener(Long2ByteMap map, IListener listener) {
        this.map = map;
        this.map.defaultReturnValue(Byte.MAX_VALUE);
        this.listener = listener;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     */
    public boolean containsKey(long key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     */
    public byte get(long key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     */
    public byte put(long key, byte value) {
        map.put(key, value);
        listener.update();
        return value;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     */
    public void putAll(Long2ByteMapListener wrapper) {
        map.putAll(wrapper.map);
        listener.update();
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     */
    public byte remove(long key) {
        byte value = map.remove(key);
        listener.update();
        return value;
    }

    /**
     * Unwrapping class to original one.
     */
    public Long2ByteMap unwrap() {
        return map;
    }
}