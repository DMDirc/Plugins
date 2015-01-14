/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.parser.interfaces.Parser;

/** Non list mode panel. */
public final class ChannelModesPane extends ModesPane {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent group chat. */
    private final GroupChat groupChat;

    /**
     * Creates a new instance of ChannelModesPane.
     *
     * @param groupChat Parent group chat
     * @param iconManager The icon manager to use
     */
    public ChannelModesPane(final GroupChat groupChat, final IconManager iconManager) {
        super(groupChat.getWindowModel().getConfigManager(), iconManager);

        this.groupChat = groupChat;
        initModesPanel();
    }

    @Override
    public boolean hasModeValue(final String mode) {
        return groupChat.getWindowModel().getConfigManager().hasOptionString("server",
                "mode" + mode);
    }

    @Override
    public String getModeValue(final String mode) {
        return groupChat.getWindowModel().getConfigManager().getOption("server", "mode" + mode);
    }

    @Override
    public boolean isModeEnabled(final String mode) {
        return !groupChat.getWindowModel().getConfigManager().hasOptionString("server",
                "enablemode" + mode) || groupChat.getWindowModel().getConfigManager()
                .getOptionBool("server", "enablemode" + mode);
    }

    @Override
    public boolean isModeSettable(final String mode) {
        return groupChat.getConnection().get().getParser().get().isUserSettable(mode.toCharArray()
                [0]);
    }

    @Override
    public String getAvailableBooleanModes() {
        return groupChat.getConnection().get().getParser().get().getBooleanChannelModes();
    }

    @Override
    public String getOurBooleanModes() {
        return groupChat.getModes();
    }

    @Override
    public String getAllParamModes() {
        final Parser parser = groupChat.getConnection().get().getParser().get();
        return parser.getParameterChannelModes()
                + parser.getDoubleParameterChannelModes();
    }

    @Override
    public String getParamModeValue(final String mode) {
        return groupChat.getModeValue(mode.charAt(0));
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

        if (state) {
            groupChat.setMode(mode.charAt(0), parameter);
        } else {
            groupChat.removeMode(mode.charAt(0), parameter);
        }
    }

    @Override
    public void flushModes() {
        groupChat.flushModes();
    }

}
