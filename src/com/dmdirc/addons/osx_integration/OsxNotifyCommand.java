package com.dmdirc.addons.osx_integration;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;

public class OsxNotifyCommand extends Command {
    public static final BaseCommandInfo INFO = new BaseCommandInfo(
            "osxnotify",
            "osxnotify - send a notification to the OSX notification centre",
            CommandType.TYPE_GLOBAL
    );

    private final SwingController uiController;

    public OsxNotifyCommand(SwingController uiController) {
        this.uiController = uiController;
    }

    @Override
    public void execute(FrameContainer origin, CommandArguments args, CommandContext context) {

    }
}
