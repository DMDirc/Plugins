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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.updater.checking.BaseCheckResult;
import com.dmdirc.updater.checking.UpdateCheckResult;
import com.dmdirc.updater.checking.UpdateCheckStrategy;
import com.dmdirc.updater.installing.TypeSensitiveInstallationStrategy;
import com.dmdirc.updater.installing.UpdateInstallationListener;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.updater.retrieving.BaseRetrievalResult;
import com.dmdirc.updater.retrieving.TypeSensitiveRetrievalStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalListener;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;
import com.dmdirc.util.collections.ListenerList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Generates some fake updates.
 */
public class FakeUpdates extends DebugCommand {

    /** The update manager to add fake updates to. */
    private final UpdateManager updateManager;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param updateManager The update manager to add fake updates to.
     */
    @Inject
    public FakeUpdates(
            final Provider<Debug> commandProvider,
            final UpdateManager updateManager) {
        super(commandProvider);

        this.updateManager = updateManager;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "fakeupdates";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Initialises a fake update handling chain";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        updateManager.addCheckStrategy(new FakeUpdateCheckStrategy());
        updateManager.addRetrievalStrategy(new FakeUpdateRetriever());
        updateManager.addInstallationStrategy(new FakeUpdateInstaller());
    }

    /**
     * A fake update check strategy which produces fake update check results.
     */
    private static class FakeUpdateCheckStrategy
            implements UpdateCheckStrategy {

        /** {@inheritDoc} */
        @Override
        public Map<UpdateComponent, UpdateCheckResult> checkForUpdates(
                final Collection<UpdateComponent> components) {
            final Map<UpdateComponent, UpdateCheckResult> res = new HashMap<>();

            for (UpdateComponent component : components) {
                if (Math.random() * components.size() < 10) {
                    res.put(component, new FakeUpdateCheckResult(component));
                }
            }

            return res;
        }
    }

    /**
     * A fake update check result.
     */
    private static class FakeUpdateCheckResult extends BaseCheckResult {

        /**
         * Creates a new fake result for the given component.
         *
         * @param component The component this result is for
         */
        public FakeUpdateCheckResult(final UpdateComponent component) {
            super(component, true, "shiny newness", new Version("1.337"));
        }

    }

    private static class FakeUpdateRetriever
            extends TypeSensitiveRetrievalStrategy<FakeUpdateCheckResult> {

        private final ListenerList listeners = new ListenerList();

        /**
         * Creates a new {@link FakeUpdateRetriever}.
         */
        public FakeUpdateRetriever() {
            super(FakeUpdateCheckResult.class);
        }

        protected void fireRetrievalProgressChanged(final UpdateComponent component, final double progress) {
            listeners.getCallable(UpdateRetrievalListener.class).retrievalProgressChanged(component, progress);
        }

        protected void fireRetrievalFailed(final UpdateComponent component) {
            listeners.getCallable(UpdateRetrievalListener.class).retrievalFailed(component);
        }

        protected void fireRetrievalCompleted(final UpdateComponent component) {
            listeners.getCallable(UpdateRetrievalListener.class).retrievalCompleted(component);
        }

        @Override
        public void addUpdateRetrievalListener(final UpdateRetrievalListener listener) {
            listeners.add(UpdateRetrievalListener.class, listener);
        }

        @Override
        public void removeUpdateRetrievalListener(final UpdateRetrievalListener listener) {
            listeners.remove(UpdateRetrievalListener.class, listener);
        }

        /** {@inheritDoc} */
        @Override
        protected UpdateRetrievalResult retrieveImpl(
                final FakeUpdateCheckResult checkResult) {
            try {
                fireRetrievalProgressChanged(checkResult.getComponent(), 0);

                final int max = (int) (Math.random() * 50);
                for (int i = 0; i < max; i++) {
                    Thread.sleep(100);

                    fireRetrievalProgressChanged(checkResult.getComponent(),
                            100 * i / max);
                }

                if (Math.random() < 0.75) {
                    fireRetrievalCompleted(checkResult.getComponent());
                    return new FakeRetrievalResult(checkResult, true);
                } else {
                    fireRetrievalFailed(checkResult.getComponent());
                    return new FakeRetrievalResult(checkResult, false);
                }
            } catch (InterruptedException ex) {
                // Don't care
            }

            return new FakeRetrievalResult(checkResult, false);
        }

    }

    /**
     * Fake retrieval result.
     */
    private static class FakeRetrievalResult extends BaseRetrievalResult {

        /**
         * Creates a new fake retrieval result.
         *
         * @param checkResult The check result that was retrieved
         * @param success Whether the result is successful or not
         */
        public FakeRetrievalResult(final UpdateCheckResult checkResult,
                final boolean success) {
            super(checkResult, success);
        }

    }

    /**
     * A fake update installer.
     */
    private static class FakeUpdateInstaller
            extends TypeSensitiveInstallationStrategy<UpdateComponent, FakeRetrievalResult> {

        private final ListenerList listeners = new ListenerList();

        /**
         * Creates a new {@link FakeUpdateInstaller}.
         */
        public FakeUpdateInstaller() {
            super(UpdateComponent.class, FakeRetrievalResult.class);
        }

        protected void fireInstallCompleted(final UpdateComponent component) {
            listeners.getCallable(UpdateInstallationListener.class).installCompleted(component);
        }

        protected void fireInstallFailed(final UpdateComponent component) {
            listeners.getCallable(UpdateInstallationListener.class).installFailed(component);
        }

        protected void fireInstallProgressChanged(final UpdateComponent component, final double progress) {
            listeners.getCallable(UpdateInstallationListener.class).installProgressChanged(component, progress);
        }

        @Override
        public void addUpdateInstallationListener(final UpdateInstallationListener listener) {
            listeners.add(UpdateInstallationListener.class, listener);
        }

        @Override
        public void removeUpdateInstallationListener(final UpdateInstallationListener listener) {
            listeners.remove(UpdateInstallationListener.class, listener);
        }

        /** {@inheritDoc} */
        @Override
        protected void installImpl(final UpdateComponent component,
                final FakeRetrievalResult retrievalResult) {
            try {
                fireInstallProgressChanged(component, 0);
                final int max = (int) (Math.random() * 50);
                for (int i = 0; i < max; i++) {
                    Thread.sleep(100);

                    fireInstallProgressChanged(component, 100 * i / max);
                }

                if (Math.random() < 0.75) {
                    fireInstallCompleted(component);
                } else {
                    fireInstallFailed(component);
                }
            } catch (InterruptedException ex) {
                // Don't care
            }
        }

    }

}
