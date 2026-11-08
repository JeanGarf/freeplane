package org.freeplane.features.mindmanager2002;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

class ImportProblem {
	interface Texts {
		String text(String key);

		String format(String key, Object... arguments);
	}

	private static final String KEY_PREFIX = "mindmanager2002.";

	enum Kind {
		PARTIAL_TOPIC, REST_IGNORED, IMAGE_IGNORED
	}

	final Kind kind;
	final String branch;
	final String detail;
	final ReadStage stage;
	final Set<ReadStage> lost;

	ImportProblem(Kind kind, String branch, String detail) {
		this(kind, branch, detail, null, Collections.<ReadStage>emptySet());
	}

	ImportProblem(Kind kind, String branch, String detail, ReadStage stage, Set<ReadStage> lost) {
		this.kind = kind;
		this.branch = branch;
		this.detail = detail;
		this.stage = stage;
		this.lost = lost;
	}

	String describe(Texts texts) {
		String kindKey = KEY_PREFIX + "problem." + kind.name().toLowerCase(Locale.ROOT);
		if (stage == null)
			return texts.text(kindKey);
		StringBuilder lostNames = new StringBuilder();
		for (ReadStage lostStage : lost) {
			if (lostNames.length() > 0)
				lostNames.append(", ");
			lostNames.append(stageName(texts, lostStage));
		}
		return texts.format(kindKey, stageName(texts, stage), lostNames.toString());
	}

	private static String stageName(Texts texts, ReadStage stage) {
		return texts.text(KEY_PREFIX + "stage." + stage.name().toLowerCase(Locale.ROOT));
	}

	@Override
	public String toString() {
		return kind + (stage != null ? " at " + stage + " lost " + lost : "") + " " + branch + ": " + detail;
	}
}
