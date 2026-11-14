package org.freeplane.features.styles.mindmapmode;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.freeplane.api.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.TemplateManager;
import org.freeplane.view.swing.features.filepreview.MindMapPreviewWithOptions;

import com.jgoodies.forms.builder.FormBuilder;

public class ManageAssociatedMindMapsDialog{
    private final MapModel map;
    private final JDialog dialog;
    private URI followedMapLocation;
    private URI associatedMapLocation;
    
    private final JButton[] followedMapButtons;
    private final JButton[] associatedMapButtons;
    
    static String titleOf(String actionName) {
        return actionName.replaceAll("\\s*(\\u2026|\\.\\.\\.)\\s*$", "");
    }

    public ManageAssociatedMindMapsDialog(String title, final MapModel map){
        this.map = map;
        Frame owner = UITools.getCurrentFrame();
        dialog = new JDialog(owner);
        FormBuilder formBuilder = FormBuilder.create().columns("p, 3dlu, fill:3dlu:grow, 3dlu, p")
                .rows("p, 3dlu, p, 24dlu, p, 3dlu, p, 3dlu, p");
        formBuilder.add(TextUtils.getText("followed_map")).xy(1,  1);
        JTextField followedMapField = createReadOnlyField();
        formBuilder.add(followedMapField).xy(3, 1);
        JButton changeFollowedMapButton = createChangeMapButton(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, "select_followed_map", followedMapField);
        formBuilder.add(changeFollowedMapButton).xy(5, 1);
        JButton openFollowedMapButton = createOpenButton(() -> followedMapLocation);
        JButton copyStylesFromFollowedMapButton = createCopyStylesButton(() -> followedMapLocation);
        JButton replaceStylesFromFollowedMapButton = createReplaceStylesButton(() -> followedMapLocation);
        JButton unfolowButton = createUnfollowButton(followedMapField);
        followedMapButtons = new JButton[] {openFollowedMapButton, copyStylesFromFollowedMapButton, replaceStylesFromFollowedMapButton, unfolowButton};
        formBuilder.addBar(openFollowedMapButton, copyStylesFromFollowedMapButton, 
                replaceStylesFromFollowedMapButton, unfolowButton).xyw(1, 3, 5);
        
        formBuilder.add(TextUtils.getText("associated_template")).xy(1,  5);
        JTextField associatedMapField = createReadOnlyField();
        formBuilder.add(associatedMapField).xy(3, 5);
        JButton changeAssosiatedMapButton = createChangeMapButton(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, "select_associated_template", associatedMapField);
        formBuilder.add(changeAssosiatedMapButton).xy(5, 5);
        JButton openAssociatedMapButton = createOpenButton(() -> associatedMapLocation);
        JButton copyStylesFromAssociatedMapButton = createCopyStylesButton(() -> associatedMapLocation);
        JButton replaceStylesFromAssociatedMapButton = createReplaceStylesButton(() -> associatedMapLocation);
        associatedMapButtons = new JButton[] {openAssociatedMapButton, copyStylesFromAssociatedMapButton, replaceStylesFromAssociatedMapButton};
        formBuilder.addBar(openAssociatedMapButton, copyStylesFromAssociatedMapButton, 
                replaceStylesFromAssociatedMapButton).xyw(1, 7, 5);
        
        JButton closeButton = createCloseMapButton();
        formBuilder.add(closeButton).xyw(1, 9, 5, "r,c");

        updateAssociatedMap(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, followedMapField, TextUtils.getText("no_map_followed"));      
        updateAssociatedMap(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, associatedMapField, TextUtils.getText("no_template_associated"));      

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                closeButton.requestFocusInWindow();
            }
        });
        UITools.addEscapeActionToDialog(dialog, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        dialog.setModal(true);
        dialog.setTitle(titleOf(title));
        dialog.getContentPane().add(formBuilder.build());
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }
    private static JTextField createReadOnlyField() {
        final JTextField field = new JTextField(60);
        field.setEditable(false);
        return field;
    }

    private static void showText(JTextComponent field, String text) {
        showText(field, text, text);
    }

    private static void showText(JTextComponent field, String text, String tooltip) {
        field.setText(text);
        field.setCaretPosition(0);
        field.setToolTipText(tooltip);
    }

    private JButton createUnfollowButton(JTextComponent followedMapField) {
        JButton unfolowButton = new JButton(TextUtils.getText("unfollow"));
        unfolowButton.setEnabled(followedMapLocation != null);
        unfolowButton.addActionListener(e -> {
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, null);
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_MAP_LAST_TIME, null);
            showText(followedMapField, TextUtils.getText("no_map_followed"));
            enableButtons(followedMapButtons, false);
        });
        return unfolowButton;
    }
    private JButton createCloseMapButton() {
        JButton closeButton = new JButton(TextUtils.getText("close"));
        closeButton.addActionListener(e -> {
            dialog.setVisible(false);
        });
        return closeButton;
    }
    private JButton createReplaceStylesButton(Supplier<URI>  sourceLocation) {
        JButton replaceStylesFromFollowedMapButton = new JButton(TextUtils.getText("replaceStyles"));
        replaceStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().replaceStyles(sourceLocation.get(), map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return replaceStylesFromFollowedMapButton;
    }

    private JButton createCopyStylesButton(Supplier<URI>  sourceLocation) {
        JButton copyStylesFromFollowedMapButton = new JButton(TextUtils.getText("copyStyles"));
        copyStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().copyStyles(sourceLocation.get(), map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return copyStylesFromFollowedMapButton;
    }

    private JButton createOpenButton(Supplier<URI> mapLocation) {
        JButton openFollowedMapButton = new JButton(TextUtils.getText("open"));
        openFollowedMapButton.addActionListener(e -> {
            UrlManager.getController().loadHyperlink(new Hyperlink(mapLocation.get()));
            dialog.setVisible(false);
        });
        return openFollowedMapButton;
    }

    private JButton createChangeMapButton(String mapProperty, String fileChooserTitleProperty, JTextComponent field) {
        JButton assignMapButton = new JButton(TextUtils.getText("browse"));
        assignMapButton.addActionListener(e -> {
            MindMapPreviewWithOptions previewWithOptions = MindMapPreviewWithOptions.createFileOpenDialogAndOptions(
                    TextUtils.getText(fileChooserTitleProperty)
            );
            JFileChooser fileChooser = previewWithOptions.getFileChooser();
            final int returnVal = fileChooser.showOpenDialog(UITools.getCurrentFrame());
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = fileChooser.getSelectedFile();
            if(! file.exists()){
                return;
            }
            MapStyle.getController().setProperty(map, mapProperty,
                    TemplateManager.INSTANCE.normalizeTemplateLocation(file.toURI()).toString());
            updateAssociatedMap(mapProperty, field, TextUtils.getText("no_map_followed"));
        });
        return assignMapButton;
    }

    private void updateAssociatedMap(String propertyName, JTextComponent mapField, String noMapMessage) {
        String followedMap = MapStyle.getController().getProperty(map, propertyName);
        mapField.setEditable(false);
        URI uri = null;
        if(followedMap != null) {
            try {
                uri = new URI(followedMap);
                String message = TemplateManager.INSTANCE.describeNormalizedLocation(uri);
                showText(mapField, LocationNames.fileNameOf(message), message);
                uri = TemplateManager.INSTANCE.expandTemplateLocation(uri);
            } catch (URISyntaxException e) {
                LogUtils.severe(e);               
            }
        } 
        else {
            showText(mapField, noMapMessage);
        }
        if(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY.equals(propertyName)) {
            followedMapLocation = uri;
            enableButtons(followedMapButtons, uri != null);
        }
        else if(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY.equals(propertyName)) {
            associatedMapLocation = uri;
            enableButtons(associatedMapButtons, uri != null);
        }
    }
    
    
    private void enableButtons(JButton[] buttons, boolean enabled) {
        Stream.of(buttons).forEach(button -> button.setEnabled(enabled));
    }
    
    public void show() {
        dialog.setVisible(true);
    }
    
}