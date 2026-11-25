package org.freeplane.main.application;

import java.util.List;

class RecentFilesHead {
	static boolean isAlreadyFirst(List<String> restorables, String restorable) {
		return !restorables.isEmpty() && restorables.get(0).equals(restorable);
	}
}
