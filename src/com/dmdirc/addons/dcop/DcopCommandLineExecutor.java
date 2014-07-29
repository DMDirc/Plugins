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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Executes DCOP commands by shelling out to the `dcop` binary.
 */
@Singleton
public class DcopCommandLineExecutor implements DcopExecutor {

    @Inject
    public DcopCommandLineExecutor() {
    }

    @Override
    public List<String> getDcopResult(
            final String app,
            final String object,
            final String function) {
        final ArrayList<String> result = new ArrayList<>();

        InputStreamReader reader;
        BufferedReader input;
        Process process;

        try {
            process = Runtime.getRuntime().exec(new String[]{"dcop", app, object, function});

            reader = new InputStreamReader(process.getInputStream());
            input = new BufferedReader(reader);

            String line;
            while ((line = input.readLine()) != null) {
                result.add(line);
            }

            reader.close();
            input.close();
            process.destroy();
        } catch (IOException ex) {
            // Do nothing
        }

        return result;
    }

}
