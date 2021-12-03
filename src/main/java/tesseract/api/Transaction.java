package tesseract.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class Transaction<T> {
    private final ObjectArrayList<Consumer<T>> onCommit;
    private final ObjectArrayList<T> transmitted;
    private final Consumer<T> consumer;
    private boolean cancelled;

    public Transaction(final Consumer<T> consumed) {
        this.transmitted = new ObjectArrayList<>(1);
        this.onCommit = new ObjectArrayList<>(1);
        this.consumer = consumed;
        this.cancelled = false;
    }

    protected T addData(T t) {
        if (cancelled) return t;
        this.transmitted.add(t);
        return t;
    }
    
    public T getLast() {
        if (transmitted.size() == 0) throw new IllegalStateException("call to Transaction::getLast without data");
        return transmitted.get(transmitted.size()-1);
    }

    public List<T> getData() {
        return cancelled ? Collections.emptyList() : transmitted;
    }

    public void onCommit(Consumer<T> consumer) {
        if (cancelled) return;
        this.onCommit.ensureCapacity(transmitted.size());
        this.onCommit.add(transmitted.size() - 1, consumer);
    }

    public void pushCallback(Consumer<T> consumer) {
        if (cancelled || this.onCommit.size() == 0) return;
        Consumer<T> current = this.onCommit.get(this.onCommit.size()-1);
        if (current != null) {
            this.onCommit.add(this.onCommit.size()-1, current.andThen(consumer));
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void commit() {
        if (cancelled) return;
        for (int i = 0; i < transmitted.size(); i++) {
            if (onCommit.get(i) != null) {
                onCommit.get(i).accept(transmitted.get(i));
            }
            this.consumer.accept(transmitted.get(i));
        }
    }

    public abstract boolean isValid();

    public abstract boolean canContinue();

}
