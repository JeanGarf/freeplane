/*
 *  Freeplane - mind map editor
 *
 *  Copyright (C) 2020 Felix Natter, Dimitry Polivaev
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

import static org.freeplane.features.commandsearch.SearchItem.normalizeText;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Event;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.AbstractAction;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardAccessor;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;


public class CommandSearchDialog extends JDialog
    implements DocumentListener, ListCellRenderer<SearchItem>, IMapSelectionListener, INodeSelectionListener {
	private static final long serialVersionUID = 1L;

	private static final String LIMIT_EXCEEDED_MESSAGE = TextUtils.getText("cmdsearch.limit_exceeded");
    private static final Icon WARNING_ICON = ResourceController.getResourceController().getIcon("/images/icons/messagebox_warning.svg");
    private static final int LIMIT_EXCEEDED_RANK = 100;
    private static final String WINDOW_CONFIG_PROPERTY = "cmdsearch_window_configuration";

    private static class SingleSelectionList extends JList<SearchItem> {
        private static final long serialVersionUID = 1L;

        @Override
        public void removeSelectionInterval(int index0, int index1) {
            //ignore
        }

        @Override
        public void addSelectionInterval(int anchor, int lead) {
            setSelectionInterval(anchor, lead);
        }
    }

    private static class UpdateableListModel<E> extends AbstractListModel<E> {
    	private final List<E> listData;

        public UpdateableListModel(List<E> listData) {
			this.listData = listData;
		}
		private static final long serialVersionUID = 1L;
        @Override
        public int getSize() { return listData.size(); }
        @Override
        public E getElementAt(int i) { return listData.get(i); }

        @Override
        public void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1);
        }

    }

    public enum Scope{
        MENUS, PREFERENCES, ICONS;

		String propertyName() {
			return "cmdsearch_scope_" + name();
		}

		String labelName() {
			return "cmdsearch.scope." + name();
		}

		boolean isEnabled() {
			return ResourceController.getResourceController().getBooleanProperty(propertyName(), true);
		}

		void setEnabled(boolean selected) {
			ResourceController.getResourceController().setProperty(propertyName(), selected);
		}
    }

    private final JCheckBox searchMenus;
    private final JCheckBox searchPrefs;
    private final JCheckBox searchIcons;
    private final JComboBox<String> inputWithHistory;
    private final JTextField input;
    private final JList<SearchItem> resultList;
    private final JCheckBox closeAfterExecute;
    private final JCheckBox searchWholeWords;

    private final PreferencesIndexer preferencesIndexer;
    private final MenuStructureIndexer menuStructureIndexer;
    private final IconIndexer iconIndexer;

    private final Controller controller;

    private final ModeController modeController;

    @SuppressWarnings("serial")
	CommandSearchDialog(Window parent, DefaultComboBoxModel<String> sharedSearchHistory)
    {
        super(parent, TextUtils.getText("CommandSearchAction.text"), java.awt.Dialog.ModalityType.MODELESS);

        controller = Controller.getCurrentController();
        modeController = Controller.getCurrentModeController();
        controller.getMapViewManager().addMapSelectionListener(this);
        modeController.getMapController().addNodeSelectionListener(this);

        setLocationRelativeTo(parent);

        preferencesIndexer = new PreferencesIndexer();
        menuStructureIndexer = new MenuStructureIndexer();
        iconIndexer = new IconIndexer();

        Handler handler = new Handler();

        inputWithHistory = new JComboBox<String>() {

			@Override
			public Object getSelectedItem() {
				return input != null && ! isPopupVisible() ? input.getText() : super.getSelectedItem();
			}
        };
        inputWithHistory.setEditable(true);
        inputWithHistory.setModel(sharedSearchHistory);


        // Override the editor to provide custom copy functionality
        inputWithHistory.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField textField = new JTextField("") {
                    @Override
                    public void copy() {
                        if(getSelectionStart() < getSelectionEnd())
                            super.copy();
                        else {
                            copySelectedItemToClipboard();
                        }
                    }
                };
                textField.setBorder(null);
                return textField;
            }
        });

        // Get reference to the actual editor component
        input = (JTextField) inputWithHistory.getEditor().getEditorComponent();
        input.setColumns(40);
        input.addKeyListener(handler);

        reconfigureComboBoxInputMapsToRespondOnlyToShiftKeys();

        resultList = new SingleSelectionList();
        resultList.setModel(new UpdateableListModel<SearchItem>(Collections.emptyList()));
        resultList.setFocusable(false);
        resultList.setCellRenderer(this);
        resultList.addMouseListener(handler);
        resultList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addKeyListener(handler);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane resultListScrollPane = new JScrollPane(resultList);
		UITools.setScrollbarIncrement(resultListScrollPane);
        getContentPane().add(panel);

        JPanel scopePanel = new JPanel();
        searchPrefs = createScopeButton(Scope.PREFERENCES);
        searchMenus = createScopeButton(Scope.MENUS);
        searchMenus.setToolTipText(TextUtils.getRawText(Scope.MENUS.labelName() + ".tooltip"));
        searchIcons = createScopeButton(Scope.ICONS);
        scopePanel.add(searchPrefs);
        scopePanel.add(searchMenus);
        scopePanel.add(searchIcons);
        searchWholeWords = new JCheckBox();
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchWholeWords, TextUtils.getRawText("cmdsearch.searchWholeWords"));
        searchWholeWords.setSelected(ResourceController.getResourceController().getBooleanProperty("cmdsearch_whole_words"));
        searchWholeWords.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ResourceController.getResourceController().setProperty("cmdsearch_whole_words", searchWholeWords.isSelected());
                updateMatches();
                input.requestFocusInWindow();
            }
        });

        Box whatbox = Box.createVerticalBox();
        scopePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        whatbox.add(scopePanel);
        searchWholeWords.setAlignmentX(Component.CENTER_ALIGNMENT);
        whatbox.add(searchWholeWords);
        inputWithHistory.setAlignmentX(Component.CENTER_ALIGNMENT);
        whatbox.add(inputWithHistory);
        initScopeFromPrefs();

        panel.add(whatbox, BorderLayout.NORTH);
        panel.add(resultListScrollPane, BorderLayout.CENTER);

        Box optionsBox = Box.createVerticalBox();
        closeAfterExecute = new JCheckBox();
        LabelAndMnemonicSetter.setLabelAndMnemonic(closeAfterExecute, TextUtils.getRawText("cmdsearch.closeAfterExecute"));
        closeAfterExecute.setSelected(ResourceController.getResourceController().getBooleanProperty("cmdsearch_close_after_execute"));
        closeAfterExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ResourceController.getResourceController().setProperty("cmdsearch_close_after_execute", closeAfterExecute.isSelected());
                updateMatches();
                input.requestFocusInWindow();
            }
        });
        optionsBox.add(closeAfterExecute);

        panel.add(optionsBox, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        input.setColumns(40);
        resultList.setVisibleRowCount(20);

        final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(WINDOW_CONFIG_PROPERTY);
        windowConfigurationStorage.setBounds(this);
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MapController mapController = modeController.getMapController();
                mapController.removeNodeSelectionListener(CommandSearchDialog.this);
                controller.getMapViewManager().removeMapSelectionListener(CommandSearchDialog.this);
            }
        });

        input.getDocument().addDocumentListener(this);
        this.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				input.requestFocusInWindow();
				removeWindowFocusListener(this);
			}
		});

        setVisible(true);
        updateMatches();
    }

    private void reconfigureComboBoxInputMapsToRespondOnlyToShiftKeys() {
        JComponent[] components = {inputWithHistory, input};
        for(JComponent component : components) {
            int[] maps = {JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT};
            for(int map : maps) {
                moveActionToShift(component, map, KeyEvent.VK_DOWN);
                moveActionToShift(component, map, KeyEvent.VK_UP);
                moveActionToShift(component, map, KeyEvent.VK_ENTER);
            }
        }
    }

    private void moveActionToShift(JComponent component, int mapIndex, int keyCode) {
        final KeyStroke originalKeyStroke = KeyStroke.getKeyStroke(keyCode, 0);
        InputMap map = component.getInputMap(mapIndex);
		Object actionKey = map.get(originalKeyStroke);
        if (actionKey != null) {
            final KeyStroke replacement = KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK);
			map.put(replacement, actionKey);
			final ActionMap actionMap = component.getActionMap();
			final Action action = actionMap.get(actionKey);
			if(action != null) {
				@SuppressWarnings("serial")
				Action replacementAction = new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e) {
						if((e.getModifiers() & Event.SHIFT_MASK) != 0 || inputWithHistory.isPopupVisible()
								|| resultList.getModel().getSize() == 0)
							action.actionPerformed(e);
					}
				};
				map.put(originalKeyStroke, replacementAction);
				actionMap.put(replacementAction, replacementAction);
			}
        }
    }

    @Override
    public void afterMapChange(MapModel oldMap, MapModel newMap) {
        if(Controller.getCurrentModeController() != modeController) {
            dispose();
        }
    }

    @Override
    public void onSelect(NodeModel node) {
        SwingUtilities.invokeLater(this::updateResultList);
    }

    private JCheckBox createScopeButton(Scope scope) {
    	JCheckBox searchPrefs = new JCheckBox();
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchPrefs, TextUtils.getRawText(scope.labelName()));
        searchPrefs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = searchPrefs.isSelected();
				scope.setEnabled(selected);
                updateMatches();
                input.requestFocusInWindow();
            }
        });
        return searchPrefs;
    }


    private void initScopeFromPrefs()
    {
        searchMenus.setSelected(Scope.MENUS.isEnabled());
        searchPrefs.setSelected(Scope.PREFERENCES.isEnabled());
        searchIcons.setSelected(Scope.ICONS.isEnabled());
     }

	@Override
	public void changedUpdate(DocumentEvent e) {
        updateMatches();
    }
    @Override
	public void removeUpdate(DocumentEvent e) {
        updateMatches();
    }
    @Override
	public void insertUpdate(DocumentEvent e) {
        updateMatches();
    }

    private void addTextToSearchHistory() {
    	String searchText = input.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String trimmedText = searchText.trim();
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) inputWithHistory.getModel();


            int index = model.getIndexOf(trimmedText);
            if(index == 0)
            	return;
            model.insertElementAt(trimmedText, 0);
			if ( index != -1 ) {
			    model.removeElementAt(index + 1);
			    return;
			}
            while (model.getSize() > 20) {
                model.removeElementAt(model.getSize() - 1);
            }
        }
    }

    private void updateMatches()
    {
    	final String searchInput = input.getText();
        String trimmedInput = searchInput.trim();
        boolean shouldSearchWholeWords =  ResourceController.getResourceController().getBooleanProperty("cmdsearch_whole_words");
        if(trimmedInput.length() >= 1
        		&& (
        				searchInput.endsWith(" ")
        				|| shouldSearchWholeWords)
        		|| (searchInput.length() >= 3 && searchInput.codePoints().limit(3).count() == 3)
        		|| ! searchInput.codePoints().allMatch(Character::isAlphabetic)
        		) {

        	//PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();
        	List<SearchItem> matches = new ArrayList<>();
        	ItemChecker textChecker = new ItemChecker(shouldSearchWholeWords);

        	final String[] searchTerms =  normalizeText(trimmedInput).split("\\s+");
        	if (searchMenus.isSelected())
        	{
        		textChecker.findMatchingItems(menuStructureIndexer.getMenuItems(), searchTerms, matches::add);
        	}
        	if (searchPrefs.isSelected())
        	{
        		textChecker.findMatchingItems(preferencesIndexer.getPrefs(), searchTerms, matches::add);
        	}
        	if (searchIcons.isSelected())
        	{
        		textChecker.findMatchingItems(iconIndexer.getIconItems(), searchTerms, matches::add);
        	}

        	Collections.sort(matches);
        	int itemLimit = ResourceController.getResourceController().getIntProperty("cmdsearch_item_limit");
        	if(matches.size() > itemLimit) {
        		matches = matches.subList(0, itemLimit);
        		matches.add(new InformationItem(LIMIT_EXCEEDED_MESSAGE, WARNING_ICON, LIMIT_EXCEEDED_RANK));
        	}
        	UpdateableListModel<SearchItem> model = new UpdateableListModel<>(matches);

        	SearchItem selectedItem = resultList.getSelectedValue();
        	resultList.setModel(model);
        	if (resultList.getModel().getSize() > 0) {
        		resultList.setSelectedValue(selectedItem, true);
        		if(resultList.getSelectedIndex() == -1)
        			resultList.setSelectedIndex(0);
        	}
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SearchItem> list, SearchItem item, int index, boolean isSelected, boolean cellHasFocus) {

        String text = item.getDisplayedText();
        Icon icon = item.getTypeIcon();
        String tooltip = item.getTooltip();

        JLabel label = (JLabel)(new DefaultListCellRenderer().getListCellRendererComponent(list, text, index, isSelected, cellHasFocus));
        if (icon != null)
        {
            label.setIcon(icon);
        }
        if (tooltip != null)
        {
            label.setToolTipText(tooltip);
        }
        return label;
    }

    protected boolean shouldAssignAccelerator(InputEvent event) {
        return event.isControlDown();
    }

    private void executeItem(InputEvent event, int index)
    {
        ListModel<SearchItem> data = resultList.getModel();
        SearchItem item = (data.getElementAt(index));

        addTextToSearchHistory();

        if(shouldAssignAccelerator(event)) {
            item.assignNewAccelerator();
        }
        else {

            item.execute(event);

            if (closeAfterExecute.isSelected())
            {
                dispose();
            }
        }

        if (item.shouldUpdateResultList())
            updateResultList();
    }

    private void updateResultList() {
        UpdateableListModel<SearchItem> model = (UpdateableListModel<SearchItem>) resultList.getModel();
        int lastElementIndex = model.getSize() - 1;
        if(lastElementIndex >= 0)
            model.fireContentsChanged(this, 0, lastElementIndex);
    }

    private void copySelectedItemToClipboard() {
        SearchItem item = resultList.getSelectedValue();
        if(item != null) {
            String text = item.getCopiedText();
            ClipboardAccessor.getInstance().setClipboardContents(text);
        }
    }

	class Handler implements MouseListener, KeyListener {

    	@Override
    	public void mouseClicked(MouseEvent e) {
    		if (e.getClickCount() == 2)
    		{
    			int index = resultList.locationToIndex(e.getPoint());
    			executeItem(e, index);
    			return;
    		}
    	}

    	@Override
    	public void mousePressed(MouseEvent e) {
    	}

    	@Override
    	public void mouseReleased(MouseEvent e) {
    	}

    	@Override
    	public void mouseEntered(MouseEvent e) {
    	}

    	@Override
    	public void mouseExited(MouseEvent e) {
    	}

    	@Override
    	public void keyTyped(KeyEvent e) {
    	}

    	@Override
    	public void keyReleased(KeyEvent e) {
    	}

    	@Override
    	public void keyPressed(KeyEvent e) {


    		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
    		{
    			if(inputWithHistory.isPopupVisible())
    				inputWithHistory.hidePopup();
    			else
    				dispose();
    		} else if(! inputWithHistory.isPopupVisible()){

    			final boolean wrapAround = false;
    			if (e.getKeyCode() == KeyEvent.VK_DOWN && e.getSource() == input)
    			{
    				if (resultList.getModel().getSize() > 0) {
    					int selectedIndex = resultList.getSelectedIndex();
    					int newIndex = selectedIndex + 1;
    					if (newIndex >= resultList.getModel().getSize())
    					{
    						newIndex = wrapAround ? (0) : (resultList.getModel().getSize() - 1);
    					}
    					resultList.setSelectedIndex(newIndex);
    					resultList.ensureIndexIsVisible(newIndex);
    				}
    			}
    			else if (e.getKeyCode() == KeyEvent.VK_UP && e.getSource() == input)
    			{
    				if (resultList.getModel().getSize() > 0) {
    					int selectedIndex = resultList.getSelectedIndex();
    					if (selectedIndex == -1)
    					{
    						resultList.setSelectedIndex(0);
    					}
    					else if (selectedIndex == 0 && wrapAround)
    					{
    						resultList.setSelectedIndex(resultList.getModel().getSize() - 1);
    					}
    					else
    					{
    						resultList.setSelectedIndex(selectedIndex - 1);
    					}
    					resultList.ensureIndexIsVisible(resultList.getSelectedIndex());
    				}
    			}
    			else if (e.getKeyCode() == KeyEvent.VK_ENTER)
    			{
    				if (resultList.getSelectedIndex() >= 0)
    				{
    					executeItem(e, resultList.getSelectedIndex());
    				}
    			}
    		}
    	}
    }
}
