package tesseract.util;

import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.Random;

/**
 * This is an adaptation of LCGs.
 * By choosing parameters that guarantee full-periodicity you can randomly enumerate numbers within a range without repetition.
 * @link https://en.wikipedia.org/wiki/Linear_congruential_generator
 */
public class RandomPermuteIterator implements IntIterator {

    private final static int c = 1013904223, a = 1664525;
    private final static double log2 = Math.log(2);

    private int seed, N, m, next;
    private boolean hasNext = true;

    /**
     * Creates a new instance of the custom iterator.
     *
     * @param size The max capacity of the iterated object.
     */
    public RandomPermuteIterator(int size) {
        N = size;
        m = (int) Math.pow(2, Math.ceil(Math.log(N) / log2));
        next = seed = new Random().nextInt(Math.min(N, Integer.MAX_VALUE));
    }

    /**
     * @return This method returns true if and only if this enumeration object
     *         contains at least one more element to provide; false otherwise.
     */
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * @return This method throws NoSuchElementException if no more elements exist.
     */
    @Override
    public int nextInt() {
        next = (a * next + c) % m;
        while (next >= N) next = (a * next + c) % m;
        if (next == seed) hasNext = false;
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("RandomPermuteIterator::remove is not exist!");
    }
}