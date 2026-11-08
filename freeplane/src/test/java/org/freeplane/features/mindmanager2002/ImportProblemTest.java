package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

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
	public void describesStoppedStageAndLostElementsWithoutInternalProperties() {
		ImportProblem problem = new ImportProblem(ImportProblem.Kind.PARTIAL_TOPIC, "a > b", "detail",
				ReadStage.CLOUD);

		assertThat(problem.describe(KEYS)).isEqualTo("partial_topic at cloud, lost cloud, image, hyperlink");
	}

	@Test
	public void describesProblemWithoutStage() {
		ImportProblem problem = new ImportProblem(ImportProblem.Kind.IMAGE_IGNORED, "a", "detail");

		assertThat(problem.describe(KEYS)).isEqualTo("image_ignored");
	}

	@Test
	public void stoppingAtSymbolLosesEverythingButText() {
		assertThat(ReadStage.ATTACHED_OBJECTS.lostElements()).containsExactly(ReadStage.ATTACHED_OBJECTS,
				ReadStage.STYLE, ReadStage.NOTE, ReadStage.RELATIONSHIPS, ReadStage.ICONS, ReadStage.CLOUD,
				ReadStage.IMAGE, ReadStage.HYPERLINK);
	}
}
