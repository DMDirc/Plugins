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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.util.URLBuilder;

import java.awt.Image;
import java.util.Map;

import javax.swing.ImageIcon;

import lombok.Getter;

/**
 * Describes an addon.
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class AddonInfo {
    /** Addon site ID. */
    @Getter
    private final int id;
    /**
     * Stable download name. This should be prepended with
     * http://addons.dmdirc.com/addondownload/ to get a full URL.
     */
    @Getter
    private final String stableDownload;
    /**
     * Stable download name. This should be prepended with
     * http://addons.dmdirc.com/addondownload/ to get a full URL.
     */
    @Getter
    private final String unstableDownload;
    /**
     * Stable download name. This should be prepended with
     * http://addons.dmdirc.com/addondownload/ to get a full URL.
     */
    @Getter
    private final String nightlyDownload;
    /** Addon title. */
    @Getter
    private final String title;
    /** Addon author and email. */
    @Getter
    private final String author;
    /** Addon rating from 0-10. */
    @Getter
    private final int rating;
    /** Full text description. */
    @Getter
    private final String description;
    /** Addon type, {@link ActionType}. */
    @Getter
    private final AddonType type;
    /** Has this addon been verified by the developers? */
    @Getter
    private final boolean verified;
    /** Date this addon was updated. */
    @Getter
    private final int date;
    /** Screenshot image. */
    @Getter
    private final ImageIcon screenshot;
    /** Current client update channel. */
    @Getter
    private UpdateChannel channel;

    /**
     * Creates a new addon info class with the specified entries.
     *
     * @param configManager The config provider to use to find settings.
     * @param urlBuilder The URL builder to use to retrieve image URLs.
     * @param entry List of entries
     */
    public AddonInfo(
            final AggregateConfigProvider configManager,
            final URLBuilder urlBuilder,
            final Map<String, String> entry) {
        id = Integer.parseInt(entry.get("id"));
        title = entry.get("title");
        author = entry.get("user");
        rating = Integer.parseInt(entry.get("rating"));
        type = entry.get("type").equals("plugin") ?
                AddonType.TYPE_PLUGIN : entry.get("type").equals("theme") ?
                        AddonType.TYPE_THEME : AddonType.TYPE_ACTION_PACK;
        stableDownload = entry.containsKey("stable") ? entry.get("stable") : "";
        unstableDownload = entry.containsKey("unstable") ? entry
                .get("unstable") : "";
        nightlyDownload = entry.containsKey("nightly") ? entry.get("nightly")
                : "";
        description = entry.get("description");
        verified = entry.get("verified").equals("yes");
        date = Integer.parseInt(entry.get("date"));
        if (entry.get("screenshot").equals("yes")) {
            screenshot = new ImageIcon(urlBuilder.getUrl(
                    "http://addons.dmdirc.com/addonimg/" + id));
            screenshot.setImage(screenshot.getImage().
                    getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        } else {
            screenshot = new ImageIcon(urlBuilder.getUrl(
                    "dmdirc://com/dmdirc/res/logo.png"));
        }
        try {
            channel = UpdateChannel.valueOf(configManager.getOption(
                    "updater", "channel"));
        } catch (final IllegalArgumentException ex) {
            channel = UpdateChannel.NONE;
        }
    }

    /**
     * Is the plugin installed?
     *
     * @return true iff installed
     */
    public boolean isInstalled() {
        for (UpdateComponent comp : UpdateChecker.getManager().getComponents()) {
            if (comp.getName().equals("addon-" + getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is the plugin downloadable?
     *
     * @return true iff the plugin is downloadable
     */
    public boolean isDownloadable() {
        return !getDownload().isEmpty();
    }

    /**
     * Returns the download location for this addoninfo, or an empty string.
     *
     * @return Download location or empty string
     */
    @SuppressWarnings("fallthrough")
    public String getDownload() {
        switch (channel) { // NOPMD
            case NONE:
                // fallthrough
            case NIGHTLY:
                if (!nightlyDownload.isEmpty()) {
                    return nightlyDownload;
                }
                // fallthrough
            case UNSTABLE:
                if (!unstableDownload.isEmpty()) {
                    return unstableDownload;
                }
                // fallthrough
            case STABLE:
                if (!stableDownload.isEmpty()) {
                    return stableDownload;
                }
                return "";
            default:
                return "";
        }
    }

    /**
     * Checks if the text matches this plugin
     *
     * @param text Comparison addon text.
     *
     * @return true iff the plugin matches
     */
    public boolean matches(final String text) {
        return title.toLowerCase().indexOf(text.toLowerCase()) > -1
                || description.toLowerCase().indexOf(text.toLowerCase()) > -1;
    }

}
