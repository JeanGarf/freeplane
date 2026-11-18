package org.freeplane.features.map.mindmapmode.clipboard;

public enum PasteImageTarget {
	NODE("PasteImageAsNodeAction"),
	DETAILS("PasteImageAsDetailsAction"),
	NOTE("PasteImageAsNoteAction"),
	EXTENSION("PasteImageAsExtensionAction"),
	LINKED_FILE("PasteImageAsLinkedFileAction");

	public static final String PROPERTY_NAME = "paste_image_target";
	public static final PasteImageTarget DEFAULT = NOTE;

	public final String actionKey;

	private PasteImageTarget(String actionKey) {
		this.actionKey = actionKey;
	}

	public static PasteImageTarget parse(String name) {
		if (name != null)
			for (final PasteImageTarget target : values())
				if (target.name().equalsIgnoreCase(name.trim()))
					return target;
		return DEFAULT;
	}
}
