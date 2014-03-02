/*
 * @author Stanislav Lapitsky
 * @version 1.0
 *
 * Extended for Hyperlink events
 */

package com.dmdirc.addons.ui_swing.components.text;

import com.dmdirc.ui.messages.IRCTextAttribute;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 *
 * Extended for Hyperlink events
 */
public class WrapEditorKit extends StyledEditorKit implements MouseListener,
        MouseMotionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Wrap column factory. */
    private final ViewFactory defaultFactory = new WrapColumnFactory();
    /** Hand cursor. */
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    /** Are we wrapping text? */
    private final boolean wrap;
    /** Associated Component. */
    private JEditorPane editorPane;

    /**
     * Initialises a new wrapping editor kit.
     *
     * @param wrapping true iif the text needs to wrap
     */
    public WrapEditorKit(final boolean wrapping) {
        super();

        wrap = wrapping;
    }

    @Override
    public void install(final JEditorPane c) {
        super.install(c);
        editorPane = c;
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
    }

    @Override
    public void deinstall(final JEditorPane c) {
        c.removeMouseListener(this);
        c.removeMouseMotionListener(this);
        editorPane = null;
        super.deinstall(c);
    }

    @Override
    public ViewFactory getViewFactory() {
        if (wrap) {
            return super.getViewFactory();
        } else {
            return defaultFactory;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        if (editorPane == null) {
            return;
        }
        if (!editorPane.isEditable() && (characterElementAt(e).getAttributes()
                .getAttribute(IRCTextAttribute.HYPERLINK) != null
                || characterElementAt(e).getAttributes().getAttribute(
                        IRCTextAttribute.CHANNEL) != null
                || characterElementAt(e).getAttributes().getAttribute(
                        IRCTextAttribute.NICKNAME) != null)) {
            editorPane.setCursor(HAND_CURSOR);
            return;
        }
        editorPane.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e) || editorPane == null) {
            return;
        }
        if (!editorPane.isEditable()) {
            Object target = characterElementAt(e).getAttributes().getAttribute(
                    IRCTextAttribute.HYPERLINK);
            if (target != null) {
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane,
                        HyperlinkEvent.EventType.ACTIVATED, null,
                        (String) target));
            }
            target = characterElementAt(e).getAttributes().getAttribute(
                    IRCTextAttribute.CHANNEL);
            if (target != null) {
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane,
                        HyperlinkEvent.EventType.ACTIVATED, null,
                        (String) target));
            }
            target = characterElementAt(e).getAttributes().getAttribute(
                    IRCTextAttribute.NICKNAME);
            if (target != null) {
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane,
                        HyperlinkEvent.EventType.ACTIVATED, null,
                        (String) target));
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse Event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        //Ignore
    }

    /**
     * Returns the character element for the position of the mouse event.
     *
     * @param e Mouse event to get position from
     *
     * @return Character element at mouse event
     */
    private Element characterElementAt(final MouseEvent e) {
        return ((StyledDocument) editorPane.getDocument()).getCharacterElement(
                editorPane.getUI().viewToModel(editorPane, e.getPoint()));
    }

}
