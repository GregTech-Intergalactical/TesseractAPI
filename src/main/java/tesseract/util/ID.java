package tesseract.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to create an unique id by incrementation.
 */
public class ID {

    public static final int INVALID = Integer.MAX_VALUE;
    private static AtomicInteger root = new AtomicInteger(Integer.MIN_VALUE);

    /**
     * @return Increments id and return.
     */
    public static int getNewId() {
        return root.incrementAndGet();
    }
}
