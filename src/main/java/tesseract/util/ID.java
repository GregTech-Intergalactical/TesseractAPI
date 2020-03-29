package tesseract.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to create an unique id by incrementation.
 */
public class ID {

    public static final int INVALID = Integer.MAX_VALUE;
    public static final int DEFAULT = Integer.MIN_VALUE;

    private static final AtomicInteger root = new AtomicInteger(DEFAULT);

    /**
     * @return Increments id and return.
     */
    public static int getNewId() {
        int id = root.incrementAndGet();
        if (id == INVALID) {
            id = DEFAULT;
        }
        return id;
    }
}
