package org.freeplane.features.mindmanager2002;

import java.util.ArrayList;
import java.util.List;

enum ReadStage {
	TEXT, ATTACHED_OBJECTS, STYLE, NOTE, RELATIONSHIPS, ICONS, CLOUD, PROPERTIES, IMAGE, HYPERLINK;

	List<ReadStage> lostElements() {
		List<ReadStage> lost = new ArrayList<>();
		for (ReadStage stage : values()) {
			if (stage.ordinal() >= ordinal() && stage != PROPERTIES)
				lost.add(stage);
		}
		return lost;
	}
}
