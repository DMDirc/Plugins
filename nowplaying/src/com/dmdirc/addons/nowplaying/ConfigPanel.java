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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ListReorderButtonPanel;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Now playing plugin config panel.
 */
public class ConfigPanel extends JPanel implements PreferencesInterface, KeyListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Now playing manager to get and handle sources. */
    private final NowPlayingManager manager;
    /** Global configuration to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** User settings to write settings to. */
    private final ConfigProvider userSettings;
    /** This plugin's settings domain. */
    private final String domain;
    /** Media sources. */
    private final List<String> sources;
    /** Media source order list. */
    private ReorderableJList<String> list;
    /** Text field for our setting. */
    private JTextField textfield;
    /** Panel that the preview is in. */
    private JPanel previewPanel;
    /** Label for previews. */
    private TextLabel preview;
    /** Update timer. */
    private Timer updateTimer;

    /**
     * Creates a new instance of ConfigPanel.
     *
     * @param manager      Now playing manager to get and handle sources
     * @param globalConfig Global config to read from
     * @param userSettings Config to write settings to
     * @param domain       This plugin's settings domain
     * @param sources      A list of sources to be used in the panel
     */
    public ConfigPanel(
            final NowPlayingManager manager,
            final AggregateConfigProvider globalConfig,
            final ConfigProvider userSettings,
            final String domain,
            final List<String> sources) {
        this.manager = manager;
        this.globalConfig = globalConfig;
        this.userSettings = userSettings;
        this.domain = domain;
        this.sources = new LinkedList<>();
        this.sources.addAll(checkNotNull(sources));

        initComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        list = new ReorderableJList<>();

        for (String source : sources) {
            list.getModel().addElement(source);
        }

        textfield = new JTextField(globalConfig.getOption(domain, "format"));
        textfield.addKeyListener(this);
        preview = new TextLabel("Preview:\n");

        setLayout(new MigLayout("fillx, ins 0"));

        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Source order"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        panel.add(new JLabel("Drag and drop items to reorder"), "wrap");
        panel.add(new JScrollPane(list), "growx, pushx");
        panel.add(new ListReorderButtonPanel<>(list), "");

        add(panel, "growx, wrap");

        panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Output format"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        panel.add(textfield, "span, growx, wrap");
        panel.add(preview, "span, grow, wrap, gaptop 10");
        add(panel, "growx, wrap");

        previewPanel = panel;

        add(new NowPlayingSubsitutionPanel(Arrays.asList("app", "title", "artist", "album",
                "bitrate", "format", "length", "time", "state")), "growx");
        schedulePreviewUpdate();
    }

    /**
     * Updates the preview text.
     */
    private void updatePreview() {
        updateTimer.cancel();

        MediaSource source = manager.getBestSource();

        if (source == null) {
            source = new DummyMediaSource();
        }

        final String text = manager.doSubstitution(
                UIUtilities.invokeAndWait(new Callable<String>() {
                    @Override
                    public String call() {
                        return textfield.getText();
                    }
                }), source);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                preview.setText("Preview:\n" + text);
                preview.repaint();
                previewPanel.revalidate();
                revalidate();
            }
        });
    }

    /**
     * Retrieves the (new) source order from this config panel.
     *
     * @return An ordered list of sources
     */
    public List<String> getSources() {
        final List<String> newSources = new LinkedList<>();

        final Enumeration<String> values = list.getModel().elements();

        while (values.hasMoreElements()) {
            newSources.add(values.nextElement());
        }

        return newSources;
    }

    @Override
    public void save() {
        userSettings.setOption(domain, "sourceOrder", getSources());
        userSettings.setOption(domain, "format", textfield.getText());
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        schedulePreviewUpdate();
    }

    /**
     * Schedules an update to the preview text.
     */
    private void schedulePreviewUpdate() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        updateTimer = new Timer("Nowplaying config timer");
        updateTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    updatePreview();
                } catch (LinkageError | Exception e) {
                    preview.setText("Error updating preview: " + e.getMessage());
                }
            }
        }, 500);
    }

    /**
     * A dummy media source for use in previews.
     */
    private static class DummyMediaSource implements MediaSource {

        @Override
        public MediaSourceState getState() {
            return MediaSourceState.PLAYING;
        }

        @Override
        public String getAppName() {
            return "MyProgram";
        }

        @Override
        public String getArtist() {
            return "The Artist";
        }

        @Override
        public String getTitle() {
            return "Song about nothing";
        }

        @Override
        public String getAlbum() {
            return "Album 45";
        }

        @Override
        public String getLength() {
            return "3:45";
        }

        @Override
        public String getTime() {
            return "1:20";
        }

        @Override
        public String getFormat() {
            return "flac";
        }

        @Override
        public String getBitrate() {
            return "128";
        }

    }

}
