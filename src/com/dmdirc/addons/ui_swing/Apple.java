/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.commandparser.commands.global.NewServer;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

/**
 * Integrate DMDirc with OS X better.
 */
public final class Apple implements InvocationHandler, ActionListener {
    /** Store any addresses that are opened before CLIENT_OPENED. */
    private final List<URI> addresses = new ArrayList<URI>();

    /** Config manager used to read settings. */
    private final ConfigManager configManager;

    /** The "Application" object used to do stuff on OS X. */
    private Object application;

    /** Are we listening? */
    private boolean isListener = false;

    /** The MenuBar for the application. */
    private MenuBar menuBar = null;

    /** Has the CLIENT_OPENED action been called? */
    private boolean clientOpened = false;

    /** Our swing controller. */
    private final SwingController controller;

    /**
     * Create the Apple class.
     * <p>
     * This attempts to:
     * </p>
     *
     * <ul>
     * <li>load the JNI library</li>
     * <li>register the callback</li>
     * <li>register a CLIENT_OPENED listener</li>
     * </ul>
     *
     * @param configManager Config manager
     */
    public Apple(final ConfigManager configManager, final SwingController controller) {
        this.configManager = configManager;
        this.controller = controller;
        if (isApple()) {
            try {
                System.loadLibrary("DMDirc-Apple"); // NOPMD
                registerOpenURLCallback();
                ActionManager.getActionManager().registerListener(this, CoreActionType.CLIENT_OPENED);
            } catch (UnsatisfiedLinkError ule) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to load JNI library.", ule);
            }
        }
    }

    /**
     * Register the getURL Callback.
     *
     * @return 0 on success, 1 on failure.
     */
    private synchronized native int registerOpenURLCallback();


    /**
     * Call a method on the given object.
     *
     * @param obj Object to call method on.
     * @oaram className Name of class that object really is.
     * @param methodName Method to call
     * @param classes Array of classes to pass when calling getMethod
     * @param objects Array of objects to pass when invoking.
     * @return Output from method.invoke()
     */
    private Object reflectMethod(final Object obj, final String className, final String methodName, final Class[] classes, final Object[] objects) {
        try {
            final Class<?> clazz = className == null ? obj.getClass() : Class.forName(className);
            final Method method = clazz.getMethod(methodName, classes == null ? new Class[0] : classes);
            return method.invoke(obj, objects == null ? new Object[0] : objects);
        } catch (IllegalArgumentException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to find OS X classes");
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to find OS X classes");
        } catch (final ClassNotFoundException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to find OS X classes");
        } catch (final NoSuchMethodException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to find OS X classes");
        } catch (final IllegalAccessException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to find OS X classes");
        }

        return null;
    }

    /**
     * Handle a method call to the apple Application class.
     *
     * @param methodName Method to call
     * @param classes Array of classes to pass when calling getMethod
     * @param objects Array of objects to pass when invoking.
     * @return Output from method.invoke()
     */
    private Object doAppleMethod(final String methodName, final Class[] classes, final Object[] objects) {
        if (!isApple()) { return null; }

        return reflectMethod(getApplication(), null, methodName, classes, objects);
    }

    /**
     * Get the "Application" object.
     *
     * @return Object that on OSX will be an "Application"
     */
    public Object getApplication() {
        synchronized (Apple.class) {
            if (isApple() && application == null) {
                application = reflectMethod(null, "com.apple.eawt.Application", "getApplication", null, null);
            }
            return application;
        }
    }

    /**
     * Are we on OS X?
     *
     * @return true if we are running on OS X
     */
    public static boolean isApple() {
        return (System.getProperty("mrj.version") != null);
    }

    /**
     * Are we using the OS X look and feel?
     *
     * @return true if we are using the OS X look and feel
     */
    public static boolean isAppleUI() {
        final String name = UIManager.getLookAndFeel().getClass().getName();
        return isApple() && (name.equals("apple.laf.AquaLookAndFeel") || name.equals("com.apple.laf.AquaLookAndFeel"));
    }

    /**
     * Set some OS X only UI settings.
     */
    public void setUISettings() {
        if (!isApple()) {
            return;
        }

        // Set some Apple OS X related stuff from http://tinyurl.com/6xwuld
        final String aaText = configManager.getOptionBool("ui", "antialias") ? "on" : "off";

        System.setProperty("apple.awt.antialiasing", aaText);
        System.setProperty("apple.awt.textantialiasing", aaText);
        System.setProperty("apple.awt.showGrowBox", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DMDirc");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("com.apple.mrj.application.live-resize", "true");
    }

    /**
     * Requests this application to move to the foreground.
     *
     * @param allWindows if all windows of this application should be moved to
     *                   the foreground, or only the foremost one
     */
    public void requestForeground(final boolean allWindows) {
        doAppleMethod("requestForeground", new Class[]{Boolean.TYPE}, new Object[]{allWindows});
    }

    /**
     * Requests user attention to this application (usually through bouncing
     * the Dock icon). Critical requests will continue to bounce the Dock icon
     * until the app is activated. An already active application requesting
     * attention does nothing.
     *
     * @param isCritical If this is false, the dock icon only bounces once,
     *            otherwise it will bounce until clicked on.
     */
    public void requestUserAttention(final boolean isCritical) {
        doAppleMethod("requestUserAttention", new Class[]{Boolean.TYPE}, new Object[]{isCritical});
    }

    /**
     * Attaches the contents of the provided PopupMenu to the application's Dock icon.
     *
     * @param menu the PopupMenu to attach to this application's Dock icon
     */
    public void setDockMenu(final PopupMenu menu) {
        doAppleMethod("setDockMenu", new Class[]{PopupMenu.class}, new Object[]{menu});
    }

    /**
     * Get the PopupMenu attached to the application's Dock icon.
     *
     * @return the PopupMenu attached to this application's Dock icon
     */
    public PopupMenu getDockMenu() {
        final Object result = doAppleMethod("getDockMenu", null, null);
        return (result instanceof PopupMenu) ? (PopupMenu)result : null;
    }

    /**
     * Changes this application's Dock icon to the provided image.
     *
     * @param image The image to use
     */
    public void setDockIconImage(final Image image) {
        doAppleMethod("setDockIconImage", new Class[]{Image.class}, new Object[]{image});
    }

    /**
     * Obtains an image of this application's Dock icon.
     *
     * @return The application's dock icon.
     */
    public Image getDockIconImage() {
        final Object result = doAppleMethod("getDockIconImage", null, null);
        return (result instanceof Image) ? (Image)result : null;
    }

    /**
     * Affixes a small system provided badge to this application's Dock icon.
     * Usually a number.
     *
     * @param badge textual label to affix to the Dock icon
     */
    public void setDockIconBadge(final String badge) {
        doAppleMethod("setDockIconBadge", new Class[]{String.class}, new Object[]{badge});
    }

    /**
     * Sets the default menu bar to use when there are no active frames.
     * Only used when the system property "apple.laf.useScreenMenuBar" is
     * "true", and the Aqua Look and Feel is active.
     *
     * @param menuBar to use when no other frames are active
     */
    public void setDefaultMenuBar(final JMenuBar menuBar) {
        doAppleMethod("setDefaultMenuBar", new Class[]{JMenuBar.class}, new Object[]{menuBar});
    }

    /**
     * Add this application as a handler for the given event.
     *
     * @param handlerClass Class used as the handler.
     * @param handlerMethod Method used to set the handler.
     * @return True if we succeeded.
     */
    private boolean addHandler(final String handlerClass, final String handlerMethod) {
        try {
            final Class<?> listenerClass = Class.forName(handlerClass);
            final Object listener = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{listenerClass}, this);

            final Method method = getApplication().getClass().getMethod(handlerMethod, new Class[]{listenerClass});
            method.invoke(getApplication(), listener);

            return true;
        } catch (final ClassNotFoundException ex) {
            return false;
        } catch (final NoSuchMethodException ex) {
            return false;
        } catch (final IllegalAccessException ex) {
            return false;
        } catch (final InvocationTargetException ex) {
            return false;
        }
    }

    /**
     * Set this up as a listener for the Apple Events.
     *
     * @return True if the listener was added, else false.
     */
    public boolean setListener() {
        if (!isApple() || isListener) {
            return false;
        }

        addHandler("com.apple.eawt.OpenURIHandler", "setOpenURIHandler");
        addHandler("com.apple.eawt.AboutHandler", "setAboutHandler");
        addHandler("com.apple.eawt.QuitHandler", "setQuitHandler");
        addHandler("com.apple.eawt.PreferencesHandler", "setPreferencesHandler");
        isListener = true;

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws Throwable Throws stuff on errors
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isApple()) {
            return null;
        }

        try {
            final Class[] classes = new Class[args.length];

            for (int i = 0; i < args.length; i++) {
                if (EventObject.class.isInstance(args[i])) {
                    classes[i] = EventObject.class;
                } else {
                    final Class c = args[i].getClass();
                    if (c.getCanonicalName().equals("com.apple.eawt.QuitResponse")) {
                        classes[i] = Object.class;
                    } else {
                        classes[i] = c;
                    }
                }
            }

            final Method thisMethod = this.getClass().getMethod(method.getName(), classes);
            return thisMethod.invoke(this, args);
        } catch (final NoSuchMethodException e) {
            if (method.getName().equals("equals") && args.length == 1) {
                return Boolean.valueOf(proxy == args[0]);
            }
        }

        return null;
    }

    /**
     * Set the MenuBar.
     * This will unset all menu mnemonics aswell if on the OSX ui.
     *
     * @param newMenuBar MenuBar to use to send events to,
     */
    public void setMenuBar(final MenuBar newMenuBar) {
        menuBar = newMenuBar;
        if (!isAppleUI()) {
            return;
        }
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            final JMenu menu = menuBar.getMenu(i);
            if (menu == null) {
                continue;
            }
            menu.setMnemonic(0);
            for (int j = 0; j < menu.getItemCount(); j++) {
                final JMenuItem menuItem = menu.getItem(j);
                if (menuItem != null) {
                    menuItem.setMnemonic(0);
                }
            }
        }
    }

    /**
     * Handle an event using the menuBar.
     *
     * @param name The name of the event according to the menubar
     */
    public void handleMenuBarEvent(final String name) {
        if (!isApple() || menuBar == null) {
            return;
        }
        final ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name);

        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            final JMenu menu = menuBar.getMenu(i);
            if (menu instanceof java.awt.event.ActionListener) {
                ((java.awt.event.ActionListener)menu).actionPerformed(actionEvent);
            }
        }
    }

    /**
     * This is called when About is selected from the Application menu.
     *
     * @param event an ApplicationEvent object
     */
    public void handleAbout(final EventObject event) {
        handleMenuBarEvent("About");
    }

    /**
     * This is called when Preferences is selected from the Application menu.
     *
     * @param event an ApplicationEvent object
     */
    public void handlePreferences(final EventObject event) {
        handleMenuBarEvent("Preferences");
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (type == CoreActionType.CLIENT_OPENED) {
            synchronized (addresses) {
                clientOpened = true;
                for (final URI addr : addresses) {
                    controller.getServerManager().connectToAddress(addr);
                }
                addresses.clear();
            }
        }
    }

    /**
     * This is called when Quit is selected from the Application menu.
     *
     * @param event an ApplicationEvent object
     * @param quitResponse QuitResponse object.
     */
    public void handleQuitRequestWith(final EventObject event, final Object quitResponse) {
        // Technically we should tell OS X if the quit succeeds or not, but we
        // have no way of knowing the result just yet.
        //
        // So instead we will just tell it that the quit was cancelled every
        // time, and then just quit anyway if we need to.
        reflectMethod(quitResponse, null, "cancelQuit", null, null);

        handleMenuBarEvent("Exit");
    }

    /**
     * Callback from our JNI library.
     * This should work when not launcher via JavaApplicationStub
     *
     * @param url The irc url string to connect to.
     */
    public void handleOpenURL(final String url) {
        try {
            final URI addr = NewServer.getURI(url);
            handleURI(addr);
        } catch (final URISyntaxException use) { }
    }

    /**
     * Callback from OSX Directly.
     * This will work if we were launched using the JavaApplicationStub
     *
     * @param event Event related to this callback. This event will have a
     *              reflectable getURI method to get a URI.
     */
    public void openURI(final EventObject event) {
        if (!isApple()) {
            return;
        }

        final Object obj = reflectMethod(event, null, "getURI", null, null);
        if (obj instanceof URI) {
            final URI uri = (URI)obj;
            handleURI(uri);
        }
    }

    /**
     * Handle connecting to a URI.
     *
     * If called before the client has finished opening, the URI will be added
     * to a list that will be connected to once the CLIENT_OPENED action is
     * called. Otherwise we connect right away.
     *
     * @param uri URI to connect to.
     */
    private void handleURI(final URI uri) {
        synchronized (addresses) {
            if (clientOpened) {
                // When the JNI callback is called there is no
                // ContextClassLoader set, which causes an NPE in
                // IconManager if no servers have been connected to yet.
                if (Thread.currentThread().getContextClassLoader() == null) {
                    Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
                }
                controller.getServerManager().connectToAddress(uri);
            } else {
                addresses.add(uri);
            }
        }
    }
}
