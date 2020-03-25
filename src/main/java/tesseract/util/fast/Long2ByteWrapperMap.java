package tesseract.util.fast;

import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tesseract.graph.Connectivity;

/**
 * Wrapper for a Long2ByteMap class and listener for updates.
 */
public class Long2ByteWrapperMap {

    private Long2ByteMap map;
    private Long2ObjectMap<Connectivity.IListener> listeners;

    /**
     * Constructs a new Long2ByteMap with the same mappings as the specified Map.
     *
     * @param rv the new default return value.
     */
    public Long2ByteWrapperMap(byte rv) {
        map = new Long2ByteLinkedOpenHashMap();
        map.defaultReturnValue(rv);
        listeners = new Long2ObjectLinkedOpenHashMap<>();
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key The key value.
     * @return True or false.
     */
    public boolean containsKey(long key) {
        return map.containsKey(key);
    }

    /**
     * @return Gets the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     */
    public byte get(long key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key The key value.
     * @param value The provided value.
     * @param listener The listener function.
     */
    public byte put(long key, byte value, Connectivity.IListener listener) {
        map.put(key, value);
        listeners.put(key, listener);
        update();
        return value;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param wrapper The other object.
     */
    public void putAll(Long2ByteWrapperMap wrapper) {
        map.putAll(wrapper.map);
        listeners.putAll(wrapper.listeners);
        update();
    }

    /**
     * @param key The key value.
     * @return Gets the mapping for the specified key from this map if present.
     */
    public byte remove(long key) {
        byte value = map.remove(key);
        listeners.remove(key);
        update();
        return value;
    }

    /**
     * @return Gets original map.
     */
    public Long2ByteMap getMap() {
        return map;
    }

    /**
     * @return Gets listeners map.
     */
    public Long2ObjectMap<Connectivity.IListener> getListeners() {
        return listeners;
    }

    /**
     * Call attached listeners.
     */
    public void update() {
        for (Connectivity.IListener listener : listeners.values()) {
            listener.update();
        }
    }
}
