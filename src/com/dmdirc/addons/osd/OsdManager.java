package com.dmdirc.addons.osd;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.messages.Styliser;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simon
 */
public class OsdManager {

    private final OsdPlugin plugin;
    private List<OsdWindow> windowList = new ArrayList<OsdWindow>();
    private static final int WINDOW_GAP = 5;
    
    public OsdManager(OsdPlugin plugin) {
        //Constructor
        this.plugin = plugin;
    }

    public void createOSDWindow(final String title, final String message) {
        //Check some form of queue.
        System.out.println("Current Displayed: " + getWindowCount());
        OsdWindow currentWindow = new OsdWindow(Styliser.stipControlCodes(message),
                false, plugin, this);

        windowList.add(currentWindow);
    }

    public void destroyOSDWindow(OsdWindow window) {
        windowList.remove(window);
    }

    public void destroyAllOSDWindows() {
        for (OsdWindow window : new ArrayList<OsdWindow>(windowList)) {
            window.setVisible(false);
            destroyOSDWindow(window);
        }
    }

    public List<OsdWindow> getWindowList() {
        return new ArrayList<OsdWindow>(windowList);
    }

    public int getWindowCount() {
        return windowList.size();
    }

    public int getYPosition() {
        final String policy = IdentityManager.getGlobalConfig()
                .getOption(plugin.getDomain(), "newbehaviour");
        int y = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "locationY");

        if ("down".equals(policy)) {
            // Place our new window below old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                if (window.isVisible()) {
                    y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                }
            }
        } else if ("up".equals(policy)) {
            // Place our new window above old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                if (window.isVisible()) {
                    y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                }
            }
        } else if ("close".equals(policy)) {
            // Close existing windows and use their place
            destroyAllOSDWindows();
        }

        return y;
    }
}