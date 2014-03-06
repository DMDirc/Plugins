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
import com.dmdirc.addons.ui_swing.wizard.SwingWindowManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
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
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.layout.PlatformDefaults;

import org.slf4j.LoggerFactory;

import dagger.ObjectGraph;


/**
 * Controls the main swing UI.
 */
public class SwingController extends BaseCommandPlugin implements UIController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MainFrame.class);
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
    /** Swing window manager. */
    private SwingWindowManager swingWindowManager;

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

    @Deprecated
    public AggregateConfigProvider getGlobalConfig() {
        return globalConfig;
    }

    @Deprecated
    public IconManager getIconManager() {
        return iconManager;
    }

    /**
     * Make swing not use Anti Aliasing if the user doesn't want it.
     */
    public final void setAntiAlias() {
        // For this to work it *HAS* to be before anything else UI related.
        final boolean aaSetting = globalConfig.getOptionBool("ui", "antialias");
        System.setProperty("awt.useSystemAAFontSettings",
                Boolean.toString(aaSetting));
        System.setProperty("swing.aatext", Boolean.toString(aaSetting));
    }

    @Subscribe
    public void showFirstRunWizard(final FirstRunEvent event) {
        if (!event.isHandled()) {
            swingManager.getFirstRunExecutor().showWizardAndWait();
            event.setHandled(true);
        }
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
                            getGlobalConfig().getOption("ui", "lookandfeel")));
                    UIUtilities.setUIFont(new Font(getGlobalConfig()
                            .getOption("ui", "textPaneFontName"), Font.PLAIN, 12));
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
     * Shows the error dialog.
     */
    public void showErrorDialog() {
        errorDialog.display();
    }

    /**
     * Returns the current look and feel.
     *
     * @return Current look and feel
     */
    public static String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        // Init the UI settings before we start any DI, as we might create frames etc.
        initUISettings();

        setObjectGraph(graph.plus(new SwingModule(this, pluginInfo.getDomain())));
        getObjectGraph().validate();
        swingManager = getObjectGraph().get(SwingManager.class);
        swingWindowManager = getObjectGraph().get(SwingWindowManager.class);

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

        for (final java.awt.Window window : swingWindowManager.getTopLevelWindows()) {
            window.dispose();
        }
        super.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        manager.getCategory("GUI").addSubCategory(createGeneralCategory());
    }

    /**
     * Creates the "Advanced" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createGeneralCategory() {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Swing UI", "These config options apply "
                + "only to the swing UI.", "category-gui");

        final Map<String, String> lafs = new HashMap<>();
        final Map<String, String> framemanagers = new HashMap<>();
        final Map<String, String> fmpositions = new HashMap<>();

        // TODO: When we can inject nicely, use a service locator to find all implementations.
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManagerProvider",
                "Treeview");
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBarProvider",
                "Button bar");

        fmpositions.put("top", "Top");
        fmpositions.put("bottom", "Bottom");
        fmpositions.put("left", "Left");
        fmpositions.put("right", "Right");

        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();

        lafs.put("Native", "Native");
        for (final LookAndFeelInfo laf : plaf) {
            lafs.put(laf.getName(), laf.getName());
        }

        general.addSetting(new PreferencesSetting("ui", "lookandfeel",
                "Look and feel", "The Java look and feel to use", lafs,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting("ui", "framemanager",
                "Window manager", "Which window manager should be used?",
                framemanagers,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                "Window manager position", "Where should the window "
                + "manager be positioned?", fmpositions,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane",
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "textPaneFontSize", "Textpane font size",
                "Font size for the textpane",
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortrootwindows", "Sort root windows",
                "Sort child windows in the frame managers?",
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortchildwindows", "Sort child windows",
                "Sort root windows in the frame managers?",
                globalConfig, globalIdentity));

        general.addSubCategory(createNicklistCategory());
        general.addSubCategory(createTreeViewCategory());
        general.addSubCategory(createAdvancedCategory());

        return general;
    }

    /**
     * Creates the "Advanced" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createAdvancedCategory() {
        final PreferencesCategory advanced = new PluginPreferencesCategory(
                pluginInfo, "Advanced", "");

        advanced.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER,
                new OptionalValidator(new NumericalValidator(10, -1)),
                "ui", "frameBufferSize",
                "Window buffer size", "The maximum number of lines in a "
                + "window buffer", globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "mdiBarVisibility", "MDI Bar Visibility",
                "Controls the visibility of the MDI bar",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, "ui",
                        "useOneTouchExpandable", "Use one touch expandable split "
                        + "panes?", "Use one touch expandable arrows for "
                        + "collapsing/expanding the split panes",
                        globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                domain, "windowMenuItems", "Window menu item count",
                "Number of items to show in the window menu",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.INTEGER, domain,
                        "windowMenuScrollInterval", "Window menu scroll interval",
                        "Number of milliseconds to pause when autoscrolling in the "
                        + "window menu",
                        globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, domain,
                        "showtopicbar", "Show topic bar",
                        "Shows a graphical topic bar in channels.",
                        globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain,
                "shownicklist", "Show nicklist?",
                "Do you want the nicklist visible",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "showfulltopic", "Show full topic in topic bar?",
                "Do you want to show the full topic in the topic bar or just"
                + "first line?",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "hideEmptyTopicBar", "Hide empty topic bar?",
                "Do you want to hide the topic bar when there is no topic",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "textpanelinenotification",
                "New line notification", "Do you want to be notified about new "
                + "lines whilst scrolled up?",
                globalConfig, globalIdentity));

        return advanced;
    }

    /**
     * Creates the "Treeview" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createTreeViewCategory() {
        final PreferencesCategory treeview = new PluginPreferencesCategory(
                pluginInfo, "Treeview", "", "treeview");

        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "backgroundcolour", "Treeview background colour",
                "Background colour to use for the treeview",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "foregroundcolour", "Treeview foreground colour",
                "Foreground colour to use for the treeview",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewRolloverColour", "Treeview rollover colour",
                "Background colour to use when the mouse cursor is over a "
                + "node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "treeviewActiveBold", "Active node bold",
                "Make the active node bold?",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveBackground", "Active node background",
                "Background colour to use for active treeview node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveForeground", "Active node foreground",
                "Foreground colour to use for active treeview node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "showtreeexpands", "Show expand/collapse handles",
                "Do you want to show tree view collapse/expand handles",
                globalConfig, globalIdentity));

        return treeview;
    }

    /**
     * Creates the "Nicklist" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createNicklistCategory() {
        final PreferencesCategory nicklist = new PluginPreferencesCategory(
                pluginInfo, "Nicklist", "", "nicklist");

        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                "Background colour to use for the nicklist",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nickListAltBackgroundColour",
                "Alternate background colour",
                "Background colour to use for every other nicklist entry",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByMode", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByCase", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?",
                globalConfig, globalIdentity));

        return nicklist;
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
     * Returns the version of this swing UI.
     *
     * @return Swing version
     */
    public Version getVersion() {
        return pluginInfo.getMetaData().getVersion();
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

    @Exported
    public FrameManagerProvider getTreeManager() {
        return swingManager.getTreeProvider();
    }

    @Exported
    public FrameManagerProvider getButtonManager() {
        return swingManager.getButtonProvider();
    }

}
