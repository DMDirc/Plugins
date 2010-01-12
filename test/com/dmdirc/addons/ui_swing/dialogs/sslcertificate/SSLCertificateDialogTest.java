/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.sslcertificate;

import com.dmdirc.Main;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.harness.ui.TestSSLCertificateDialogModel;
import com.dmdirc.ui.IconManager;
import com.dmdirc.addons.ui_swing.UIUtilities;

import com.dmdirc.config.IdentityManager;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.fest.swing.driver.BasicCellRendererReader;
import org.fest.swing.driver.BasicJListCellReader;
import org.fest.swing.fixture.DialogFixture;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SSLCertificateDialogTest {

    private DialogFixture window;

    @BeforeClass
    public static void setUpClass() throws InvalidIdentityFileException {
        IdentityManager.load();
        UIUtilities.initUISettings();
        Main.setUI(new SwingController());
    }

    @After
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }

    @Test
    public void testTicksAndCrosses() {
        setupWindow();

        assertTrue(Arrays.equals(new String[]{
            "first cert",
            "second cert",
            "invalid cert",
            "trusted cert",
            "invalid+trusted"
        }, window.list().contents()));

        assertTrue(Arrays.equals(new String[]{
            "nothing",
            "nothing",
            "cross",
            "tick",
            "cross"
        },window.list().cellReader(new BasicJListCellReader(new CertificateListCellReader()))
                .contents()));
    }

    @Test
    public void testSelection() {
        setupWindow();

        window.list().requireSelection("first cert");
        
        for (String cert : window.list().contents()) {
            window.list().selectItem(cert).requireSelection(cert);

            assertEquals("Information for " + cert, ((TitledBorder) window
                    .scrollPane(new ClassFinder<CertificateInfoPanel>(CertificateInfoPanel.class, null))
                    .target.getBorder()).getTitle());
        }
    }

    protected void setupWindow() {
        window = new DialogFixture(new SSLCertificateDialog(null,
                new TestSSLCertificateDialogModel()));
        window.show();
    }

    private static class CertificateListCellReader extends BasicCellRendererReader {

        @Override
        public String valueFrom(Component c) {
            final Icon target = ((JLabel) c).getIcon();

            for (String icon : new String[]{"tick", "cross", "nothing"}) {
                if (target == IconManager.getIconManager().getIcon(icon)) {
                    return icon;
                }
            }

            return "?";
        }

    }

}
