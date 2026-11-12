package org.freeplane.features.edge;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.nodestyle.NodeStyleShape;
import org.junit.Test;

public class EdgeAttachmentTest {
	@Test
	public void forkEdgesEnterAtTheBottomByDefault() {
		assertThat(EdgeAttachment.defaultFor(NodeStyleShape.fork)).isEqualTo(EdgeAttachment.bottom);
	}

	@Test
	public void otherShapesEnterInTheMiddleByDefault() {
		for (final NodeStyleShape shape : NodeStyleShape.values())
			if (shape != NodeStyleShape.fork)
				assertThat(EdgeAttachment.defaultFor(shape)).isEqualTo(EdgeAttachment.middle);
	}
}
