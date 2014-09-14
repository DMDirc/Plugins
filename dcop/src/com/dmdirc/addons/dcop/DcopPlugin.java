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

package com.dmdirc.addons.dcop;

import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Allows the user to execute dcop commands (and read the results).
 */
public final class DcopPlugin extends BaseCommandPlugin {

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new DcopModule()));
        registerCommand(DcopCommand.class, DcopCommand.INFO);
    }

    /**
     * Retrieves the result from executing the specified command.
     *
     * @param command The command to be executed
     *
     * @return The output of the specified command
     *
     * @deprecated Use a {@link DcopExecutor}.
     */
    @Exported
    @Deprecated
    public static List<String> getDcopResult(final String command) {
        final List<String> result = new ArrayList<>();

        try {
            final Process process = Runtime.getRuntime().exec(command);

            try (InputStreamReader reader = new InputStreamReader(process.getInputStream());
                    BufferedReader input = new BufferedReader(reader)) {
                String line;

                while ((line = input.readLine()) != null) {
                    result.add(line);
                }
            }

            process.destroy();
        } catch (IOException ex) {
            // Do nothing
        }

        return result;
    }

}
