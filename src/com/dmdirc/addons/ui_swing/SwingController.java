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

import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.commands.ChannelSettings;
import com.dmdirc.addons.ui_swing.commands.Input;
import com.dmdirc.addons.ui_swing.commands.PopInCommand;
import com.dmdirc.addons.ui_swing.commands.PopOutCommand;
import com.dmdirc.addons.ui_swing.commands.ServerSettings;
import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
import com.dmdirc.addons.ui_swing.framemanager.FrameManagerProvider;
import com.dmdirc.addons.ui_swing.injection.SwingModule;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.events.FeedbackNagEvent;
import com.dmdirc.events.FirstRunEvent;
import com.dmdirc.events.UnknownURLEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;
import com.dmdirc.ui.IconManager;
import com.dmdirc.updater.Version;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.layout.PlatformDefaults;

import dagger.ObjectGraph;

/**
 * Controls the main swing UI.
 */
public class SwingController extends BaseCommandPlugin implements UIController {

    /** Error dialog. */
    private ErrorListDialog errorDialog;
    /** This plugin's plugin info object. */
    private final PluginInfo pluginInfo;
    /** Global config manager. */
    private final AggregateConfigProvider globalConfig;
    /** Global config identity. */
    private final ConfigProvider globalIdentity;
    /** Addon config identity. */
    private final ConfigProvider addonIdentity;
    /** Global Swing UI Icon manager. */
    private final IconManager iconManager;
    /** Apple handler, deals with Mac specific code. */
    private final Apple apple;
    /** The manager we're using for dependencies. */
    private SwingManager swingManager;
    /** This plugin's settings domain. */
    private final String domain;
    /** Event bus to subscribe to events with. */
    private final EventBus eventBus;

    /**
     * Instantiates a new SwingController.
     *
     * @param pluginInfo      Plugin info
     * @param identityManager Identity Manager
     * @param serverManager   Server manager to use for server information.
     * @param urlBuilder      URL builder to use to resolve icons etc.
     * @param eventBus        The bus to publish and subscribe to events on.
     */
    public SwingController(
            final PluginInfo pluginInfo,
            final IdentityController identityManager,
            final ServerManager serverManager,
            final URLBuilder urlBuilder,
            final EventBus eventBus) {
        this.pluginInfo = pluginInfo;
        this.domain = pluginInfo.getDomain();
        this.eventBus = eventBus;

        globalConfig = identityManager.getGlobalConfiguration();
        globalIdentity = identityManager.getUserSettings();
        addonIdentity = identityManager.getAddonSettings();
        apple = new Apple(globalConfig, serverManager, eventBus);
        iconManager = new IconManager(globalConfig, urlBuilder);
        setAntiAlias();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new SwingModule(this, pluginInfo.getDomain())));
        getObjectGraph().validate();
        swingManager = getObjectGraph().get(SwingManager.class);

        registerCommand(ServerSettings.class, ServerSettings.INFO);
        registerCommand(ChannelSettings.class, ChannelSettings.INFO);
        registerCommand(Input.class, Input.INFO);
        registerCommand(PopOutCommand.class, PopOutCommand.INFO);
        registerCommand(PopInCommand.class, PopInCommand.INFO);
    }

    @Override
    public void onLoad() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Swing UI can't be run in a headless environment");
        }

        // Init the UI settings before we start any DI, as we might create frames etc.
        initUISettings();
        swingManager.load();

        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                getMainFrame().setVisible(true);
                errorDialog = new ErrorListDialog(getMainFrame(), getIconManager());
            }
        });

        addonIdentity.setOption("ui", "textPaneFontName",
                UIManager.getFont("TextPane.font").getFamily());
        addonIdentity.setOption("ui", "textPaneFontSize",
                UIManager.getFont("TextPane.font").getSize());

        eventBus.register(this);

        super.onLoad();
    }

    @Override
    public void onUnload() {
        swingManager.unload();

        errorDialog.dispose();
        eventBus.unregister(this);

        super.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        manager.getCategory("GUI").addSubCategory(
                new SwingPreferencesModel(pluginInfo, domain, globalConfig, globalIdentity)
                        .getSwingUICategory());
    }

    @Subscribe
    public void showFirstRunWizard(final FirstRunEvent event) {
        if (!event.isHandled()) {
            swingManager.getFirstRunExecutor().showWizardAndWait();
            event.setHandled(true);
        }
    }

    @Subscribe
    public void showURLDialog(final UnknownURLEvent event) {
        if (!event.isHandled()) {
            event.setHandled(true);
            UIUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    swingManager.getUrlDialogFactory().getURLDialog(event.getURI()).display();
                }
            });
        }
    }

    @Subscribe
    public void showFeedbackNag(final FeedbackNagEvent event) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                swingManager.getFeedbackNagProvider().get();
            }
        });
    }

    /**
     * Returns the version of this swing UI.
     *
     * @return Swing version
     */
    public Version getVersion() {
        return pluginInfo.getMetaData().getVersion();
    }

    /**
     * Returns the current look and feel.
     *
     * @return Current look and feel
     */
    public static String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    /**
     * Returns an instance of SwingController. This method is exported for use in other plugins.
     *
     * @return A reference to this SwingController.
     */
    @Exported
    public UIController getController() {
        return this;
    }

    /**
     * Returns the exported tree manager provider.
     *
     * @return A tree manager provider.
     */
    @Exported
    public FrameManagerProvider getTreeManager() {
        return swingManager.getTreeProvider();
    }

    /**
     * Returns the exported button manager provider.
     *
     * @return A button manager provider.
     */
    @Exported
    public FrameManagerProvider getButtonManager() {
        return swingManager.getButtonProvider();
    }

    /**
     * Retrieves the main frame to use.
     *
     * @return The main frame to use.
     *
     * @deprecated Should be injected where needed.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return swingManager.getMainFrame();
    }

    /**
     * @return Global config object.
     *
     * @deprecated Should be injected.
     */
    @Deprecated
    public AggregateConfigProvider getGlobalConfig() {
        return globalConfig;
    }

    /**
     * @return Global icon manager object.
     *
     * @deprecated Should be injected.
     */
    @Deprecated
    public IconManager getIconManager() {
        return iconManager;
    }

    /**
     * Shows the error dialog.
     *
     * @deprecated callers should use DI instead.
     */
    @Deprecated
    public void showErrorDialog() {
        errorDialog.display();
    }

    /**
     * Make swing not use Anti Aliasing if the user doesn't want it.
     */
    private void setAntiAlias() {
        // For this to work it *HAS* to be before anything else UI related.
        final boolean aaSetting = globalConfig.getOptionBool("ui", "antialias");
        System.setProperty("awt.useSystemAAFontSettings",
                Boolean.toString(aaSetting));
        System.setProperty("swing.aatext", Boolean.toString(aaSetting));
    }

    /**
     * Initialises the global UI settings for the Swing UI.
     */
    private void initUISettings() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                // This will do nothing on non OS X Systems
                if (Apple.isApple()) {
                    apple.setUISettings();
                    apple.setListener();
                }

                final Font defaultFont = new Font(Font.DIALOG, Font.TRUETYPE_FONT, 12);
                if (UIManager.getFont("TextField.font") == null) {
                    UIManager.put("TextField.font", defaultFont);
                }
                if (UIManager.getFont("TextPane.font") == null) {
                    UIManager.put("TextPane.font", defaultFont);
                }

                try {
                    UIUtilities.initUISettings();
                    UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(
                            globalConfig.getOption("ui", "lookandfeel")));
                    UIUtilities.setUIFont(new Font(globalConfig.getOption("ui", "textPaneFontName"),
                            Font.PLAIN, 12));
                } catch (UnsupportedOperationException | UnsupportedLookAndFeelException |
                        IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
                }

                if ("Metal".equals(UIManager.getLookAndFeel().getName())
                        || Apple.isAppleUI()) {
                    PlatformDefaults.setPlatform(PlatformDefaults.WINDOWS_XP);
                }
            }
        });
    }

}
