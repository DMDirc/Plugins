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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.textpane.LineRenderer.RenderResult;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.messages.CachingDocument;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.messages.LinePosition;
import com.dmdirc.util.StringUtils;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;

/** Canvas object to draw text. */
class TextPaneCanvas extends JPanel implements MouseInputListener,
        ComponentListener, AdjustmentListener, ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 8;
    /** Hand cursor. */
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    /** Single Side padding for textpane. */
    private static final int SINGLE_SIDE_PADDING = 3;
    /** Both Side padding for textpane. */
    private static final int DOUBLE_SIDE_PADDING = SINGLE_SIDE_PADDING * 2;
    /** IRCDocument. */
    private final CachingDocument<AttributedString> document;
    /** parent textpane. */
    private final TextPane textPane;
    /** Position -> LineInfo. */
    private final Map<LineInfo, Rectangle2D.Float> lineAreas;
    /** TextLayout -> Line numbers. */
    private final Map<LineInfo, TextLayout> lineLayouts;
    /** Start line. */
    private int startLine;
    /** Selection. */
    private LinePosition selection;
    /** First visible line (from the top). */
    private int firstVisibleLine;
    /** Last visible line (from the top). */
    private int lastVisibleLine;
    /** Config Manager. */
    private final AggregateConfigProvider manager;
    /** Quick copy? */
    private boolean quickCopy;
    /** Mouse click listeners. */
    private final ListenerList listeners = new ListenerList();
    /** Renderer to use for lines. */
    private final LineRenderer lineRenderer;

    /**
     * Creates a new text pane canvas.
     *
     * @param parent   parent text pane for the canvas
     * @param document IRCDocument to be displayed
     */
    public TextPaneCanvas(final TextPane parent, final CachingDocument<AttributedString> document) {
        this.document = document;
        textPane = parent;
        this.manager = parent.getWindow().getContainer().getConfigManager();
        this.lineRenderer = new BasicTextLineRenderer(textPane, this, document);
        startLine = 0;
        setDoubleBuffered(true);
        setOpaque(true);
        lineLayouts = new HashMap<>();
        lineAreas = new HashMap<>();
        selection = new LinePosition(-1, -1, -1, -1);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        manager.addChangeListener("ui", "quickCopy", this);

        updateCachedSettings();
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /**
     * Paints the text onto the canvas.
     *
     * @param graphics graphics object to draw onto
     */
    @Override
    public void paintComponent(final Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        final Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g.addRenderingHints(desktopHints);
        }
        paintOntoGraphics(g);
    }

    /**
     * Re calculates positions of lines and repaints if required.
     */
    protected void recalc() {
        if (isVisible()) {
            repaint();
        }
    }

    /**
     * Updates cached config settings.
     */
    private void updateCachedSettings() {
        quickCopy = manager.getOptionBool("ui", "quickCopy");
        UIUtilities.invokeLater(this::recalc);
    }

    private void paintOntoGraphics(final Graphics2D g) {
        final float formatWidth = getWidth() - DOUBLE_SIDE_PADDING;
        final float formatHeight = getHeight();
        float drawPosY = formatHeight - DOUBLE_SIDE_PADDING;

        lineLayouts.clear();
        lineAreas.clear();

        //check theres something to draw and theres some space to draw in
        if (document.getNumLines() == 0 || formatWidth < 1) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        // Check the start line is in range
        startLine = Math.max(0, Math.min(document.getNumLines() - 1, startLine));

        //sets the last visible line
        lastVisibleLine = startLine;
        firstVisibleLine = startLine;

        // Iterate through the lines
        for (int line = startLine; line >= 0; line--) {
            drawPosY = paintLineOntoGraphics(g, formatWidth, formatHeight, drawPosY, line);
            if (drawPosY <= 0) {
                break;
            }
        }

        checkForLink();
    }

    private float paintLineOntoGraphics(final Graphics2D g, final float formatWidth,
            final float formatHeight, final float drawPosY, final int line) {
        final RenderResult result = lineRenderer.render(g, formatWidth, formatHeight, drawPosY,
                line);
        lineAreas.putAll(result.drawnAreas);
        lineLayouts.putAll(result.textLayouts);
        firstVisibleLine = result.firstVisibleLine;
        return drawPosY - result.totalHeight;
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        if (startLine != e.getValue()) {
            startLine = e.getValue();
            recalc();
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        final LineInfo lineInfo = getClickPosition(getMousePosition(), true);
        fireMouseEvents(getClickType(lineInfo), MouseEventType.CLICK, e);

        if (lineInfo.getLine() != -1) {
            final String clickedText = document.getLine(lineInfo.getLine()).getText();

            final int start;
            final int end;
            if (lineInfo.getIndex() == -1) {
                start = -1;
                end = -1;
            } else {
                final int[] extent = StringUtils.indiciesOfWord(clickedText, lineInfo.getIndex());
                start = extent[0];
                end = extent[1];
            }

            if (e.getClickCount() == 2) {
                setSelection(lineInfo.getLine(), start, end, e.isShiftDown());
            } else if (e.getClickCount() == 3) {
                setSelection(lineInfo.getLine(), 0, clickedText.length(), e.isShiftDown());
            }
        }
    }

    /**
     * Sets the selection to a range of characters on the specified line. If quick copy is enabled,
     * the selection will be copied.
     *
     * @param line The line of the selection
     * @param start The start of the selection
     * @param end The end of the selection
     * @param copyControlCharacters Whether or not to copy control characters.
     */
    private void setSelection(final int line, final int start, final int end,
            final boolean copyControlCharacters) {
        selection.setStartLine(line);
        selection.setEndLine(line);
        selection.setStartPos(start);
        selection.setEndPos(end);
        if (quickCopy) {
            textPane.copy(copyControlCharacters);
            clearSelection();
        }
    }

    /**
     * Returns the type of text this click represents.
     *
     * @param lineInfo Line info of click.
     *
     * @return Click type for specified position
     */
    public ClickTypeValue getClickType(final LineInfo lineInfo) {
        if (lineInfo.getLine() != -1) {
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    lineInfo.getLine()).getIterator();
            final int index = lineInfo.getIndex();
            if (index >= iterator.getBeginIndex() && index <= iterator.getEndIndex()) {
                iterator.setIndex(lineInfo.getIndex());
                final Object linkAttribute =
                        iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
                if (linkAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.HYPERLINK, (String) linkAttribute);
                }
                final Object channelAttribute =
                        iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
                if (channelAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.CHANNEL, (String) channelAttribute);
                }
                final Object nickAttribute =
                        iterator.getAttributes().get(IRCTextAttribute.NICKNAME);
                if (nickAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.NICKNAME, (String) nickAttribute);
                }
            } else {
                return new ClickTypeValue(ClickType.NORMAL, "");
            }
        }
        return new ClickTypeValue(ClickType.NORMAL, "");
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                MouseEventType.PRESSED, e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.CLICK, e);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                MouseEventType.RELEASED, e);
        if (quickCopy) {
            textPane.copy((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK);
            SwingUtilities.invokeLater(this::clearSelection);
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.RELEASED, e);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
            highlightEvent(MouseEventType.DRAG, e);
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        checkForLink();
    }

    /** Checks for a link under the cursor and sets appropriately. */
    private void checkForLink() {
        final AttributedCharacterIterator iterator = getIterator(getMousePosition());

        if (iterator != null
                && (iterator.getAttribute(IRCTextAttribute.HYPERLINK) != null
                || iterator.getAttribute(IRCTextAttribute.CHANNEL) != null
                || iterator.getAttribute(IRCTextAttribute.NICKNAME) != null)) {
            setCursor(HAND_CURSOR);
            return;
        }

        if (getCursor() == HAND_CURSOR) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Retrieves a character iterator for the text at the specified mouse position.
     *
     * @since 0.6.4
     * @param mousePosition The mouse position to retrieve text for
     *
     * @return A corresponding character iterator, or null if the specified mouse position doesn't
     *         correspond to any text
     */
    private AttributedCharacterIterator getIterator(final Point mousePosition) {
        final LineInfo lineInfo = getClickPosition(mousePosition, false);

        if (lineInfo.getLine() != -1
                && document.getLine(lineInfo.getLine()) != null) {
            final AttributedCharacterIterator iterator =
                    document.getStyledLine(lineInfo.getLine()).getIterator();

            if (lineInfo.getIndex() < iterator.getBeginIndex()
                    || lineInfo.getIndex() > iterator.getEndIndex()) {
                return null;
            }

            iterator.setIndex(lineInfo.getIndex());
            return iterator;
        }

        return null;
    }

    /**
     * Sets the selection for the given event.
     *
     * @param type mouse event type
     * @param e    responsible mouse event
     */
    protected void highlightEvent(final MouseEventType type, final MouseEvent e) {
        if (isVisible()) {
            final Point point = e.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(point, this);
            if (!contains(point)) {
                final Rectangle bounds = getBounds();
                final Point mousePos = e.getPoint();
                if (mousePos.getX() < bounds.getX()) {
                    point.setLocation(bounds.getX() + SINGLE_SIDE_PADDING, point.getY());
                } else if (mousePos.getX() > bounds.getX() + bounds.getWidth()) {
                    point.setLocation(bounds.getX() + bounds.getWidth()
                            - SINGLE_SIDE_PADDING, point.getY());
                }

                if (mousePos.getY() < bounds.getY()) {
                    point.setLocation(point.getX(), bounds.getY() + DOUBLE_SIDE_PADDING);
                } else if (mousePos.getY() > bounds.getY() + bounds.getHeight()) {
                    point.setLocation(bounds.getX() + bounds.getWidth() - SINGLE_SIDE_PADDING,
                            bounds.getY() + bounds.getHeight() - DOUBLE_SIDE_PADDING - 1);
                }
            }
            LineInfo info = getClickPosition(point, true);

            // TODO: These are fairly expensive if the user is moving around a lot; cache them.
            final Rectangle2D.Float first = getFirstLineRectangle();
            final Rectangle2D.Float last = getLastLineRectangle();
            if (info.getLine() == -1 && info.getPart() == -1 && contains(point)
                    && document.getNumLines() != 0 && first != null && last != null) {
                if (first.getY() >= point.getY()) {
                    info = getFirstLineInfo();
                } else if (last.getY() <= point.getY()) {
                    info = getLastLineInfo();
                }
            }

            if (info.getLine() != -1 && info.getPart() != -1) {
                if (type == MouseEventType.CLICK) {
                    selection.setStartLine(info.getLine());
                    selection.setStartPos(info.getIndex());
                }
                selection.setEndLine(info.getLine());
                selection.setEndPos(info.getIndex());

                recalc();
            }
        }
    }

    /**
     * Returns the visible rectangle of the first line.
     *
     * @return First line's rectangle
     */
    private Rectangle2D.Float getFirstLineRectangle() {
        for (Map.Entry<LineInfo, Rectangle2D.Float> entry : lineAreas.entrySet()) {
            if (entry.getKey().getLine() == firstVisibleLine) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the last line's visible rectangle.
     *
     * @return Last line's rectangle
     */
    private Rectangle2D.Float getLastLineRectangle() {
        for (Map.Entry<LineInfo, Rectangle2D.Float> entry : lineAreas.entrySet()) {
            if (entry.getKey().getLine() == lastVisibleLine) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the LineInfo for the first visible line.
     *
     * @return First line's line info
     */
    private LineInfo getFirstLineInfo() {
        int firstLineParts = Integer.MAX_VALUE;
        for (LineInfo info : lineAreas.keySet()) {
            if (info.getLine() == firstVisibleLine && info.getPart() < firstLineParts) {
                firstLineParts = info.getPart();
            }
        }
        return new LineInfo(firstVisibleLine, firstLineParts);
    }

    /**
     * Returns the LineInfo for the last visible line.
     *
     * @return Last line's line info
     */
    private LineInfo getLastLineInfo() {
        int lastLineParts = -1;
        for (LineInfo info : lineAreas.keySet()) {
            if (info.getLine() == lastVisibleLine && info.getPart() > lastLineParts) {
                lastLineParts = info.getPart();
            }
        }
        return new LineInfo(lastVisibleLine + 1, lastLineParts);
    }

    /**
     *
     * Returns the line information from a mouse click inside the textpane.
     *
     * @param point     mouse position
     * @param selection Are we selecting text?
     *
     * @return line number, line part, position in whole line
     */
    public LineInfo getClickPosition(final Point point, final boolean selection) {
        int lineNumber = -1;
        int linePart = -1;
        int pos = 0;

        if (point != null) {
            for (Map.Entry<LineInfo, Rectangle2D.Float> entry : lineAreas.entrySet()) {
                if (entry.getValue().contains(point)) {
                    lineNumber = entry.getKey().getLine();
                    linePart = entry.getKey().getPart();
                }
            }

            pos = getHitPosition(lineNumber, linePart, (int) point.getX(), (int) point.getY(),
                    selection);
        }

        return new LineInfo(lineNumber, linePart, pos);
    }

    /**
     * Returns the character index for a specified line and part for a specific hit position.
     *
     * @param lineNumber Line number
     * @param linePart   Line part
     * @param x          X position
     * @param y          Y position
     *
     * @return Hit position
     */
    private int getHitPosition(final int lineNumber, final int linePart,
            final float x, final float y, final boolean selection) {
        int pos = 0;

        for (Map.Entry<LineInfo, TextLayout> entry : lineLayouts.entrySet()) {
            if (entry.getKey().getLine() == lineNumber) {
                if (entry.getKey().getPart() < linePart) {
                    pos += entry.getValue().getCharacterCount();
                } else if (entry.getKey().getPart() == linePart) {
                    final TextHitInfo hit =
                            entry.getValue().hitTestChar(x - DOUBLE_SIDE_PADDING, y);
                    if (selection || x > entry.getValue().getBounds().getX()) {
                        pos += hit.getInsertionIndex();
                    } else {
                        pos += hit.getCharIndex();
                    }
                }
            }
        }
        return pos;
    }

    /**
     * Returns the selected range info.
     *
     * @return Selected range info
     */
    protected LinePosition getSelectedRange() {
        return selection.getNormalised();
    }

    /** Clears the selection. */
    protected void clearSelection() {
        selection.setEndLine(selection.getStartLine());
        selection.setEndPos(selection.getStartPos());
        recalc();
    }

    /**
     * Selects the specified region of text.
     *
     * @param position Line position
     */
    public void setSelectedRange(final LinePosition position) {
        selection = new LinePosition(position);
        recalc();
    }

    /**
     * Returns the first visible line.
     *
     * @return the line number of the first visible line
     */
    public int getFirstVisibleLine() {
        return firstVisibleLine;
    }

    /**
     * Returns the last visible line.
     *
     * @return the line number of the last visible line
     */
    public int getLastVisibleLine() {
        return lastVisibleLine;
    }

    /**
     * Returns the number of visible lines.
     *
     * @return Number of visible lines
     */
    public int getNumVisibleLines() {
        return lastVisibleLine - firstVisibleLine;
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        recalc();
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentShown(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void configChanged(final String domain, final String key) {
        updateCachedSettings();
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        final AttributedCharacterIterator iterator = getIterator(
                event.getPoint());

        if (iterator != null
                && iterator.getAttribute(IRCTextAttribute.TOOLTIP) != null) {
            return iterator.getAttribute(IRCTextAttribute.TOOLTIP).toString();
        }

        return super.getToolTipText(event);
    }

    /**
     * Fires mouse clicked events with the associated values.
     *
     * @param clickType Click type
     * @param eventType Mouse event type
     * @param event     Triggering mouse event
     */
    private void fireMouseEvents(final ClickTypeValue clickType,
            final MouseEventType eventType, final MouseEvent event) {
        for (TextPaneListener listener : listeners.get(TextPaneListener.class)) {
            listener.mouseClicked(clickType, eventType, event);
        }
    }

    /**
     * Adds a textpane listener.
     *
     * @param listener Listener to add
     */
    public void addTextPaneListener(final TextPaneListener listener) {
        listeners.add(TextPaneListener.class, listener);
    }

    /**
     * Removes a textpane listener.
     *
     * @param listener Listener to remove
     */
    public void removeTextPaneListener(final TextPaneListener listener) {
        listeners.remove(TextPaneListener.class, listener);
    }

}
