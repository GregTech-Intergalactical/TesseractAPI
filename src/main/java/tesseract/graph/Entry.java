package tesseract.graph;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

// Taken from https://stackoverflow.com/a/26164155
// Java implementation of Either, adapted to Zap
public class Entry<C, N> {

	public static <C, N> Entry<C, N> connector(C value) {
		return new Entry<>(Optional.of(value), Optional.empty());
	}
	public static <C, N> Entry<C, N> node(N value) {
		return new Entry<>(Optional.empty(), Optional.of(value));
	}

	private final Optional<C> connector;
	private final Optional<N> node;

	private Entry(Optional<C> l, Optional<N> r) {
		connector=l;
		node=r;
	}

	public <T> T map(Function<? super C, ? extends T> lFunc, Function<? super N, ? extends T> rFunc) {
		return connector.<T>map(lFunc).orElseGet(()->node.map(rFunc).get());
	}

	public <T> Entry<T,N> mapCable(Function<? super C, ? extends T> lFunc) {
		return new Entry<>(connector.map(lFunc),node);
	}

	public <T> Entry<C,T> mapEndpoint(Function<? super N, ? extends T> rFunc) {
		return new Entry<>(connector, node.map(rFunc));
	}

	public void apply(Consumer<? super C> lFunc, Consumer<? super N> rFunc) {
		connector.ifPresent(lFunc);
		node.ifPresent(rFunc);
	}

	public Optional<C> asCable() {
		return connector;
	}

	public Optional<N> asEndpoint() {
		return node;
	}
}