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

package com.dmdirc.addons.ui_swing.components.modes;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.parser.interfaces.Parser;

/** Non list mode panel. */
public final class ChannelModesPane extends ModesPane {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent channel. */
    private final Channel channel;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Creates a new instance of ChannelModesPane.
     *
     * @param controller Swing controller
     * @param channel    Parent channel
     */
    public ChannelModesPane(final SwingController controller,
            final Channel channel) {
        super();

        this.controller = controller;
        this.channel = channel;
        initModesPanel();
    }

    
    @Override
    public boolean hasModeValue(final String mode) {
        return channel.getConfigManager().hasOptionString("server", "mode"
                + mode);
    }

    
    @Override
    public String getModeValue(final String mode) {
        return channel.getConfigManager().getOption("server", "mode" + mode);
    }

    
    @Override
    public boolean isModeEnabled(final String mode) {
        return !channel.getConfigManager().hasOptionString("server",
                "enablemode" + mode) || channel.getConfigManager()
                .getOptionBool("server", "enablemode" + mode);
    }

    
    @Override
    public boolean isModeSettable(final String mode) {
        return channel.getConnection().getParser().isUserSettable(
                mode.toCharArray()[0]);
    }

    
    @Override
    public String getAvailableBooleanModes() {
        return channel.getConnection().getParser().getBooleanChannelModes();
    }

    
    @Override
    public String getOurBooleanModes() {
        return channel.getChannelInfo().getModes();
    }

    
    @Override
    public String getAllParamModes() {
        final Parser parser = channel.getConnection().getParser();
        return parser.getParameterChannelModes()
                + parser.getDoubleParameterChannelModes();
    }

    
    @Override
    public String getParamModeValue(final String mode) {
        return channel.getChannelInfo().getMode(mode.charAt(0));
    }

    
    @Override
    public void alterMode(final boolean add, final String mode,
            final String parameter) {
        final boolean state;
        if (getBooleanModes().containsKey(mode)) {
            state = getBooleanModes().get(mode).isSelected();
        } else {
            state = getParamModes().get(mode).getState();
        }
        channel.getChannelInfo().alterMode(state, mode.charAt(0), parameter);
    }

    
    @Override
    public void flushModes() {
        channel.getChannelInfo().flushModes();
    }

    
    @Override
    public SwingController getSwingController() {
        return controller;
    }

}
