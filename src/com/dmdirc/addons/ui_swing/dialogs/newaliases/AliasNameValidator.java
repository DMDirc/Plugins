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

package com.dmdirc.addons.ui_swing.dialogs.newaliases;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

import com.google.common.base.Optional;

/**
 * Validates an alias name again the list, taking into account the selected alias.
 */
public class AliasNameValidator implements Validator<String> {

    private final AliasManagerModel aliases;
    private final Optional<Alias> selectedAlias;

    public AliasNameValidator(final AliasManagerModel aliases, final Optional<Alias> selectedAlias) {
        this.aliases = aliases;
        this.selectedAlias = selectedAlias;
    }

    @Override
    public ValidationResponse validate(final String object) {
        for (Alias targetAlias : aliases.getAliases()) {
            if (targetAlias != selectedAlias.get() && targetAlias.getName().equalsIgnoreCase(object)) {
                return new ValidationResponse("Alias names must be unique");
            }
        }
        return new ValidationResponse();
    }

}
