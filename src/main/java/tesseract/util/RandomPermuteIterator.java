package tesseract.util;

import java.util.Random;

/**
 * This is an adaptation of LCGs.
 * By choosing parameters that guarantee full-periodicity you can randomly enumerate numbers within a range without repetition.
 * @link https://en.wikipedia.org/wiki/Linear_congruential_generator
 */
public class RandomPermuteIterator {
    private final static int c = 1013904223, a = 1664525;
    private int seed, N, m, next;
    private boolean hasNext = true;

    /**
     * Creates a new instance of the Iterator.
     *
     * @param size The max capacity of the iterated object.
     */
    public RandomPermuteIterator(int size) {
        N = size;
        m = (int) Math.pow(2, Math.ceil(Math.log(N) / Math.log(2)));
        next = seed = new Random().nextInt(Math.min(N, Integer.MAX_VALUE));
    }

    /**
     * @return This method returns true if and only if this enumeration object
     *         contains at least one more element to provide; false otherwise.
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * @return This method throws NoSuchElementException if no more elements exist.
     */
    public int next() {
        next = (a * next + c) % m;
        while (next >= N) next = (a * next + c) % m;
        if (next == seed) hasNext = false;
        return next;
    }
}