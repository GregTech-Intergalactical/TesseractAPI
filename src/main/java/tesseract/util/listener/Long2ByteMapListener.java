package tesseract.util.listener;

import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;

/**
 *
 */
public class Long2ByteMapListener {

    private Long2ByteMap map;
    private IListener listener;

    /**
     *
     * @param map
     * @param listener
     */
    public Long2ByteMapListener(Long2ByteMap map, IListener listener) {
        this.map = map;
        this.map.defaultReturnValue(Byte.MAX_VALUE);
        this.listener = listener;
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean containsKey(long key) {
        return map.containsKey(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public byte get(long key) {
        return map.get(key);
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public byte put(long key, byte value) {
        map.put(key, value);
        listener.update();
        return value;
    }

    /**
     *
     * @param wrapper
     */
    public void putAll(Long2ByteMapListener wrapper) {
        map.putAll(wrapper.map);
        listener.update();
    }

    /**
     *
     * @param key
     * @return
     */
    public byte remove(long key) {
        byte value = map.remove(key);
        listener.update();
        return value;
    }

    /**
     *
     */
    public Long2ByteMap unwrap() {
        return map;
    }
}