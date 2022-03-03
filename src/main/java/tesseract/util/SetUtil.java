package tesseract.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public class SetUtil {

    public static <T>Set<T> union(Set<T> first, Set<T> second) {
        Set<T> set = new ObjectOpenHashSet<>(first);
        set.addAll(second);
        return set;
    }

    public static <T>Set<T> union(Set<T> first, Set<T> second, Predicate<T> keep) {
        Set<T> set = new ObjectOpenHashSet<>(first);
        second.forEach(t -> {
            if (keep.test(t)) set.add(t);
        });
        return set;
    }

    public static LongSet union(LongSet first, LongSet second, LongPredicate keep) {
        LongSet set = new LongOpenHashSet(first);
        second.forEach((Consumer<? super Long>) t -> {
            if (keep.test(t)) set.add(t);
        });
        return set;
    }
}
