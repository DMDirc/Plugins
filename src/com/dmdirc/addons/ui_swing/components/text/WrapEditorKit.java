/*
 * @author Stanislav Lapitsky
 * @version 1.0
 */

package com.dmdirc.addons.ui_swing.components.text;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
public class WrapEditorKit extends StyledEditorKit {

    private static final long serialVersionUID = 1;
    private ViewFactory defaultFactory = new WrapColumnFactory();

    /** {@inheritDoc} */
    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}