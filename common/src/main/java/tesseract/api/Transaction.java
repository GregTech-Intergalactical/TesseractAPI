package tesseract.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public abstract class Transaction<T> {
    private final ObjectArrayList<Consumer<T>> onCommit;
    private final ObjectArrayList<T> transmitted;
    private final Consumer<T> consumer;
    private boolean committed;

    public Transaction(final Consumer<T> consumed) {
        this.transmitted = new ObjectArrayList<>(1);
        this.onCommit = new ObjectArrayList<>(1);
        this.consumer = consumed;
        this.committed = false;
    }

    protected T addData(T t) {
        if (committed) return t;
        this.transmitted.add(t);
        return t;
    }

    public T getLast() {
        if (transmitted.size() == 0) throw new IllegalStateException("call to Transaction::getLast without data");
        return transmitted.get(transmitted.size()-1);
    }

    public Iterable<T> getOffset(int j) {
        return () ->{
            Iterator<T> t = this.transmitted.iterator();
            for (int i = 0; i < j; i++) {
                t.next();
            }
            return t;
        };
    }

    public List<T> getData() {
        return committed ? Collections.emptyList() : transmitted;
    }

    public void onCommit(Consumer<T> consumer) {
        if (committed) return;
        this.onCommit.ensureCapacity(transmitted.size());
        this.onCommit.add(transmitted.size() - 1, consumer);
    }

    public void withCallbackBefore(int offset, Consumer<T> consumer) {
        for (int i = offset; i < this.onCommit.size(); i++) {
            var value = this.onCommit.get(i);
            this.onCommit.add(i, consumer.andThen(value));
        }
    }

    public void withCallbackAfter(int offset, Consumer<T> consumer) {
        for (int i = offset; i < this.onCommit.size(); i++) {
            var value = this.onCommit.get(i);
            this.onCommit.add(i, value.andThen(consumer));
        }
    }
    public void commit() {
        if (committed) return;
        for (int i = 0; i < transmitted.size(); i++) {
            if (onCommit.get(i) != null) {
                onCommit.get(i).accept(transmitted.get(i));
            }
            this.consumer.accept(transmitted.get(i));
        }
        committed = true;
    }

    public abstract boolean isValid();

    public abstract boolean canContinue();

}
