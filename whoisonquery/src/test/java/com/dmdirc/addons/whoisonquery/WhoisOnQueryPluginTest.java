/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.whoisonquery;

import com.dmdirc.plugins.PluginInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import dagger.ObjectGraph;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisOnQueryPluginTest {

    @Mock private PluginInfo pluginInfo;
    @Mock private ObjectGraph objectGraph;
    @Mock private WhoisOnQueryManager whoisOnQueryManager;
    private WhoisOnQueryPlugin instance;

    @Before
    public void setup() {
        when(objectGraph.plus(any())).thenReturn(objectGraph);
        when(objectGraph.get(any())).thenReturn(whoisOnQueryManager);
        instance = new WhoisOnQueryPlugin();
    }

    @Test
    public void testOnLoad() throws Exception {
        instance.load(pluginInfo, objectGraph);
        instance.onLoad();
        verify(whoisOnQueryManager).load();
    }

    @Test
    public void testOnUnload() throws Exception {
        instance.load(pluginInfo, objectGraph);
        instance.onUnload();
        verify(whoisOnQueryManager).unload();
    }
}