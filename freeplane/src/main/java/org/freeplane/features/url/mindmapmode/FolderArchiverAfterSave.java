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
		final FolderArchiverAfterSave archiver = new FolderArchiverAfterSave(map, folder);
		if (archiver.isMapSaved())
			archiver.archive();
		else
			map.addMapChangeListener(archiver);
	}

	boolean isMapSaved() {
		final File mapFile = map.getFile();
		return map.isSaved() && mapFile != null && mapFile.exists();
	}

	FolderArchiverAfterSave(MapModel map, File folder) {
		this.map = map;
		this.folder = folder;
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getMap() != map || !MapChangeEventProperty.MAP_VIEW_TITLE.equals(event.getProperty()))
			return;
		if (!isMapSaved())
			return;
		map.removeMapChangeListener(this);
		archive();
	}

	File archive() {
		return folder.isDirectory() ? OldMapFolder.moveInto(folder) : null;
	}
}
