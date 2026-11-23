package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.features.url.mindmapmode.OldMapFolder;

public class SourceFileArchiver implements IMapChangeListener {
	public static final String MMP_FOLDER = "OLD_mmp";
	public static final String MMAP_FOLDER = "OLD_mmap";

	private final MapModel map;
	private final File source;
	private final String folderName;
	private final Consumer<File> notifier;

	public static void archiveAfterFirstSave(MapModel map, File source, String folderName) {
		if (map != null)
			map.addMapChangeListener(new SourceFileArchiver(map, source, folderName, SourceFileArchiver::showMoved));
	}

	SourceFileArchiver(MapModel map, File source, String folderName, Consumer<File> notifier) {
		this.map = map;
		this.source = source;
		this.folderName = folderName;
		this.notifier = notifier;
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getMap() != map || !MapChangeEventProperty.MAP_VIEW_TITLE.equals(event.getProperty()))
			return;
		File mapFile = map.getFile();
		if (!map.isSaved() || mapFile == null || !mapFile.exists())
			return;
		map.removeMapChangeListener(this);
		archive();
	}

	File archive() {
		File moved = OldMapFolder.moveInto(source, folderName);
		if (moved != null)
			notifier.accept(moved);
		return moved;
	}

	private static void showMoved(File moved) {
		JOptionPane.showMessageDialog(Controller.getCurrentController().getMapViewManager().getMapViewComponent(),
				TextUtils.format("mindmanager_import_source_moved", moved.getName(), moved.getParentFile().getName()),
				"Freeplane", JOptionPane.INFORMATION_MESSAGE);
	}
}
