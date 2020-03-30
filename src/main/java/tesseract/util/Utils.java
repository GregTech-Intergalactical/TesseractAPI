package tesseract.util;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static final int INVALID = Integer.MAX_VALUE;
    public static final int DEFAULT = Integer.MIN_VALUE;

    /**
     * Used to create an unique id by incrementation.
     */
    private static final AtomicInteger ATOMIC_ID = new AtomicInteger(DEFAULT);

    /**
     * @return Increments id and return.
     */
    public static int getNewId() {
        int id = ATOMIC_ID.incrementAndGet();
        if (id == INVALID) {
            id = DEFAULT;
        }
        return id;
    }
}
