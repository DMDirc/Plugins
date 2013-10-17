/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionFactory;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionSubstitutorFactory;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.addons.ui_swing.commands.ChannelSettings;
import com.dmdirc.addons.ui_swing.commands.Input;
import com.dmdirc.addons.ui_swing.commands.PopInCommand;
import com.dmdirc.addons.ui_swing.commands.PopOutCommand;
import com.dmdirc.addons.ui_swing.commands.ServerSettings;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.DialogManager;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.url.URLDialog;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.Version;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.Getter;

import net.miginfocom.layout.PlatformDefaults;

import dagger.ObjectGraph;

/**
 * Controls the main swing UI.
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class SwingController extends BaseCommandPlugin implements UIController {

    /** Top level window list. */
    private final List<java.awt.Window> windows;
    /** Error dialog. */
    private ErrorListDialog errorDialog;
    /** This plugin's plugin info object. */
    private final PluginInfo pluginInfo;
    /** Global config manager. */
    @Getter
    private final AggregateConfigProvider globalConfig;
    /** Server manager. */
    @Getter
    private final ServerManager serverManager;
    /** Identity Manager. */
    @Getter
    private final IdentityController identityManager;
    /** Identity factory. */
    @Getter
    private final IdentityFactory identityFactory;
    /** Global config identity. */
    @Getter
    private final ConfigProvider globalIdentity;
    /** Addon config identity. */
    private final ConfigProvider addonIdentity;
    /** Global Swing UI Icon manager. */
    @Getter
    private final IconManager iconManager;
    /** Prefs component factory instance. */
    @Getter
    private final PrefsComponentFactory prefsComponentFactory;
    /** Dialog manager. */
    @Getter
    private final DialogManager dialogManager;
    /** Action manager. */
    @Getter
    private final ActionManager actionManager;
    /** Action factory. */
    @Getter
    private final ActionFactory actionFactory;
    /** Plugin manager. */
    @Getter
    private final PluginManager pluginManager;
    /** Perform wrapper. */
    @Getter
    private final PerformWrapper performWrapper;
    /** Alias wrapper. */
    @Getter
    private final AliasWrapper aliasWrapper;
    /** Theme manager to use. */
    @Getter
    private final ThemeManager themeManager;
    /** Apple handler, deals with Mac specific code. */
    @Getter
    private final Apple apple;
    /** The colour manager to use to parse colours. */
    @Getter
    private final ColourManager colourManager;
    /** The URL builder to use. */
    @Getter
    private final URLBuilder urlBuilder;
    /** Factory to use to create action substitutors. */
    @Getter
    private final ActionSubstitutorFactory actionSubstitutorFactory;

    /** The manager we're using for dependencies. */
    private SwingManager swingManager;

    /**
     * Instantiates a new SwingController.
     *
     * @param pluginInfo Plugin info
     * @param identityManager Identity Manager
     * @param identityFactory Factory used to create identities.
     * @param pluginManager Plugin manager
     * @param actionManager Action manager
     * @param actionFactory The factory to use to create actions.
     * @param serverManager Server manager to use for server information.
     * @param performWrapper Perform wrapper to use for performs.
     * @param aliasWrapper Alias wrapper to use for aliases.
     * @param themeManager Theme manager to use.
     * @param urlBuilder URL builder to use to resolve icons etc.
     * @param colourManager The colour manager to use to parse colours.
     * @param actionSubstitutorFactory Factory to use to create action substitutors.
     */
    public SwingController(
            final PluginInfo pluginInfo,
            final IdentityController identityManager,
            final IdentityFactory identityFactory,
            final PluginManager pluginManager,
            final ActionManager actionManager,
            final ActionFactory actionFactory,
            final ServerManager serverManager,
            final PerformWrapper performWrapper,
            final AliasWrapper aliasWrapper,
            final ThemeManager themeManager,
            final URLBuilder urlBuilder,
            final ColourManager colourManager,
            final ActionSubstitutorFactory actionSubstitutorFactory) {
        this.pluginInfo = pluginInfo;
        this.identityManager = identityManager;
        this.identityFactory = identityFactory;
        this.actionManager = actionManager;
        this.actionFactory = actionFactory;
        this.pluginManager = pluginManager;
        this.serverManager = serverManager;
        this.performWrapper = performWrapper;
        this.aliasWrapper = aliasWrapper;
        this.themeManager = themeManager;
        this.colourManager = colourManager;
        this.urlBuilder = urlBuilder;
        this.actionSubstitutorFactory = actionSubstitutorFactory;

        globalConfig = identityManager.getGlobalConfiguration();
        globalIdentity = identityManager.getUserSettings();
        addonIdentity = identityManager.getAddonSettings();
        apple = new Apple(getGlobalConfig(), this);
        iconManager = new IconManager(globalConfig, urlBuilder);
        prefsComponentFactory = new PrefsComponentFactory(this);
        dialogManager = new DialogManager(this);
        setAntiAlias();
        windows = new ArrayList<>();
    }

    /**
     * Make swing not use Anti Aliasing if the user doesn't want it.
     */
    public final void setAntiAlias() {
        // For this to work it *HAS* to be before anything else UI related.
        final boolean aaSetting = getGlobalConfig()
                .getOptionBool("ui", "antialias");
        System.setProperty("awt.useSystemAAFontSettings",
                Boolean.toString(aaSetting));
        System.setProperty("swing.aatext", Boolean.toString(aaSetting));
    }

    /**
     * Does the main frame exist?
     *
     * @return true iif mainframe exists
     */
    protected boolean hasMainFrame() {
        return swingManager != null;
    }

    /** {@inheritDoc} */
    @Override
    public void showFirstRunWizard() {
        swingManager.getFirstRunExecutor().showWizardAndWait();
    }

    /** {@inheritDoc} */
    @Override
    public void showChannelSettingsDialog(final Channel channel) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                showDialog(ChannelSettingsDialog.class, channel,
                        getWindowFactory().getSwingWindow(channel));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showServerSettingsDialog(final Server server) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                showDialog(ServerSettingsDialog.class,
                        server, getWindowFactory().getSwingWindow(server));
            }
        });
    }

    /**
     * Proxy method to {@link DialogManager} that shows a dialog in the client.
     * For more details on what parameters might be required see
     * {@link DialogManager#getDialog(Class, Object...)}
     *
     * @param <T> Dialog type
     * @see DialogManager#getDialog(Class, Object...) getDialog
     *
     * @param klass The class of the dialog to show
     * @param params Any non standard parameters required
     */
    public <T extends StandardDialog> void showDialog(final Class<T> klass,
            final Object... params) {
        dialogManager.showDialog(klass, params);
    }

    /**
     * Updates the look and feel to the current config setting.
     */
    public void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(
                    getGlobalConfig().getOption("ui", "lookandfeel")));
            updateComponentTrees();
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to change Look and Feel: " + ex.getMessage());
        }
    }

    /**
     * Updates the component trees of all known windows in the Swing UI.
     */
    public void updateComponentTrees() {
        final int state = UIUtilities.invokeAndWait(
                new Callable<Integer>() {

                    /** {@inheritDoc} */
                    @Override
                    public Integer call() {
                        return getMainFrame().getExtendedState();
                    }
                });
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                SwingUtilities.updateComponentTreeUI(errorDialog);
            }
        });
        for (final java.awt.Window window : getTopLevelWindows()) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    SwingUtilities.updateComponentTreeUI(window);
                    if (window != getMainFrame()) {
                        window.pack();
                    }
                }
            });
        }
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                getMainFrame().setExtendedState(state);
            }
        });
    }

    /**
     * Initialises the global UI settings for the Swing UI.
     */
    private void initUISettings() {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void showURLDialog(final URI url) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                showDialog(URLDialog.class, url, getUrlHandler());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showFeedbackNag() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                new FeedbackNag(SwingController.this);
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

    /** {@inheritDoc} */
    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        // Init the UI settings before we start any DI, as we might create frames etc.
        initUISettings();

        setObjectGraph(graph.plus(new SwingModule(this)));
        getObjectGraph().validate();
        swingManager = getObjectGraph().get(SwingManager.class);

        registerCommand(ServerSettings.class, ServerSettings.INFO);
        registerCommand(ChannelSettings.class, ChannelSettings.INFO);
        registerCommand(Input.class, Input.INFO);
        registerCommand(PopOutCommand.class, PopOutCommand.INFO);
        registerCommand(PopInCommand.class, PopInCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Swing UI can't be run in a headless environment");
        }

        swingManager.load();

        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                getMainFrame().setVisible(true);
                errorDialog = new ErrorListDialog(SwingController.this);
            }
        });

        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        swingManager.unload();

        errorDialog.dispose();

        for (final java.awt.Window window : getTopLevelWindows()) {
            window.dispose();
        }
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        addonIdentity.setOption("ui", "textPaneFontName",
                UIManager.getFont("TextPane.font").getFamily());
        addonIdentity.setOption("ui", "textPaneFontSize",
                UIManager.getFont("TextPane.font").getSize());
    }

    /**
     * Returns the preferences dialog instance creating if required.
     *
     * @return Swing prefs dialog
     */
    public SwingPreferencesDialog getPrefsDialog() {
        return getDialogManager().getDialog(SwingPreferencesDialog.class);
    }

    /** {@inheritDoc} */
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

        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManager",
                "Treeview");
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBar",
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
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting("ui", "framemanager",
                "Window manager", "Which window manager should be used?",
                framemanagers,
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                "Window manager position", "Where should the window "
                + "manager be positioned?", fmpositions,
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane",
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "textPaneFontSize", "Textpane font size",
                "Font size for the textpane",
                globalConfig, globalIdentity));
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
                getDomain(), "mdiBarVisibility", "MDI Bar Visibility",
                "Controls the visibility of the MDI bar",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, "ui",
                "useOneTouchExpandable", "Use one touch expandable split "
                + "panes?", "Use one touch expandable arrows for "
                + "collapsing/expanding the split panes",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "windowMenuItems", "Window menu item count",
                "Number of items to show in the window menu",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.INTEGER, getDomain(),
                "windowMenuScrollInterval", "Window menu scroll interval",
                "Number of milliseconds to pause when autoscrolling in the "
                + "window menu",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(),
                "showtopicbar", "Show topic bar",
                "Shows a graphical topic bar in channels.",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(),
                "shownicklist", "Show nicklist?",
                "Do you want the nicklist visible",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "showfulltopic", "Show full topic in topic bar?",
               "Do you want to show the full topic in the topic bar or just"
               + "first line?",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "hideEmptyTopicBar", "Hide empty topic bar?",
                "Do you want to hide the topic bar when there is no topic",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "textpanelinenotification",
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
                getDomain(), "showtreeexpands", "Show expand/collapse handles",
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
     * Adds a top level window to the window list.
     *
     * @param source New window
     */
    protected void addTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.add(source);
        }
    }

    /**
     * Deletes a top level window to the window list.
     *
     * @param source Old window
     */
    protected void delTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.remove(source);
        }
    }

    /**
     * Returns a list of top level windows.
     *
     * @return Top level window list
     */
    public List<java.awt.Window> getTopLevelWindows() {
        synchronized (windows) {
            return new ArrayList<>(windows);
        }
    }

    /**
     * Returns an instance of SwingController. This method is exported for use
     * in other plugins.
     *
     * @return A reference to this SwingController.
     */
    @Exported
    public UIController getController() {
        return this;
    }

    /**
     * Adds the specified menu item to the named parent menu, creating the
     * parent menu if required.
     *
     * @param parentMenu Parent menu name
     * @param menuItem Menu item to add
     */
    public void addMenuItem(final String parentMenu, final JMenuItem menuItem) {
        getMainFrame().getJMenuBar().addMenuItem(parentMenu, menuItem);
    }

    /** {@inheritDoc} */
    @Override
    public void requestWindowFocus(final Window window) {
        if (window instanceof TextFrame) {
            getMainFrame().setActiveFrame((TextFrame) window);
        }
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
     * Retrieves the command controller to use.
     *
     * @return The command controller to use.
     * @deprecated Should be injected where needed.
     */
    @Override
    @Deprecated
    public CommandController getCommandController() {
        return super.getCommandController();
    }

    /**
     * Retrieves the window factory to use.
     *
     * @return The window factory to use.
     * @deprecated Should be injected where needed.
     */
    @Deprecated
    public SwingWindowFactory getWindowFactory() {
        return swingManager.getWindowFactory();
    }

    /**
     * Retrieves the main frame to use.
     *
     * @return The main frame to use.
     * @deprecated Should be injected where needed.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return swingManager.getMainFrame();
    }

    /**
     * Retrieves the status bar that's in use.
     *
     * @return The status bar that's in use.
     * @deprecated Should be injected where needed.
     */
    @Deprecated
    public SwingStatusBar getSwingStatusBar() {
        return swingManager.getMainFrame().getStatusBar();
    }

    /**
     * Gets the URL handler to use.
     *
     * @return The URL handler to use.
     * @deprecated Should be injected.
     */
    @Deprecated
    public URLHandler getUrlHandler() {
        return swingManager.getUrlHandler();
    }

}
