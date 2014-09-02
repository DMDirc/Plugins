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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.util.collections.RollingList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

/**
 * Shows a ping history graph for the current server.
 */
public class PingHistoryPanel extends JPanel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The manager that this panel is for. */
    protected final LagDisplayManager manager;
    /** The history that we're graphing. */
    protected final RollingList<Long> history;
    /** The maximum ping value. */
    protected long maximum;

    /**
     * Creates a new history panel for the specified plugin.
     *
     * @param manager            The manager that owns this panel
     * @param activeFrameManager Active frame manager.
     */
    public PingHistoryPanel(final LagDisplayManager manager,
            final ActiveFrameManager activeFrameManager) {
        setMinimumSize(new Dimension(50, 100));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        setOpaque(false);

        this.manager = manager;
        history = manager.getHistory(activeFrameManager.getActiveFrame().getContainer().
                getConnection());

        for (Long value : history.getList()) {
            maximum = Math.max(value, maximum);
        }
    }

    @Override
    public void paint(final Graphics g) {
        super.paint(g);

        g.setColor(Color.DARK_GRAY);
        g.drawLine(2, 1, 2, getHeight() - 1);
        g.drawLine(1, getHeight() - 2, getWidth() - 1, getHeight() - 2);
        g.setFont(g.getFont().deriveFont(10f));

        final float pixelsperpointX = (getWidth() - 3)
                / (float) (history.getList().size() == 1 ? 1
                : history.getList().size() - 1);
        final float pixelsperpointY = (getHeight() - 10) / (float) maximum;

        if (history.isEmpty()) {
            g.drawString("No data", getWidth() / 2 - 25, getHeight() / 2 + 5);
        }

        long last1 = -1;
        long last2 = -1;
        final List<Long> list = history.getList();
        final Collection<Rectangle> rects = new ArrayList<>();

        float lastX = -1;
        float lastY = -1;
        for (int i = 0; i < list.size(); i++) {
            final Long value = list.get(i);

            final float x = lastX == -1 ? 2 : lastX + pixelsperpointX;
            final float y = getHeight() - 5 - value * pixelsperpointY;

            if (lastX > -1) {
                g.drawLine((int) lastX, (int) lastY, (int) x, (int) y);
            }

            g.drawRect((int) x - 1, (int) y - 1, 2, 2);

            if (manager.shouldShowLabels() && last1 > -1 && (last2 <= last1 || last1 >= value)) {
                final String text = manager.formatTime(last1);
                final Rectangle2D rect = g.getFont().getStringBounds(text,
                        ((Graphics2D) g).getFontRenderContext());
                final int width = 10 + (int) rect.getWidth();
                final int points = (int) Math.ceil(width / pixelsperpointX);
                final int diffy = (int) (last1 - (10 + rect.getHeight()) / pixelsperpointY);

                float posX = lastX - width + 7;
                final float posY = (float) (lastY + rect.getHeight() / 2) - 1;
                boolean failed = posX < 0;

                // Check left
                for (int j = Math.max(0, i - points); j < i - 1; j++) {
                    if (list.get(j) > diffy) {
                        failed = true;
                        break;
                    }
                }

                if (failed) {
                    posX = lastX + 3;
                    failed = posX + width > getWidth();

                    // Check right
                    for (int j = i; j < Math.min(list.size(), i + points); j++) {
                        if (list.get(j) > diffy) {
                            failed = true;
                            break;
                        }
                    }
                }

                if (!failed) {
                    final Rectangle myrect = new Rectangle((int) posX - 2,
                            (int) lastY - 7, 5 + (int) rect.getWidth(),
                            3 + (int) rect.getHeight());

                    for (Rectangle test : rects) {
                        if (test.intersects(myrect)) {
                            failed = true;
                        }
                    }

                    if (!failed) {
                        g.drawString(text, (int) posX, (int) posY);
                        rects.add(myrect);
                    }
                }
            }

            lastX = x;
            lastY = y;

            last2 = last1;
            last1 = value;
        }
    }

}
