package ggc.app.products;

import pt.tecnico.uilib.Display;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ggc.core.Batch;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/** Shows batches */
class ShowBatches {
	/** Shows all batches from `receiver` to `display`, filtering using `predicate`. */
	public static final void executeFilter(WarehouseManager receiver, Display display,
			Predicate<? super Batch> predicate) {
		Stream<Batch> batches = receiver.getBatches().filter(predicate).sorted(receiver.batchComparator());
		for (var batch : StreamIterator.streamIt(batches)) {
			display.addLine(receiver.format(batch));
		}

		display.display();
	}

}
