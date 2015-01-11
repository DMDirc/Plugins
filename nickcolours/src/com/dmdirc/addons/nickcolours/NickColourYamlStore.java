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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.BaseYamlStore;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.YamlReaderUtils.asMap;
import static com.dmdirc.util.YamlReaderUtils.requiredString;

@Singleton
public class NickColourYamlStore extends BaseYamlStore<NickColourEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(NickColourYamlStore.class);
    private final ColourManager colourManager;

    @Inject
    public NickColourYamlStore(@GlobalConfig final ColourManager colourManager) {
        this.colourManager = colourManager;
    }

    public Map<String, Color> readNickColourEntries(final Path path) {
        final Map<String, Color> nickColours = new HashMap<>();
        final Collection<NickColourEntry> nickColourEntries = read(path);
        nickColourEntries.stream().forEach(
                e -> nickColours.put(e.getNetwork() + ':' + e.getUser(), e.getColor())
        );
        return nickColours;
    }

    public void writeNickColourEntries(final Path path, final Map<String, Color> nickColours) {
        final Collection<NickColourEntry> nickColourEntries = new ArrayList<>();
        nickColours.forEach((description, colour) -> nickColourEntries.add(NickColourEntry
                .create(description.split(":")[0], description.split(":")[1], colour)));
        write(path, nickColourEntries);
    }

    @Override
    protected Optional<NickColourEntry> convertFromYaml(final Object object) {
        try {
            final Map<Object, Object> map = asMap(object);
            final String network = requiredString(map, "network");
            final String user = requiredString(map, "user");
            final String colour = requiredString(map, "colour");
            return Optional.of(NickColourEntry.create(network, user,
                    NickColourUtils.getColourFromString(colourManager, colour)));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read profile", ex);
            return Optional.empty();
        }
    }

    @Override
    protected Object convertToYaml(final NickColourEntry object) {
        final Map<Object, Object> map = new HashMap<>();
        map.put("network", object.getNetwork());
        map.put("user", object.getUser());
        map.put("colour", NickColourUtils.getStringFromColor(object.getColor()));
        return map;
    }
}
