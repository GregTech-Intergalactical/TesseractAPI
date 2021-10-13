package tesseract.graph;

import net.minecraft.util.LazyValue;

import java.util.function.Supplier;

public class NodeCache<T> {
    private byte refCount;
    private final LazyValue<T> value;
    private boolean isPipe;
    /**
     * Creates a cache instance.
     */
    public NodeCache(Supplier<T> value) {
        this.value = new LazyValue<>(value);
        this.refCount = 1;
        this.isPipe = false;
    }
    public NodeCache<T> setIsPipe() {
        isPipe = true;
        return this;
    }

    public boolean isPipe() {
        return isPipe;
    }

    public void increaseCount() {
        this.refCount++;
    }

    public boolean decreaseCount() {
        this.refCount--;
        return refCount == 0;
    }

    public T value() {
        return value.getValue();
    }

    public byte count() {
        return refCount;
    }
}
