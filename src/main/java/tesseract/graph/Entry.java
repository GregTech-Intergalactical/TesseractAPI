package tesseract.graph;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A class that acts as a container for a value of one of two types.
 * <p>
 * An Entry can be used to express a success or failure case. By convention,
 * Right is used to store the success value, (you can use the play on words
 * "right" == "correct" as a mnemonic) and Left is used to store failure values
 * (such as exceptions).
 * <p>
 * While this class is public and abstract it does not expose a constructor as
 * only the concrete Left and Right subclasses are meant to be used.
 * <p>
 * Entry is immutable, but does not force immutability on contained objects; if
 * the contained objects are mutable then equals and hashcode methods should not
 * be relied on.
 *
 * @link https://stackoverflow.com/a/26164155
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Entry<C, N> {

	protected static <C, N> Entry<C, N> connector(C value) {
		return new Entry<>(Optional.of(value), Optional.empty());
	}
	protected static <C, N> Entry<C, N> node(N value) {
		return new Entry<>(Optional.empty(), Optional.of(value));
	}
	protected static <C, N> Entry<C, N> empty() {
		return new Entry<>(Optional.empty(), Optional.empty());
	}

	private final Optional<C> connector;
	private final Optional<N> node;

	private Entry(Optional<C> l, Optional<N> r) {
		connector = l;
		node = r;
	}

	public <T> T map(Function<? super C, ? extends T> lFunc, Function<? super N, ? extends T> rFunc) {
		return connector.<T>map(lFunc).orElseGet(() -> node.map(rFunc).get());
	}

	public <T> Entry<T,N> mapCable(Function<? super C, ? extends T> lFunc) {
		return new Entry<>(connector.map(lFunc), node);
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