/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.application;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.DocuMapAttribute;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class manages a list of the maps that were opened last. It aims to
 * provide persistence for the last recent maps and the last selected nodes (in separate properties).
 * Maps should be shown in the format:"mode\:key",ie."Mindmap\:/home/joerg/freeplane.mm"
 */

public class LastOpenedList implements IMapViewChangeListener, IMapChangeListener {
    static class RecentFile {
        public RecentFile(String restorable) {
            this.restorable = restorable;
        }


        public RecentFile(RecentFile prototype) {
			super();
			this.restorable = prototype.restorable;
			this.lastVisitedNodeId = prototype.lastVisitedNodeId;
			this.lastRootNodeId = prototype.lastRootNodeId;
			this.lastRootHistoryNodeIds = new ArrayList<>(prototype.lastRootHistoryNodeIds);
		}


		String restorable;
        /** persisted, but not necessary not-null. */
        String lastVisitedNodeId;

        String lastRootNodeId;
        List<String> lastRootHistoryNodeIds = Collections.emptyList();

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((restorable == null) ? 0 : restorable.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RecentFile other = (RecentFile) obj;
            if (restorable == null) {
                if (other.restorable != null)
                    return false;
            }
            else if (!restorable.equals(other.restorable))
                return false;
            return true;
        }
		@Override
        public String toString() {
	        return "RecentFile(" + restorable + "@" + lastVisitedNodeId + "/"
	                + lastRootNodeId + "/" + lastRootHistoryNodeIds + ")";
        }
    }

	private static final String LAST_OPENED_LIST_LENGTH = "last_opened_list_length";
	private static final String LAST_OPENED = "lastOpened_1.0.20";
	private static final String LAST_LOCATIONS = "lastLocations";
	private static final String LAST_ROOTS = "lastRoots";
	private static final String LAST_ROOTS_HISTORY = "lastRootsHistory";
    private static final String LAST_MODE = "lastMode";
	private static boolean PORTABLE_APP = System.getProperty("portableapp", "false").equals("true");
	private static String USER_DRIVE = System.getProperty("user.home", "").substring(0, 2);

	final private List<RecentFile> lastOpenedList = new LinkedList<RecentFile>();
	private RecentFile mapSelectedOnStart;

	LastOpenedList() {
		restore();

	}

	public void registerMenuContributor(final ModeController modeController) {
		modeController.addUiBuilder(Phase.ACTIONS, "lastOpenedMaps", new EntryVisitor() {

			@Override
			public void visit(Entry target) {
				updateMenus(modeController, target);
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		}, new ChildActionEntryRemover(modeController));

	}

	@Override
	public void afterViewChange(final Component oldView, final Component newView) {
		updateLastVisitedNodeId(oldView);
		if (newView == null) {
			updateMenus();
			return;
		}
		final MapModel map = getMapModel(newView);
		final String restoreString = getRestoreable(map);
		updateList(map, restoreString);
    }

	private boolean selectLastVisitedNode(RecentFile recentFile) {
		if (recentFile != null && recentFile.lastVisitedNodeId != null) {
			final Controller controller = Controller.getCurrentController();
			IMapSelection selection = controller.getSelection();
            final MapModel map = selection.getMap();
			if (selection.isSelected(map.getRootNode())) {
				restoreRootsHistory(recentFile);
				if(recentFile.lastRootNodeId !=  null) {
					final NodeModel root = map.getNodeForID(recentFile.lastRootNodeId);
					if(root != null)
						controller.getMapViewManager().setViewRoot(root);
				}
				final NodeModel node = map.getNodeForID(recentFile.lastVisitedNodeId);
				if (node != null && node.hasVisibleContent(selection.getFilter())) {
					// don't override node selection done by UriManager.loadURI()
					selection.selectAsTheOnlyOneSelected(node);
				}
				return true;
			}
		}
		return false;
	}

	private boolean saveLastPositionInMapEnabled() {
	    return ResourceController.getResourceController().getBooleanProperty("save_last_position_in_map");
    }

	@Override
	public void afterViewDisplayed(Component oldView, Component newView) {
		final MapModel map = getMapModel(newView);
		final RecentFile recentFile = findRecentFileByMapModel(map);
		// the next line will only succeed if the map is already opened
		if(oldView instanceof MapView && newView instanceof MapView
		        && ((MapView)oldView).getMap() == ((MapView)newView).getMap()) {
		    List<NodeModel> nodes = ((MapView)oldView).getMapSelection().getOrderedSelection();
            ((MapView)newView).getMapSelection().replaceSelection(nodes.toArray(new NodeModel[nodes.size()]));
		}
		else if (saveLastPositionInMapEnabled() && ! selectLastVisitedNode(recentFile)) {
			ensureSelectLastVisitedNodeOnOpen(map, recentFile);
		}
	}

	private void ensureSelectLastVisitedNodeOnOpen(final MapModel map, final RecentFile recentFile) {
	    final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (recentFile != null && recentFile.lastVisitedNodeId != null) {
			final WeakReference<MapModel> mapReference = new WeakReference<>(map);
			RecentFile recentFileCopy = new RecentFile(recentFile);
			mapController.addNodeSelectionListener(new INodeSelectionListener() {
				@Override
				public void onSelect(NodeModel node) {
					MapModel map = mapReference.get();
					if(map == null)
						mapController.removeNodeSelectionListener(this);
					else if (node.getMap() == map) {
						mapController.removeNodeSelectionListener(this);
						selectLastVisitedNode(recentFileCopy);
					}
				}
			});
		}
    }

	private MapModel getMapModel(final Component mapView) {
	    final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		return mapViewManager.getMap(mapView);
    }

	private int getMaxMenuEntries() {
		return ResourceController.getResourceController().getIntProperty(LAST_OPENED_LIST_LENGTH, 25);
	}

	private String getRestorable(final File file) {
		if (file == null //
				|| !AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
					@Override
					public Boolean run() {
						return file.exists();
					}
		})) {
			return null;
		}
		final String absolutePath = file.getAbsolutePath();
		if (!PORTABLE_APP || !USER_DRIVE.endsWith(":")) {
			return "MindMap:" + absolutePath;
		}
		final String diskName = absolutePath.substring(0, 2);
		if (!diskName.equals(USER_DRIVE)) {
			return "MindMap:" + absolutePath;
		}
		return "MindMap::" + absolutePath.substring(2);
	}

	public String getRestoreable( final MapModel map) {
		if (map == null) {
			return null;
		}
		//ignore documentation maps loaded using documentation actions
		if(map.containsExtension(DocuMapAttribute.class))
			return null;
		final File file = map.getFile();
		return getRestorable(file);
	}

	@Override
	public void mapChanged(final MapChangeEvent event) {
		if (!event.getProperty().equals(UrlManager.MAP_URL)) {
			return;
		}
		final URL after = (URL) event.getNewValue();
		if (after != null) {
			final String fileAfter = after.getFile();
			if (fileAfter != null) {
				final String restorable = getRestorable(new File(fileAfter));
				updateList(event.getMap(), restorable);
			}
		}
	}

    public void open(final RecentFile recentFile) throws FileNotFoundException, MalformedURLException, IOException,
            URISyntaxException, XMLException {
        if (recentFile == null)
            return;
        final StringTokenizer tokens = new StringTokenizer(recentFile.restorable, ":");
        if (!tokens.hasMoreTokens())
            return;
        final String mode = tokens.nextToken();
        Controller.getCurrentController().selectMode(mode);
        File file = createFileFromRestorable(tokens);
        final URL url = Compat.fileToUrl(file);
        if (!tryToChangeToMapView(url))
			Controller.getCurrentModeController().getMapController().openMap(url);
    }

	public File createFileFromRestorable(StringTokenizer tokens) {
		String fileName = tokens.nextToken(";").substring(1);
		if (PORTABLE_APP && fileName.startsWith(":") && USER_DRIVE.endsWith(":")) {
			fileName = USER_DRIVE + fileName.substring(1);
		}
		File file = new File(fileName);
		return file;
	}

	void openLastMapOnStart(final List<URL> notRestoredMapUrls, final Consumer<File> afterReopened) {
		final List<RecentFile> toReopen = notRestoredMaps(notRestoredMapUrls);
		if (mapSelectedOnStart != null)
			toReopen.add(mapSelectedOnStart);
		RecentFileOpening.reopenAll(toReopen, this::open,
		    this::askWhatToDoWithUnopenedFile,
		    recentFile -> afterReopened.accept(fileOf(recentFile)),
		    recentFile -> {
		        lastOpenedList.remove(recentFile);
		        updateMenus();
		    });
		String lastMode = ResourceController.getResourceController().getProperty(LAST_MODE);
		if(lastMode != null && ! lastMode.equals( Controller.getCurrentModeController().getModeName()))
		    Controller.getCurrentController().selectMode(lastMode);
	}

	private List<RecentFile> notRestoredMaps(final List<URL> notRestoredMapUrls) {
		final List<File> notRestoredFiles = new ArrayList<File>();
		for (URL url : notRestoredMapUrls)
			notRestoredFiles.add(new File(url.getFile()));
		return NotRestoredMaps.toReopen(new ArrayList<RecentFile>(lastOpenedList), this::fileOf, notRestoredFiles,
		    mapSelectedOnStart);
	}

	private File fileOf(final RecentFile recentFile) {
		final StringTokenizer tokens = new StringTokenizer(recentFile.restorable, ":");
		if (!tokens.hasMoreTokens())
			return null;
		tokens.nextToken();
		return createFileFromRestorable(tokens);
	}

	private void restore() {
        final List<String> lastOpened = getListPropertyNotNull(LAST_OPENED);
        final List<String> lastLocation = getListPropertyNotNull(LAST_LOCATIONS);
        final List<String> lastRoot = getListPropertyNotNull(LAST_ROOTS);
        final List<String> lastRootHistory = getListPropertyNotNull(LAST_ROOTS_HISTORY);
        for (int i = 0; i < lastOpened.size(); i++) {
            final RecentFile recentFile = new RecentFile(lastOpened.get(i));
            if (lastLocation.size() == lastOpened.size())
                recentFile.lastVisitedNodeId = lastLocation.get(i);
            if (lastRoot.size() == lastOpened.size())
                recentFile.lastRootNodeId = lastRoot.get(i);
            if (lastRootHistory.size() == lastOpened.size())
                recentFile.lastRootHistoryNodeIds = ConfigurationUtils.decodeListValue(lastRootHistory.get(i), false);
            lastOpenedList.add(recentFile);
        }
        if (!lastOpenedList.isEmpty()) {
            mapSelectedOnStart = lastOpenedList.get(0);
        }
    }

    private List<String> getListPropertyNotNull(String key) {
        final String lastOpened = ResourceController.getResourceController().getProperty(key, "");
        return ConfigurationUtils.decodeListValue(lastOpened, true);
    }

	public void safeOpen(final RecentFile recentFile) {
		final boolean remove = RecentFileOpening.shouldRemoveFromList(() -> open(recentFile),
		    exception -> askWhatToDoWithUnopenedFile(recentFile, exception));
		if (remove) {
			lastOpenedList.remove(recentFile);
			updateMenus();
		}
	}

	private RecentFileOpening.Choice askWhatToDoWithUnopenedFile(final RecentFile recentFile, final Exception exception) {
		LogUtils.warn(exception);
		final String message = TextUtils.format("recent_file_open_failed", recentFile.restorable);
		UITools.showFrame();
		final Component frame = UITools.getMenuComponent();
		JTextArea messageArea = new JTextArea(message);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setColumns(Math.min(80, message.length() + 5));
		messageArea.setEditable(false);
		messageArea.setSize(messageArea.getPreferredSize());
		final Object[] options = {
		        TextUtils.getText("recent_file_open_failed.retry"),
		        TextUtils.getText("recent_file_open_failed.remove"),
		        TextUtils.getText("recent_file_open_failed.keep")};
		final int selected = JOptionPane.showOptionDialog(frame, messageArea, "Freeplane",
		    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return RecentFileOpening.Choice.ofOptionIndex(selected);
	}

	public void saveProperties() {
	    updateLastVisitedNodeId(Controller.getCurrentController().getMapViewManager().getMapViewComponent());
	    ResourceController.getResourceController().setProperty(LAST_OPENED,
		    ConfigurationUtils.encodeListValue(getRestoreables(), true));
	    ResourceController.getResourceController().setProperty(LAST_LOCATIONS,
		        ConfigurationUtils.encodeListValue(getLastVisitedNodeIds(), true));
	    ResourceController.getResourceController().setProperty(LAST_ROOTS,
		        ConfigurationUtils.encodeListValue(getLastRootNodeIds(), true));
	    ResourceController.getResourceController().setProperty(LAST_ROOTS_HISTORY,
		        ConfigurationUtils.encodeListValue(getLastRootHistoryNodeIds(), true));
	    ResourceController.getResourceController().setProperty(LAST_MODE,
	            Controller.getCurrentController().getModeController().getModeName());
	}

	private void updateLastVisitedNodeId(final Component mapViewComponent) {
		if (!(mapViewComponent instanceof MapView))
			return;
		MapView mapView = (MapView) mapViewComponent;
        if (!mapView.getModeController().getModeName().equals(MModeController.MODENAME))
		    return;
		final NodeView selected = mapView.getSelected();
		final RecentFile recentFile = findRecentFileByMapModel(getMapModel(mapView));
		if (selected != null && recentFile != null) {
			NodeModel selectedNode = selected.getNode();
			recentFile.lastVisitedNodeId = selectedNode.getID();
			recentFile.lastRootNodeId = mapView.getRoot().getNode().getID();
			recentFile.lastRootHistoryNodeIds = mapView.getRootsHistoryNodeIds();
		}
	}

	private RecentFile findRecentFileByRestorable(String restorable) {
		for (RecentFile recentFile : lastOpenedList) {
			if (recentFile.restorable.equals(restorable))
				return recentFile;
		}
		return null;
	}

	private RecentFile findRecentFileByMapModel(final MapModel map) {
		return findRecentFileByRestorable(getRestoreable(map));
	}

    private List<String> getRestoreables() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
            result.add(recentFile.restorable);
        }
		return result;
	}

	private List<String> getLastVisitedNodeIds() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
	        result.add(recentFile.lastVisitedNodeId);
	    }
	    return result;
	}
	private List<String> getLastRootNodeIds() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
	        result.add(recentFile.lastRootNodeId);
	    }
	    return result;
	}

	private List<String> getLastRootHistoryNodeIds() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
	        result.add(ConfigurationUtils.encodeListValue(recentFile.lastRootHistoryNodeIds, false));
	    }
	    return result;
	}

    private boolean tryToChangeToMapView(URL url) {
		try {
			return Controller.getCurrentController().getMapViewManager().tryToChangeToMapView(url);
		} catch (MalformedURLException e) {
			LogUtils.warn(e);
			return false;
		}
	}

	private void restoreRootsHistory(RecentFile recentFile) {
		Component mapViewComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		if(mapViewComponent instanceof MapView) {
			((MapView) mapViewComponent).setRootsHistoryNodeIds(recentFile.lastRootHistoryNodeIds);
		}
	}

	private void updateList(final MapModel map, final String restoreString) {
		//ignore documentation maps loaded using documentation actions
		if(map.containsExtension(DocuMapAttribute.class))
			return;
		if (restoreString != null) {
			RecentFile recentFile = findRecentFileByRestorable(restoreString);
			if (recentFile != null) {
				lastOpenedList.remove(recentFile);
				lastOpenedList.add(0, recentFile);
			}
			else {
				lastOpenedList.add(0, new RecentFile(restoreString));
			}
		}
		updateMenus();
	}

	private void updateMenus() {
		final IUserInputListenerFactory userInputListenerFactory = Controller.getCurrentModeController()
		    .getUserInputListenerFactory();
		userInputListenerFactory.rebuildMenus("lastOpenedMaps");
	}

	private void updateMenus(ModeController modeController, Entry target) {
		List<AFreeplaneAction> openMapActions = createOpenLastMapActionList();
		for (AFreeplaneAction openMapAction : openMapActions) {
			modeController.addActionIfNotAlreadySet(openMapAction);
			new EntryAccessor().addChildAction(target, openMapAction);
		}
	}

	public List<AFreeplaneAction> createOpenLastMapActionList() {
		Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
	    int i = 0;
	    int maxEntries = getMaxMenuEntries();
	    List<AFreeplaneAction> openMapActions = new ArrayList<AFreeplaneAction>(maxEntries);
	    for (final RecentFile recentFile : lastOpenedList) {
	    	if (i == 0
	    	        && (!modeController.getModeName().equals(MModeController.MODENAME) || controller.getMap() == null || controller
	    	            .getMap().getURL() == null)) {
	    		i++;
	    		maxEntries++;
	    	}
	    	if (i == maxEntries) {
	    		break;
	    	}

			final AFreeplaneAction openMapAction = new OpenLastOpenedAction(i++, this, recentFile);
			createOpenMapItemName(openMapAction, recentFile.restorable);
	    	openMapActions.add(openMapAction);
	    }
	    return openMapActions;
    }

	private void createOpenMapItemName(AFreeplaneAction openMapAction, final String restorable) {
		final int separatorIndex = restorable.indexOf(':');
		String key = restorable.substring(0, separatorIndex);
		String filePath = restorable.substring(separatorIndex);
		String keyName = TextUtils.getText("open_as" + key, key);
		openMapAction.putValue(Action.SHORT_DESCRIPTION, keyName);
		openMapAction.putValue(Action.DEFAULT, filePath);
		if(filePath.startsWith("::"))
			filePath = filePath.substring(2);
		else
			filePath = filePath.substring(1);
		final int fileSeparatorIndex = filePath.lastIndexOf('/');
		String actionName;
		if(fileSeparatorIndex == -1) {
			actionName = filePath;
		}
		else {
			String fileName = filePath.substring(fileSeparatorIndex + 1);
			String folderPath = filePath.substring(0, fileSeparatorIndex);
			actionName = fileName + " (" + folderPath + ")";
		}
		openMapAction.putValue(Action.NAME, TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(actionName));

    }

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}
}
