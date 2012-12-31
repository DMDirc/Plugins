/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.parser_twitter.actions;

import com.dmdirc.Server;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.addons.parser_twitter.Twitter;
import com.dmdirc.addons.parser_twitter.api.TwitterStatus;
import com.dmdirc.addons.parser_twitter.api.TwitterUser;
import com.dmdirc.interfaces.actions.ActionComponentArgument;
import com.dmdirc.parser.interfaces.Parser;

/**
 * Action components which expose Twitter functionality.
 *
 * @since 0.6.4
 * @author chris
 */
public enum TwitterActionComponents implements ActionComponent {

    /** Takes a twitter channel name (&12345) and returns the status. */
    TWITTER_CHANNEL_NAME_STATUS {

        /** {@inheritDoc} */
        @Override
        public Object get(final ActionComponentArgument arg) {
            final long id = Long.parseLong(((String) arg.getObject()).substring(1));

            for (Server server : arg.getMain().getServerManager().getServers()) {
                final Parser parser = server.getParser();

                if (parser instanceof Twitter) {
                    final TwitterStatus status = ((Twitter) parser).getApi().getStatus(id);

                    if (status != null) {
                        return status;
                    }
                }
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() {
            return String.class;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return TwitterStatus.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "twitter status (if a Twitter channel link)";
        }

    },

    /** Returns the user who created a tweet. */
    TWITTER_STATUS_USER {

        /** {@inheritDoc} */
        @Override
        public Object get(final ActionComponentArgument arg) {
            return ((TwitterStatus) arg.getObject()).getUser();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() {
            return TwitterStatus.class;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return TwitterUser.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "user";
        }

    },

    /** Returns the screen name of a twitter user. */
    TWITTER_USER_SCREENNAME {

        /** {@inheritDoc} */
        @Override
        public Object get(final ActionComponentArgument arg) {
            return ((TwitterUser) arg.getObject()).getScreenName();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() {
            return TwitterUser.class;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return String.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "screen name";
        }

    };

}
