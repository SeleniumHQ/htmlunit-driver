// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConsole.Logger;

/**
 * An implementation of the {@link Logs} interface for HtmlUnit.
 * At the moment this is empty.
 */
public class HtmlUnitLogs implements Logs {
    final HtmlUnitDriverLogger logger;

    public HtmlUnitLogs(WebClient webClient) {
        logger = new HtmlUnitDriverLogger();
        webClient.getWebConsole().setLogger(logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogEntries get(String logType) {
        if (logType == LogType.BROWSER) {
            return new LogEntries(logger.getContentAndFlush());
        }

        return new LogEntries(Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAvailableLogTypes() {
        return Collections.<String>emptySet();
    }

    private static class HtmlUnitDriverLogger implements Logger {
        private static final int BUFFER_SIZE = 1000;

        private LogEntry[] buffer = new LogEntry[BUFFER_SIZE];
        private int insertPos = 0;
        private boolean isFull = false;

        private void append(LogEntry entry) {
            buffer[insertPos] = entry;
            insertPos++;
            if (insertPos == BUFFER_SIZE) {
                insertPos = 0;
                isFull = true;
            }
        }

        private List<LogEntry> getContentAndFlush() {
            List<LogEntry> result;
            if (isFull) {
                result = new ArrayList<>(BUFFER_SIZE);
                int i = insertPos;
                for (; i < BUFFER_SIZE; i++) {
                    result.add(buffer[i]);
                }
            }
            else {
                result = new ArrayList<>(insertPos);
            }

            for (int i = 0; i < insertPos; i++) {
                result.add(buffer[i]);
            }

            insertPos = 0;
            isFull = false;

            return result;
        }

        @Override
        public void warn(final Object message) {
            append(new LogEntry(Level.WARNING, System.currentTimeMillis(), message == null ? "" : message.toString()));
        }

        @Override
        public void trace(final Object message) {
            append(new LogEntry(Level.FINEST, System.currentTimeMillis(), message == null ? "" : message.toString()));
        }

        @Override
        public void info(final Object message) {
            append(new LogEntry(Level.INFO, System.currentTimeMillis(), message == null ? "" : message.toString()));
        }

        @Override
        public void error(final Object message) {
            append(new LogEntry(Level.SEVERE, System.currentTimeMillis(), message == null ? "" : message.toString()));
        }

        @Override
        public void debug(final Object message) {
            append(new LogEntry(Level.FINE, System.currentTimeMillis(), message == null ? "" : message.toString()));
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }
    }
}
