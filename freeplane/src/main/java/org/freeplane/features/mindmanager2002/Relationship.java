package org.freeplane.features.mindmanager2002;

class Relationship {
	final long id;
	final long sourceId;
	final long targetId;
	final String label;
	final int color;
	final boolean arrowAtBothEnds;

	Relationship(long id, long sourceId, long targetId, String label, int color, boolean arrowAtBothEnds) {
		this.id = id;
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.label = label;
		this.color = color;
		this.arrowAtBothEnds = arrowAtBothEnds;
	}
}
