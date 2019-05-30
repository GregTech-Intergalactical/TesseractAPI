package zap.graph;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

// Taken from https://stackoverflow.com/a/26164155
// Basic Java implementation from Entry<C, E>
public class Entry<C, E>
{
	public static <C, E> Entry<C, E> cable(C value) {
		return new Entry<>(Optional.of(value), Optional.empty());
	}
	public static <C, E> Entry<C, E> endpoint(E value) {
		return new Entry<>(Optional.empty(), Optional.of(value));
	}
	private final Optional<C> cable;
	private final Optional<E> endpoint;
	private Entry(Optional<C> l, Optional<E> r) {
		cable=l;
		endpoint=r;
	}
	public <T> T map(
			Function<? super C, ? extends T> lFunc,
			Function<? super E, ? extends T> rFunc)
	{
		return cable.<T>map(lFunc).orElseGet(()->endpoint.map(rFunc).get());
	}
	public <T> Entry<T,E> mapCable(Function<? super C, ? extends T> lFunc)
	{
		return new Entry<>(cable.map(lFunc),endpoint);
	}
	public <T> Entry<C,T> mapEndpoint(Function<? super E, ? extends T> rFunc)
	{
		return new Entry<>(cable, endpoint.map(rFunc));
	}
	public void apply(Consumer<? super C> lFunc, Consumer<? super E> rFunc)
	{
		cable.ifPresent(lFunc);
		endpoint.ifPresent(rFunc);
	}

	public Optional<C> asCable() {
		return cable;
	}

	public Optional<E> asEndpoint() {
		return endpoint;
	}
}