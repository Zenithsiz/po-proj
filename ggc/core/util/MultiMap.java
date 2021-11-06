package ggc.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/// A map from a key `K` to a list of values `V`.
public class MultiMap<K, V> {
	/// The underlying implementation as a map of lists
	private Map<K, List<V>> _map = new HashMap<>();

	/// Inserts a new value into the map
	public void put(K key, V value) {
		_map.computeIfAbsent(key, _key -> new ArrayList<>()).add(value);
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
	public static <K, V> Collector<Pair<K, V>, ?, MultiMap<K, V>> collector() {
		return new CollectorImpl<>();
	}

	/// Collector for `collector`
	private static class CollectorImpl<K, V> implements Collector<Pair<K, V>, MultiMap<K, V>, MultiMap<K, V>> {
		@Override
		public Supplier<MultiMap<K, V>> supplier() {
			return () -> new MultiMap<>();
		}

		@Override
		public BiConsumer<MultiMap<K, V>, Pair<K, V>> accumulator() {
			return (map, pair) -> map.put(pair.getLhs(), pair.getRhs());
		}

		@Override
		public BinaryOperator<MultiMap<K, V>> combiner() {
			return (lhsMap, rhsMap) -> {
				for (var pair : StreamIterator.streamIt(rhsMap.keyValuesStream())) {
					lhsMap.put(pair.getLhs(), pair.getRhs());
				}
				return lhsMap;
			};
		}

		@Override
		public Function<MultiMap<K, V>, MultiMap<K, V>> finisher() {
			return map -> map;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
		}

	}
}
