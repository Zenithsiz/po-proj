package ggc.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/// A map from a key `K` to a set of sorted values `V`.
public class SortedMultiMap<K, V> {
	/// The underlying implementation as a map of sorted sets
	private Map<K, SortedSet<V>> _map = new HashMap<>();

	/// Comparator
	private Comparator<? super V> _comparator;

	public SortedMultiMap(Comparator<? super V> comparator) {
		_comparator = comparator;
	}

	/// Inserts a new value into the map
	public void put(K key, V value) {
		_map.computeIfAbsent(key, _key -> new TreeSet<>(_comparator)).add(value);
	}

	/// Returns a stream over all keys and values in this map
	public Stream<Pair<K, V>> keyValuesStream() {
		return _map.entrySet().stream().flatMap(
				keyValues -> keyValues.getValue().stream().map(value -> new Pair<>(keyValues.getKey(), value)));
	}

	/// Returns a stream over all values in this map
	public Stream<V> valuesStream() {
		return _map.values().stream().flatMap(values -> values.stream());
	}

	/// Returns a collector for this map
	public static <K, V> Collector<Pair<K, V>, ?, SortedMultiMap<K, V>> collector(Comparator<? super V> comparator) {
		return new CollectorImpl<>(comparator);
	}

	/// Collector for `collector`
	private static class CollectorImpl<K, V>
			implements Collector<Pair<K, V>, SortedMultiMap<K, V>, SortedMultiMap<K, V>> {
		/// Comparator
		Comparator<? super V> _comparator;

		public CollectorImpl(Comparator<? super V> _comparator) {
			this._comparator = _comparator;
		}

		@Override
		public Supplier<SortedMultiMap<K, V>> supplier() {
			return () -> new SortedMultiMap<>(_comparator);
		}

		@Override
		public BiConsumer<SortedMultiMap<K, V>, Pair<K, V>> accumulator() {
			return (map, pair) -> map.put(pair.getLhs(), pair.getRhs());
		}

		@Override
		public BinaryOperator<SortedMultiMap<K, V>> combiner() {
			return (lhsMap, rhsMap) -> {
				for (var pair : StreamIterator.streamIt(rhsMap.keyValuesStream())) {
					lhsMap.put(pair.getLhs(), pair.getRhs());
				}
				return lhsMap;
			};
		}

		@Override
		public Function<SortedMultiMap<K, V>, SortedMultiMap<K, V>> finisher() {
			return map -> map;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
		}

	}
}
