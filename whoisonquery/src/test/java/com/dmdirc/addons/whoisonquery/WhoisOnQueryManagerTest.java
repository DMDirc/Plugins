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

import com.dmdirc.Query;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.ConnectionPrefsRequestedEvent;
import com.dmdirc.events.QueryOpenedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginMetaData;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisOnQueryManagerTest {

    @Mock private EventBus eventBus;
    @Mock private PluginInfo pluginInfo;
    @Mock private PluginMetaData pluginMetaData;
    @Mock private QueryOpenedEvent queryOpenedEvent;
    @Mock private AggregateConfigProvider aggregateConfigProvider;
    @Mock private Connection connection;
    @Mock private WindowModel windowModel;
    @Mock private Query query;
    @Mock private User user;
    @Mock private ClientPrefsOpenedEvent clientPrefsOpenedEvent;
    @Mock private ConnectionPrefsRequestedEvent connectionPrefsRequestedEvent;
    @Mock private PreferencesCategory preferencesCategory;
    @Captor private ArgumentCaptor<PreferencesSetting> preferencesSetting;
    @Mock private ConfigProvider configProvider;
    @Mock private PreferencesDialogModel preferencesDialogModel;
    @Captor private ArgumentCaptor<PreferencesCategory> preferencesCategoryArgumentCaptor;
    private WhoisOnQueryManager instance;

    @Before
    public void setUp() throws Exception {
        when(pluginInfo.getMetaData()).thenReturn(pluginMetaData);
        when(pluginMetaData.getFriendlyName()).thenReturn("Plugin");
        when(queryOpenedEvent.getQuery()).thenReturn(query);
        when(query.getUser()).thenReturn(user);
        when(query.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getWindowModel()).thenReturn(windowModel);
        when(windowModel.getConfigManager()).thenReturn(aggregateConfigProvider);
        when(connectionPrefsRequestedEvent.getConfig()).thenReturn(aggregateConfigProvider);
        when(connectionPrefsRequestedEvent.getIdentity()).thenReturn(configProvider);
        when(connectionPrefsRequestedEvent.getCategory()).thenReturn(preferencesCategory);
        when(clientPrefsOpenedEvent.getModel()).thenReturn(preferencesDialogModel);
        when(preferencesDialogModel.getIdentity()).thenReturn(configProvider);
        when(preferencesDialogModel.getConfigManager()).thenReturn(aggregateConfigProvider);
        when(preferencesDialogModel.getCategory("Plugins")).thenReturn(preferencesCategory);
        instance = new WhoisOnQueryManager("domain", pluginInfo, eventBus);
    }

    @Test
    public void testLoad() throws Exception {
        instance.load();
        verify(eventBus).subscribe(instance);
    }

    @Test
    public void testUnload() throws Exception {
        instance.unload();
        verify(eventBus).unsubscribe(instance);
    }

    @Test
    public void testHandleQueryEvent_Set() throws Exception {
        when(aggregateConfigProvider.getOptionBool("domain", "whoisonquery")).thenReturn(true);
        instance.handleQueryEvent(queryOpenedEvent);
        verify(connection).requestUserInfo(user);
    }

    @Test
    public void testHandleQueryEvent_Unset() throws Exception {
        when(aggregateConfigProvider.getOptionBool("domain", "whoisonquery")).thenReturn(false);
        instance.handleQueryEvent(queryOpenedEvent);
        verify(connection, never()).requestUserInfo(user);
    }

    @Test
    public void testHandlePrefsEvent() throws Exception {
        when(aggregateConfigProvider.getOptionBool("domain", "whoisonquery")).thenReturn(true);
        instance.handlePrefsEvent(clientPrefsOpenedEvent);
        verify(preferencesCategory).addSubCategory(preferencesCategoryArgumentCaptor.capture());
    }

    @Test
    public void testHandleConnectionPrefsEvent() throws Exception {
        when(aggregateConfigProvider.getOptionBool("domain", "whoisonquery")).thenReturn(true);
        instance.handleConnectionPrefsEvent(connectionPrefsRequestedEvent);
        verify(preferencesCategory).addSetting(any());
    }
}