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

package com.dmdirc.addons.calc;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;

import java.text.ParseException;

/**
 * A command which allows users to evaluate various mathematical expressions,
 * and perform basic calculations.
 *
 * @author chris
 */
public class CalcCommand extends GlobalCommand {

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        try {
            int offset = 0;
            boolean showexpr = false;

            if (args.getArguments().length > 0 && args.getArguments()[0].equals("--showexpr")) {
                showexpr = true;
                offset++;
            }

            final String input = args.getArgumentsAsString(offset);
            final Lexer lexer = new Lexer(input);
            final Parser parser = new Parser(lexer);
            final Evaluator evaluator = new Evaluator(parser.parse());
            final Number result = evaluator.evaluate();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    (showexpr ? input + " = " : "") + result);
        } catch (ParseException ex) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to parse expression: "
                    + ex.getMessage());
        } catch (ArithmeticException ex) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to calculate expression: "
                    + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "calc";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "calc [--showexpr] <expr> - evaluate mathematical expression";
    }
}
