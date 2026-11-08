package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.EnumSet;

import org.junit.Test;

public class ImportProblemTest {
	private static final ImportProblem.Texts KEYS = new ImportProblem.Texts() {
		@Override
		public String text(String key) {
			return key.substring(key.lastIndexOf('.') + 1);
		}

		@Override
		public String format(String key, Object... arguments) {
			return MessageFormat.format(text(key) + " at {0}, lost {1}", arguments);
		}
	};

	@Test
	public void describesStoppedStageAndLostElements() {
		ImportProblem problem = new ImportProblem(ImportProblem.Kind.PARTIAL_TOPIC, "a > b", "detail",
				ReadStage.CLOUD, EnumSet.of(ReadStage.CLOUD, ReadStage.IMAGE));

		assertThat(problem.describe(KEYS)).isEqualTo("partial_topic at cloud, lost cloud, image");
	}

	@Test
	public void describesProblemWithoutStage() {
		ImportProblem problem = new ImportProblem(ImportProblem.Kind.IMAGE_IGNORED, "a", "detail");

		assertThat(problem.describe(KEYS)).isEqualTo("image_ignored");
	}
}
