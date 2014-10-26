/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.ui_swing;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.components.DMDircUndoableEditListener;
import com.dmdirc.util.colours.Colour;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import net.miginfocom.layout.PlatformDefaults;

/**
 * UI constants.
 */
@SuppressWarnings("PMD.UnusedImports")
public final class UIUtilities {

    /**
     * GTK LAF class name.
     */
    private static final String GTKUI = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    /**
     * Nimbus LAF class name.
     */
    private static final String NIMBUSUI = "sun.swing.plaf.nimbus.NimbusLookAndFeel";
    /**
     * Windows LAF class name.
     */
    private static final String WINDOWSUI = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    /**
     * Windows classic LAF class name.
     */
    private static final String WINDOWSCLASSICUI
            = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";

    /** Not intended to be instantiated. */
    private UIUtilities() {
    }

    /**
     * Adds an undo manager and associated key bindings to the specified text component.
     *
     * @param eventBus  The event bus to post errors to
     * @param component The text component to add an undo manager to
     */
    public static void addUndoManager(final DMDircMBassador eventBus, final JTextComponent component) {
        final UndoManager undoManager = new UndoManager();

        // Listen for undo and redo events
        component.getDocument().addUndoableEditListener(
                new DMDircUndoableEditListener(undoManager));

        // Create an undo action and add it to the text component
        component.getActionMap().put("Undo", new UndoAction(eventBus, undoManager));

        // Bind the undo action to ctl-Z
        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        // Create a redo action and add it to the text component
        component.getActionMap().put("Redo", new RedoAction(eventBus, undoManager));

        // Bind the redo action to ctl-Y
        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }

    /**
     * Initialises any settings required by this UI (this is always called before any aspect of the
     * UI is instantiated).
     *
     * @throws UnsupportedOperationException If unable to switch to the system look and feel
     */
    public static void initUISettings() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (InstantiationException | ClassNotFoundException |
                UnsupportedLookAndFeelException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Unable to switch to the "
                    + "system look and feel", ex);
        }

        UIManager.put("swing.useSystemFontSettings", true);
        if (getTabbedPaneOpaque()) {
            // If this is set on windows then .setOpaque seems to be ignored
            // and still used as true
            UIManager.put("TabbedPane.contentOpaque", false);
        }
        UIManager.put("swing.boldMetal", false);
        UIManager.put("InternalFrame.useTaskBar", false);
        UIManager.put("SplitPaneDivider.border",
                BorderFactory.createEmptyBorder());
        UIManager.put("Tree.scrollsOnExpand", true);
        UIManager.put("Tree.scrollsHorizontallyAndVertically", true);
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPane.dividerSize", (int) PlatformDefaults.
                getPanelInsets(0).getValue());
        UIManager.put("TreeUI", TreeUI.class.getName());
        UIManager.put("TextField.background", new Color(255, 255, 255));
        PlatformDefaults.setDefaultRowAlignmentBaseline(false);
    }

    /**
     * Sets the font of all components in the current look and feel to the specified font, using the
     * style and size from the original font.
     *
     * @param font New font
     */
    public static void setUIFont(final Font font) {
        for(Object key : UIManager.getDefaults().keySet()) {
            final Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                final FontUIResource orig = (FontUIResource) value;
                UIManager.put(key, new UIDefaults.ProxyLazyValue(
                        "javax.swing.plaf.FontUIResource", new Object[]{
                        font.getFontName(), orig.getStyle(), orig.getSize()
                }));
            }
        }
    }

    /**
     * Returns the class name of the look and feel from its display name.
     *
     * @param displayName Look and feel display name
     *
     * @return Look and feel class name or a zero length string
     */
    public static String getLookAndFeel(final String displayName) {
        if (displayName == null || displayName.isEmpty() || "Native".equals(displayName)) {
            return UIManager.getSystemLookAndFeelClassName();
        }

        final StringBuilder classNameBuilder = new StringBuilder();

        for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if (laf.getName().equals(displayName)) {
                classNameBuilder.append(laf.getClassName());
                break;
            }
        }

        if (classNameBuilder.length() == 0) {
            classNameBuilder.append(UIManager.getSystemLookAndFeelClassName());
        }

        return classNameBuilder.toString();
    }

    /**
     * Invokes and waits for the specified runnable, executed on the EDT.
     *
     * @param runnable Thread to be executed
     */
    public static void invokeAndWait(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ex) {
                //Ignore
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex); // NOPMD
            }
        }
    }

    /**
     * Invokes and waits for the specified callable, executed on the EDT.
     *
     * @param <T>        The return type of the returnable thread
     * @param returnable Thread to be executed
     *
     * @return Result from the completed thread or null if an exception occurred
     */
    public static <T> T invokeAndWait(final Callable<T> returnable) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                return returnable.call();
            } else {
                final FutureTask<T> task = new FutureTask<>(returnable);
                SwingUtilities.invokeAndWait(task);
                return task.get();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // NOPMD
        }
    }

    /**
     * Queues the runnable to be executed on the EDT.
     *
     * @param runnable Runnable to be executed.
     */
    public static void invokeLater(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    /**
     * Check if we are using the GTK look and feel.
     *
     * @return true iif the LAF is GTK
     */
    public static boolean isGTKUI() {
        return GTKUI.equals(UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Check if we are using the Nimbus look and feel.
     *
     * @return true iif the LAF is Nimbus
     */
    public static boolean isNimbusUI() {
        return NIMBUSUI.equals(UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Check if we are using the new Windows Look and Feel.
     *
     * @return true iif the current LAF is Windows
     */
    public static boolean isWindowsNewUI() {
        return WINDOWSUI.equals(
                UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Check if we are using the new Windows Classic Look and Feel.
     *
     * @return true iif the current LAF is Windows Classic
     */
    public static boolean isWindowsClassicUI() {
        return WINDOWSCLASSICUI.equals(UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Check if we are using one of the Windows Look and Feels.
     *
     * @return True iff the current LAF is "Windows" or "Windows Classic"
     */
    public static boolean isWindowsUI() {
        return isWindowsNewUI() || isWindowsClassicUI();
    }

    /**
     * Get the value to pass to set Opaque on items being added to a JTabbedPane. Currently they
     * need to be transparent on Windows (non classic), Apple, Nimbus and GTK.
     *
     * @return True if tabbed panes should be opaque
     *
     * @since 0.6
     */
    public static boolean getTabbedPaneOpaque() {
        return !(isWindowsNewUI() || Apple.isAppleUI() || isNimbusUI() || isGTKUI());
    }

    /**
     * Get the DOWN_MASK for the command/ctrl key.
     *
     * @return on OSX this returns META_DOWN_MASK, else CTRL_DOWN_MASK
     *
     * @since 0.6
     */
    public static int getCtrlDownMask() {
        return Apple.isAppleUI() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }

    /**
     * Get the MASK for the command/ctrl key.
     *
     * @return on OSX this returns META_MASK, else CTRL_MASK
     *
     * @since 0.6
     */
    public static int getCtrlMask() {
        return Apple.isAppleUI() ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
    }

    /**
     * Check if the command/ctrl key is pressed down.
     *
     * @param e The KeyEvent to check
     *
     * @return on OSX this returns e.isMetaDown(), else e.isControlDown()
     *
     * @since 0.6
     */
    public static boolean isCtrlDown(final KeyEvent e) {
        return Apple.isAppleUI() ? e.isMetaDown() : e.isControlDown();
    }

    /**
     * Clips a string if its longer than the specified width.
     *
     * @param component     Component containing string
     * @param string        String to check
     * @param avaiableWidth Available Width
     *
     * @return String (clipped if required)
     */
    public static String clipStringifNeeded(final JComponent component,
            final String string, final int avaiableWidth) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        final FontMetrics fm = component.getFontMetrics(component.getFont());
        final int width = SwingUtilities.computeStringWidth(fm, string);
        if (width > avaiableWidth) {
            return clipString(component, string, avaiableWidth);
        }
        return string;
    }

    /**
     * Clips the passed string .
     *
     * @param component     Component containing string
     * @param string        String to check
     * @param avaiableWidth Available Width
     *
     * @return String (clipped if required)
     */
    public static String clipString(final JComponent component,
            final String string, final int avaiableWidth) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        final FontMetrics fm = component.getFontMetrics(component.getFont());
        final String clipString = "...";
        int width = SwingUtilities.computeStringWidth(fm, clipString);

        int nChars = 0;
        for (final int max = string.length(); nChars < max; nChars++) {
            width += fm.charWidth(string.charAt(nChars));
            if (width > avaiableWidth) {
                break;
            }
        }
        return string.substring(0, nChars) + clipString;
    }

    /**
     * Resets the scroll pane to 0,0.
     *
     * @param scrollPane Scrollpane to reset
     *
     * @since 0.6.3m1
     */
    public static void resetScrollPane(final JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            scrollPane.getHorizontalScrollBar().setValue(0);
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    /**
     * Paints the background, either from the config setting or the background colour of the
     * textpane.
     *
     * @param g                Graphics object to draw onto
     * @param bounds           Bounds to paint within
     * @param backgroundImage  background image
     * @param backgroundOption How to draw the background
     */
    public static void paintBackground(final Graphics2D g,
            final Rectangle bounds,
            final Image backgroundImage,
            final BackgroundOption backgroundOption) {
        if (backgroundImage == null) {
            paintNoBackground(g, bounds);
        } else {
            switch (backgroundOption) {
                case TILED:
                    paintTiledBackground(g, bounds, backgroundImage);
                    break;
                case SCALE:
                    paintStretchedBackground(g, bounds, backgroundImage);
                    break;
                case SCALE_ASPECT_RATIO:
                    paintStretchedAspectRatioBackground(g, bounds, backgroundImage);
                    break;
                case CENTER:
                    paintCenterBackground(g, bounds, backgroundImage);
                    break;
                default:
                    break;
            }
        }
    }

    private static void paintNoBackground(final Graphics2D g, final Shape bounds) {
        g.fill(bounds);
    }

    private static void paintStretchedBackground(final Graphics2D g,
            final Rectangle bounds, final Image backgroundImage) {
        g.drawImage(backgroundImage, 0, 0, bounds.width, bounds.height, null);
    }

    private static void paintCenterBackground(final Graphics2D g,
            final Rectangle bounds, final Image backgroundImage) {
        final int x = bounds.width / 2 - backgroundImage.getWidth(null) / 2;
        final int y = bounds.height / 2 - backgroundImage.getHeight(null) / 2;
        g.drawImage(backgroundImage, x, y, backgroundImage.getWidth(null),
                backgroundImage.getHeight(null), null);
    }

    private static void paintStretchedAspectRatioBackground(final Graphics2D g,
            final Rectangle bounds, final Image backgroundImage) {
        final double widthratio = bounds.width
                / (double) backgroundImage.getWidth(null);
        final double heightratio = bounds.height
                / (double) backgroundImage.getHeight(null);
        final double ratio = Math.min(widthratio, heightratio);
        final int width = (int) (backgroundImage.getWidth(null) * ratio);
        final int height = (int) (backgroundImage.getWidth(null) * ratio);

        final int x = bounds.width / 2 - width / 2;
        final int y = bounds.height / 2 - height / 2;
        g.drawImage(backgroundImage, x, y, width, height, null);
    }

    private static void paintTiledBackground(final Graphics2D g,
            final Rectangle bounds, final Image backgroundImage) {
        final int width = backgroundImage.getWidth(null);
        final int height = backgroundImage.getHeight(null);

        if (width <= 0 || height <= 0) {
            // ARG!
            return;
        }

        for (int x = 0; x < bounds.width; x += width) {
            for (int y = 0; y < bounds.height; y += height) {
                g.drawImage(backgroundImage, x, y, width, height, null);
            }
        }
    }

    /**
     * Adds a popup listener which will modify the width of the combo box popup menu to be sized
     * according to the preferred size of its components.
     *
     * @param combo Combo box to modify
     */
    public static void addComboBoxWidthModifier(final JComboBox<?> combo) {
        combo.addPopupMenuListener(new ComboBoxWidthModifier());
    }

    /**
     * Converts a DMDirc {@link Colour} into an AWT-specific {@link Color} by copying the values of
     * the red, green and blue channels.
     *
     * @param colour The colour to be converted
     *
     * @return A corresponding AWT colour
     */
    public static Color convertColour(final Colour colour) {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue());
    }

    /**
     * Converts the given colour into a hexadecimal string.
     *
     * @param colour The colour to be converted
     *
     * @return A corresponding 6 character hex string
     */
    public static String getHex(final Color colour) {
        return getHex(colour.getRed()) + getHex(colour.getGreen())
                + getHex(colour.getBlue());
    }

    /**
     * Converts the specified integer (in the range 0-255) into a hex string.
     *
     * @param value The integer to convert
     *
     * @return A 2 char hex string representing the specified integer
     */
    private static String getHex(final int value) {
        final String hex = Integer.toHexString(value);
        return (hex.length() < 2 ? "0" : "") + hex;
    }

}
