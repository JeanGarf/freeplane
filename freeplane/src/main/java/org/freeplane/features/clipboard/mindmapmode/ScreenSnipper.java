package org.freeplane.features.clipboard.mindmapmode;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.freeplane.core.util.LogUtils;

class ScreenSnipper {
	private static final int DELAY_BEFORE_CAPTURE_MS = 350;
	private static final Color DIMMING = new Color(0, 0, 0, 110);
	private static final Color BORDER = new Color(0, 120, 215);

	private final Consumer<BufferedImage> onCaptured;
	private final List<Window> hiddenWindows = new ArrayList<>();

	static void start(Consumer<BufferedImage> onCaptured) {
		new ScreenSnipper(onCaptured).hideWindowsThenCapture();
	}

	private ScreenSnipper(Consumer<BufferedImage> onCaptured) {
		this.onCaptured = onCaptured;
	}

	private void hideWindowsThenCapture() {
		for (final Window window : Window.getWindows())
			if (window.isShowing()) {
				hiddenWindows.add(window);
				window.setVisible(false);
			}
		final Timer timer = new Timer(DELAY_BEFORE_CAPTURE_MS, e -> captureAndSelect());
		timer.setRepeats(false);
		timer.start();
	}

	private void captureAndSelect() {
		final Rectangle area = virtualScreen();
		final BufferedImage screen;
		try {
			screen = new Robot().createScreenCapture(area);
		}
		catch (AWTException | SecurityException e) {
			LogUtils.warn(e);
			restoreWindows();
			return;
		}
		final SelectionWindow window = new SelectionWindow(screen, area);
		window.setVisible(true);
		window.toFront();
		window.takeFocus();
	}

	private static Rectangle virtualScreen() {
		final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		final Rectangle[] bounds = new Rectangle[devices.length];
		for (int i = 0; i < devices.length; i++)
			bounds[i] = devices[i].getDefaultConfiguration().getBounds();
		return SnipSelection.unionOf(bounds);
	}

	private void restoreWindows() {
		for (final Window window : hiddenWindows)
			window.setVisible(true);
		if (!hiddenWindows.isEmpty())
			hiddenWindows.get(0).toFront();
	}

	private void finish(BufferedImage screen, Rectangle selection) {
		restoreWindows();
		if (SnipSelection.isUsable(selection))
			onCaptured.accept(screen.getSubimage(selection.x, selection.y, selection.width, selection.height));
	}

	private class SelectionWindow extends JWindow {
		private static final long serialVersionUID = 1L;
		private final BufferedImage screen;
		private final Rectangle area;
		private Point start;
		private Rectangle selection;

		SelectionWindow(BufferedImage screen, Rectangle area) {
			this.screen = screen;
			this.area = area;
			setBounds(area);
			setAlwaysOnTop(true);
			final JComponent surface = new JComponent() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics graphics) {
					paintSurface((Graphics2D) graphics);
				}
			};
			surface.setFocusable(true);
			surface.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			final MouseAdapter mouse = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					surface.requestFocusInWindow();
					if (e.getButton() == MouseEvent.BUTTON1)
						start = e.getPoint();
					else
						cancel();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (start != null) {
						selection = SnipSelection.between(start, e.getPoint(), new Rectangle(area.getSize()));
						surface.repaint();
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (start != null && e.getButton() == MouseEvent.BUTTON1)
						validate(SnipSelection.between(start, e.getPoint(), new Rectangle(area.getSize())));
				}
			};
			surface.addMouseListener(mouse);
			surface.addMouseMotionListener(mouse);
			surface.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
			surface.getActionMap().put("cancel", new javax.swing.AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel();
				}
			});
			setContentPane(surface);
		}

		void takeFocus() {
			getContentPane().requestFocusInWindow();
		}

		private void paintSurface(Graphics2D g) {
			g.drawImage(screen, 0, 0, null);
			g.setColor(DIMMING);
			g.fillRect(0, 0, getWidth(), getHeight());
			if (selection != null && selection.width > 0 && selection.height > 0) {
				g.drawImage(screen, selection.x, selection.y, selection.x + selection.width, selection.y + selection.height,
				    selection.x, selection.y, selection.x + selection.width, selection.y + selection.height, null);
				g.setColor(BORDER);
				g.drawRect(selection.x, selection.y, selection.width, selection.height);
			}
		}

		private void validate(Rectangle chosen) {
			dispose();
			finish(screen, chosen);
		}

		private void cancel() {
			dispose();
			finish(screen, null);
		}
	}
}
