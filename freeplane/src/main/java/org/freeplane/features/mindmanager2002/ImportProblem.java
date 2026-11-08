package org.freeplane.features.mindmanager2002;

class ImportProblem {
	enum Kind {
		PARTIAL_TOPIC, REST_IGNORED, IMAGE_IGNORED
	}

	final Kind kind;
	final String branch;
	final String detail;

	ImportProblem(Kind kind, String branch, String detail) {
		this.kind = kind;
		this.branch = branch;
		this.detail = detail;
	}

	@Override
	public String toString() {
		return kind + " " + branch + ": " + detail;
	}
}
