/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2020 Felix Natter
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
package org.freeplane.features.commandsearch;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class CommandSearchAction extends AFreeplaneAction {
    private static final long serialVersionUID = 1L;
    public static final String KEY = "CommandSearchAction";
    private CommandSearchDialog commandSearchDialog;
    private boolean searchesPreferencesOnlyInDialog;
    private DefaultComboBoxModel<String> sharedSearchHistory;

    @SuppressWarnings("serial")
	public CommandSearchAction()
    {
        super(KEY);
        sharedSearchHistory = new DefaultComboBoxModel<String>() {
        	private boolean removalInProgress = false;
			@Override
			public void removeElementAt(int index) {
				boolean removalInProgress = this.removalInProgress;
				this.removalInProgress = true;
				try {
					super.removeElementAt(index);
				}
				finally {
					this.removalInProgress = removalInProgress;
				}
			}
			@Override
			public void setSelectedItem(Object anObject) {
				if(! removalInProgress)
					super.setSelectedItem(anObject);
			}

        };
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        showOwnedBy(UITools.getCurrentFrame());
    }

    public void showOwnedBy(Window owner) {
        show(owner, false);
    }

    public void showForPreferences(Window owner) {
        show(owner, true);
    }

    private void show(Window owner, boolean searchesPreferencesOnly) {
        if(commandSearchDialog != null && (commandSearchDialog.getOwner() != owner || searchesPreferencesOnly != searchesPreferencesOnlyInDialog)) {
            commandSearchDialog.dispose();
            commandSearchDialog = null;
        }
        if(commandSearchDialog == null)
            createDialog(owner, searchesPreferencesOnly);
        else
            commandSearchDialog.toFront();
    }

    private void createDialog(Window owner, boolean searchesPreferencesOnly) {
        final CommandSearchStateOverride override = searchesPreferencesOnly ? CommandSearchStateOverride.forPreferencesOnly() : null;
        final CommandSearchDialog dialog = new CommandSearchDialog(owner, sharedSearchHistory);
        commandSearchDialog = dialog;
        searchesPreferencesOnlyInDialog = searchesPreferencesOnly;
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if(override != null)
                    override.restore();
                if(commandSearchDialog == dialog)
                    commandSearchDialog = null;
            }
        });

    }
}
