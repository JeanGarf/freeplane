package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Container;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;

public class EntryPopupMenuFactory {
	private static final String SEPARATOR_BUILDER = "separator";

	private final EntryAccessor entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());

	public static Entry findByName(Entry top, String name) {
		if (name.equals(top.getName()))
			return top;
		for (final Entry child : top.children()) {
			final Entry found = findByName(child, name);
			if (found != null)
				return found;
		}
		return null;
	}

	public JPopupMenu createPopupMenu(Entry entry) {
		final JPopupMenu popupMenu = new JPopupMenu();
		addChildren(entry, popupMenu);
		return popupMenu;
	}

	private void addChildren(Entry parent, Container container) {
		for (final Entry child : parent.children())
			addEntry(child, container);
	}

	private void addEntry(Entry entry, Container container) {
		if (Boolean.FALSE.equals(entry.getAttribute("allowed")))
			return;
		if (entry.builders().contains(SEPARATOR_BUILDER)) {
			addSeparator(container);
			return;
		}
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		if (entry.hasChildren()) {
			final JMenu submenu = new JMenu(entryAccessor.getText(entry).replace("&", ""));
			addChildren(entry, submenu);
			container.add(submenu);
		}
		else if (action != null) {
			final JMenuItem item = new JMenuItem(action);
			final ActionAcceleratorManager accelerators = ResourceController.getResourceController()
			    .getAcceleratorManager();
			item.setAccelerator(accelerators.getAccelerator(action));
			container.add(item);
		}
	}

	private void addSeparator(Container container) {
		if (container instanceof JPopupMenu)
			((JPopupMenu) container).addSeparator();
		else if (container instanceof JMenu)
			((JMenu) container).addSeparator();
	}
}
