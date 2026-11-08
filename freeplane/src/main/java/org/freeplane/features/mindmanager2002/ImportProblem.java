package org.freeplane.features.mindmanager2002;

import java.util.Locale;

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

	ImportProblem(Kind kind, String branch, String detail) {
		this(kind, branch, detail, null);
	}

	ImportProblem(Kind kind, String branch, String detail, ReadStage stage) {
		this.kind = kind;
		this.branch = branch;
		this.detail = detail;
		this.stage = stage;
	}

	String describe(Texts texts) {
		String kindKey = KEY_PREFIX + "problem." + kind.name().toLowerCase(Locale.ROOT);
		if (stage == null)
			return texts.text(kindKey);
		StringBuilder lost = new StringBuilder();
		for (ReadStage lostStage : stage.lostElements()) {
			if (lost.length() > 0)
				lost.append(", ");
			lost.append(stageName(texts, lostStage));
		}
		return texts.format(kindKey, stageName(texts, stage), lost.toString());
	}

	private static String stageName(Texts texts, ReadStage stage) {
		return texts.text(KEY_PREFIX + "stage." + stage.name().toLowerCase(Locale.ROOT));
	}

	@Override
	public String toString() {
		return kind + (stage != null ? " at " + stage : "") + " " + branch + ": " + detail;
	}
}
