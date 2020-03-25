package tesseract.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to create an unique id or hash by incrementation.
 */
public class NanoID {

    public static final int INVALID = Integer.MAX_VALUE;
    public static final int DEFAULT = Integer.MIN_VALUE;

    private static AtomicInteger id = new AtomicInteger(DEFAULT);
    private static AtomicInteger hs = new AtomicInteger(DEFAULT);

    /**
     * @return Increments id and return.
     */
    public static int getNewId() {
        return increment(id);
    }

    /**
     * @return Increments hash and return.
     */
    public static int getNewHash() {
        return increment(hs);
    }

    /**
     * @param ref The ref to AtomicInteger object.
     * @return The function returns the value after increment operation is performed to the previous value.
     */
    private static int increment(AtomicInteger ref) {
        int id = ref.incrementAndGet();
        if (id == INVALID) {
            id = DEFAULT;
        }
        return id;
    }
}
