package ggc.core.util;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/// Result type, either a value `T`, or exception error
public interface Result<T> {
	/// Returns an `Ok` result of type `T`
	public static <T> Result<T> ofOk(T value) {
		return new Ok<>(value);
	}

	/// Returns an `Err` result of type `E`
	public static <T, E extends Throwable> Result<T> ofErr(E err) {
		return new Err<>(err);
	}

	/// Returns an `Ok` result if `opt` is present, else returns `Err` with `errSupplier`
	public static <T, E extends Throwable> Result<T> fromOptional(Optional<T> opt, Supplier<E> errSupplier) {
		if (opt.isPresent()) {
			return Result.ofOk(opt.get());
		} else {
			return Result.ofErr(errSupplier.get());
		}
	}

	/// Maps this result with `onOk` if it's `Ok`.
	public <U> Result<U> map(Function<T, U> mapper);

	/// If this is an `Err`, maps it to a result of any value type
	public <U> Optional<Result<U>> mapWithErr();

	/// Returns this as `T`, if it is `Ok`, else throws `NoSuchElementException`
	public T getOk() throws NoSuchElementException;

	/// Returns if this result is an `Ok`
	public boolean isOk();

	/// Returns if this result is an `Err`
	public boolean isErr();

	/// Returns the value in this result, or throws, if it's an error.
	/// 
	/// Throws `NoSuchElementException` if the result is an `Err`, but the
	/// contained error isn't `errClass`.
	public <E extends Throwable> T getOrThrow(Class<E> errClass) throws E, NoSuchElementException;

	/// `Ok` type of the result
	public class Ok<T> implements Result<T> {
		private T _value;

		public Ok(T value) {
			_value = value;
		}

		/// Returns the value
		public T getValue() {
			return _value;
		}

		public <U> Result<U> map(Function<T, U> mapper) {
			return new Ok<>(mapper.apply(_value));
		}

		public <U> Optional<Result<U>> mapWithErr() {
			return Optional.empty();
		}

		public T getOk() {
			return _value;
		}

		public boolean isOk() {
			return true;
		}

		public boolean isErr() {
			return false;
		}

		public <E extends Throwable> T getOrThrow(Class<E> errClass) {
			return _value;
		}
	}

	/// `Error` type of the result
	public class Err<T, E extends Throwable> implements Result<T> {
		private E _err;

		public Err(E err) {
			_err = err;
		}

		/// Returns the exception
		public E getErr() {
			return _err;
		}

		public <U> Result<U> map(Function<T, U> mapper) {
			return new Err<>(_err);
		}

		public <U> Optional<Result<U>> mapWithErr() {
			return Optional.of(new Err<>(_err));
		}

		public T getOk() throws NoSuchElementException {
			throw new NoSuchElementException("Result was error");
		}

		public boolean isOk() {
			return false;
		}

		public boolean isErr() {
			return true;
		}

		@SuppressWarnings("unchecked") // We're manually checking
		public <E2 extends Throwable> T getOrThrow(Class<E2> errClass) throws E2, NoSuchElementException {
			if (errClass.isInstance(_err)) {
				throw (E2) _err;
			}

			throw new NoSuchElementException("Error specified was not correct");
		}
	}
}
