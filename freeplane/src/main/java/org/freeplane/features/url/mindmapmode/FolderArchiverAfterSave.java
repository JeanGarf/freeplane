package org.freeplane.features.url.mindmapmode;

import java.io.File;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;

class FolderArchiverAfterSave implements IMapChangeListener {
	private final MapModel map;
	private final File folder;

	static void archiveAfterNextSave(MapModel map, File folder) {
		map.addMapChangeListener(new FolderArchiverAfterSave(map, folder));
	}

	FolderArchiverAfterSave(MapModel map, File folder) {
		this.map = map;
		this.folder = folder;
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getMap() != map || !MapChangeEventProperty.MAP_VIEW_TITLE.equals(event.getProperty()))
			return;
		final File mapFile = map.getFile();
		if (!map.isSaved() || mapFile == null || !mapFile.exists())
			return;
		map.removeMapChangeListener(this);
		archive();
	}

	File archive() {
		return folder.isDirectory() ? OldMapFolder.moveInto(folder) : null;
	}
}
