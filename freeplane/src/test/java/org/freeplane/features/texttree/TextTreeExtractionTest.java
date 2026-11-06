package org.freeplane.features.texttree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.junit.Before;
import org.junit.Test;

public class TextTreeExtractionTest {
	private MapFake mapFake;
	private NodeModel car;
	private NodeModel toyota;
	private NodeModel renault;
	private final Set<NodeModel> filteredOut = new HashSet<NodeModel>();

	@Before
	public void setUp() {
		mapFake = new MapFake();
		car = childOf(mapFake.getRoot(), "Car");
		toyota = childOf(car, "Toyota");
		childOf(toyota, "Corolla");
		renault = childOf(car, "Renault");
		childOf(renault, "Clio");
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	private String extract(final NodeModel start, final boolean includesStart, final boolean includesFolded) {
		return new TextTreeExtraction(includesFolded, node -> !filteredOut.contains(node),
			node -> node.getText(), node -> node == toyota ? "https://www.toyota.fr" : null)
			.extract(start, includesStart).toString();
	}

	@Test
	public void extractsBranchWithItsRootAndLinks() {
		assertThat(extract(car, true, true))
			.isEqualTo("[Car[Toyota (https://www.toyota.fr)[Corolla], Renault[Clio]]]");
	}

	@Test
	public void extractsOnlyChildrenWithoutRoot() {
		assertThat(extract(car, false, true)).isEqualTo("[Toyota (https://www.toyota.fr)[Corolla], Renault[Clio]]");
	}

	@Test
	public void skipsContentOfFoldedBranchesUnlessIncluded() {
		toyota.setFolded(true);

		assertThat(extract(car, true, false)).isEqualTo("[Car[Toyota (https://www.toyota.fr), Renault[Clio]]]");
		assertThat(extract(car, true, true))
			.isEqualTo("[Car[Toyota (https://www.toyota.fr)[Corolla], Renault[Clio]]]");
	}

	@Test
	public void keepsChildrenOfFoldedStartNode() {
		car.setFolded(true);

		assertThat(extract(car, true, false))
			.isEqualTo("[Car[Toyota (https://www.toyota.fr)[Corolla], Renault[Clio]]]");
	}

	@Test
	public void skipsFilteredNodeButKeepsItsShownDescendants() {
		filteredOut.add(renault);

		assertThat(extract(car, true, true)).isEqualTo("[Car[Toyota (https://www.toyota.fr)[Corolla], Clio]]");
	}
}
