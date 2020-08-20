package tesseract.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @link https://gist.github.com/icella/d4011b6808fc549c538c0310528d9e94
 */
public class CID {

    public static final int INVALID = Integer.MAX_VALUE;
    public static final int DEFAULT = Integer.MIN_VALUE;

    /**
     * Used to create an unique id by incrementation.
     */
    private static final AtomicInteger ATOMIC = new AtomicInteger(DEFAULT);

    /**
     * @return Increments id and return.
     */
    public static int nextId() {
        return ATOMIC.getAndIncrement();
    }
}
