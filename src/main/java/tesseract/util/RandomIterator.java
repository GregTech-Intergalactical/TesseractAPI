package tesseract.util;

import it.unimi.dsi.fastutil.ints.IntIterator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

//TODO @Qubka unused, delete?

/**
 * Class acts as a wrapper of a random permute iterator over a list.
 */
@ParametersAreNonnullByDefault
public class RandomIterator<T> implements Iterator<T> {

    final List<T> delegate;
    final LCG iterator;

    /**
     * Creates a permute iterator wrapper over a consumer list.
     *
     * @param list The provided list.
     */
    public RandomIterator(List<T> list) {
        delegate = list;
        iterator = new LCG(list.size());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Nonnull
    @Override
    public T next() {
        return delegate.get(iterator.nextInt());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("RandomIterator::remove is not make sense!");
    }

    /**
     * This is an adaptation of LCGs.
     * By choosing parameters that guarantee full-periodicity you can randomly enumerate numbers within a range without repetition.
     * @link https://en.wikipedia.org/wiki/Linear_congruential_generator
     */
    private static class LCG implements IntIterator {

        final static int c = 1013904223, a = 1664525;
        final static double log2 = Math.log(2);

        int seed, N, m, next;
        boolean hasNext = true;

        /**
         * Creates a new instance of the custom iterator.
         *
         * @param size The max capacity of the iterated object.
         */
        LCG(int size) {
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
}
