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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.CopyAction;
import com.dmdirc.addons.ui_swing.actions.CutAction;
import com.dmdirc.addons.ui_swing.actions.InputFieldCopyAction;
import com.dmdirc.addons.ui_swing.components.AwayLabel;
import com.dmdirc.addons.ui_swing.components.TypingLabel;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.input.TabCompleterUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.inject.Provider;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Frame with an input field.
 */
public abstract class InputTextFrame extends TextFrame implements InputWindow, MouseListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** Factory to create an {@link InputTextFramePasteAction}. */
    private final InputTextFramePasteActionFactory inputTextFramePasteActionFactory;
    /** Input field panel. */
    protected JPanel inputPanel;
    /** The InputHandler for our input field. */
    private InputHandler inputHandler;
    /** Frame input field. */
    private SwingInputField inputField;
    /** Popupmenu for this frame. */
    private JPopupMenu inputFieldPopup;
    /** Nick popup menu. */
    protected JPopupMenu nickPopup;
    /** Away label. */
    private AwayLabel awayLabel;
    /** Typing indicator label. */
    private TypingLabel typingLabel;
    /** Plugin Manager. */
    private final PluginManager pluginManager;
    /** The controller to use to retrieve command information. */
    private final CommandController commandController;
    /** The bus to dispatch input events on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of InputFrame.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param owner              WritableFrameContainer owning this frame.
     */
    protected InputTextFrame(
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final InputTextFramePasteActionFactory inputTextFramePasteActionFactory,
            final FrameContainer owner) {
        super(owner, owner.getCommandParser(), deps);

        pluginManager = deps.pluginManager;
        commandController = deps.commandController;
        eventBus = deps.eventBus;
        this.inputTextFramePasteActionFactory = inputTextFramePasteActionFactory;

        initComponents(inputFieldProvider, deps.tabCompleterUtils);

        getInputField().getTextField().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, UIUtilities.getCtrlMask()), "textpaneCopy");
        getInputField().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, UIUtilities.getCtrlMask()
                | KeyEvent.SHIFT_DOWN_MASK), "textpaneCopy");
        getInputField().getTextField().getActionMap().put("textpaneCopy",
                new InputFieldCopyAction(getTextPane(), getInputField().getTextField()));
    }

    /**
     * Initialises the instance, adding any required listeners.
     */
    @Override
    public void init() {
        getContainer().getConfigManager().getBinder().bind(this, InputTextFrame.class);
        getInputField().addMouseListener(this);
        eventBus.subscribe(awayLabel);
        eventBus.subscribe(typingLabel);
        super.init();
    }

    /**
     * Initialises the components for this frame.
     *
     * @param inputFieldProvider The provider to use to create a new input field.
     */
    private void initComponents(final Provider<SwingInputField> inputFieldProvider,
            final TabCompleterUtils tabCompleterUtils) {
        inputField = inputFieldProvider.get();
        inputHandler = new SwingInputHandler(pluginManager, inputField, commandController,
                getContainer().getCommandParser(), getContainer(), tabCompleterUtils, eventBus);
        inputHandler.addValidationListener(inputField);
        inputHandler.setTabCompleter(frameParent.getTabCompleter());

        initPopupMenu();
        nickPopup = new JPopupMenu();

        inputPanel = new JPanel(new BorderLayout(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));

        inputPanel.add(awayLabel, BorderLayout.LINE_START);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(typingLabel, BorderLayout.LINE_END);

        initInputField();
    }

    /** Initialises the popupmenu. */
    private void initPopupMenu() {
        inputFieldPopup = new JPopupMenu();

        inputFieldPopup.add(new CutAction(getInputField().getTextField()));
        inputFieldPopup.add(new CopyAction(getInputField().getTextField()));
        inputFieldPopup.add(inputTextFramePasteActionFactory.getInputTextFramePasteAction(this,
                inputField, getContainer()));
        inputFieldPopup.setOpaque(true);
        inputFieldPopup.setLightWeightPopupEnabled(true);

        awayLabel = new AwayLabel(getContainer());
        typingLabel = new TypingLabel(getContainer());
    }

    /**
     * Initialises the input field.
     */
    private void initInputField() {
        UIUtilities.addUndoManager(eventBus, getInputField().getTextField());

        getInputField().getActionMap().put("paste", inputTextFramePasteActionFactory
                .getInputTextFramePasteAction(this, inputField, getContainer()));
        getInputField().getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                "shift INSERT"), "paste");
        getInputField().getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl V"), "paste");
    }

    /**
     * Returns the input handler associated with this frame.
     *
     * @return Input handlers for this frame
     */
    @Override
    public final InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Returns the input field for this frame.
     *
     * @return SwingInputField input field for the frame.
     */
    public final SwingInputField getInputField() {
        return inputField;
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getSource() == getTextPane()) {
            processMouseEvent(mouseEvent);
        }
    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore
    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore
    }

    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() == getInputField()) {
            final Point point = getInputField().getMousePosition();

            if (point != null) {
                initPopupMenu();
                inputFieldPopup.show(this, (int) point.getX(),
                        (int) point.getY() + getTextPane().getHeight()
                        + (int) PlatformDefaults.
                        getUnitValueX("related").getValue());
            }
        }
    }

    @ConfigBinding(domain="ui", key="inputbackgroundcolour",
            fallbacks = {"ui", "backgroundcolour"}, invocation = EDTInvocation.class)
    public void handleInputBackgroundColour(final String value) {
        if (getInputField() == null || UIUtilities.isGTKUI()) {
            return;
        }
        getInputField().setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(value, null)));
    }

    @ConfigBinding(domain = "ui", key="inputforegroundcolour", fallbacks = {"ui",
            "foregroundcolour"}, invocation = EDTInvocation.class)
    public void handleInputForegroundColour(final String value) {
        if (getInputField() == null || UIUtilities.isGTKUI()) {
            return;
        }
        final Color colour = UIUtilities.convertColour(colourManager.getColourFromString(value, null));
        getInputField().setForeground(colour);
        getInputField().setCaretColor(colour);
    }

    @Override
    public void activateFrame() {
        super.activateFrame();
        inputField.requestFocusInWindow();
    }

    @Override
    public void dispose() {
        getInputField().removeMouseListener(this);
        eventBus.unsubscribe(awayLabel);
        eventBus.unsubscribe(typingLabel);
        getContainer().getConfigManager().getBinder().unbind(this);
        super.dispose();
    }

}
