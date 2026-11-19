package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;

public class JButtonWithDropdownMenu extends JButton {
	private static final long serialVersionUID = 1L;
	private JPopupMenu menu;

	public JButtonWithDropdownMenu(String text, Icon icon) {
		super(text, icon);
		menu = new JPopupMenu();
		setHorizontalAlignment(SwingConstants.CENTER);
		addActionListener(e -> menu.show(this, 0, this.getHeight()));
	}

	public void addMenuSeparator() {
		menu.addSeparator();
	}

	public JMenuItem addMenuAction(Action a) {
		if (a instanceof AFreeplaneAction && ((AFreeplaneAction) a).isSelectable())
			return menu.add(new JAutoCheckBoxMenuItem((AFreeplaneAction) a));
		return menu.add(a);
	}

}
