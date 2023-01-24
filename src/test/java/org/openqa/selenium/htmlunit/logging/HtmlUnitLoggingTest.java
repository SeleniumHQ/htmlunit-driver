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

import java.util.List;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

/**
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitLoggingTest extends WebDriverTestCase {

    @Test
    public void log() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "    console.log('test log');\n"
            + "</script>\n"
            +  "</head>\n"
            + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final Logs logs = driver.manage().logs();
        LogEntries logEntries = logs.get(LogType.BROWSER);
        List<LogEntry> logEntryList = logEntries.getAll();
        assertEquals(1, logEntryList.size());

        final LogEntry logEntry = logEntryList.get(0);
        assertEquals(Level.INFO, logEntry.getLevel());
        assertEquals("test log", logEntry.getMessage());

        // getting the log again has nothing new
        logEntries = logs.get(LogType.BROWSER);
        logEntryList = logEntries.getAll();
        assertEquals(0, logEntryList.size());
    }

    @Test
    public void logMany() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  for (i = 0; i < 1000; i++) {\n"
            + "    console.log('test log ' + i);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final Logs logs = driver.manage().logs();
        final LogEntries logEntries = logs.get(LogType.BROWSER);
        final List<LogEntry> logEntryList = logEntries.getAll();
        assertEquals(1000, logEntryList.size());

        LogEntry logEntry = logEntryList.get(0);
        assertEquals("test log 0", logEntry.getMessage());

        logEntry = logEntryList.get(999);
        assertEquals("test log 999", logEntry.getMessage());
    }

    @Test
    public void logCycleBuffer() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  for (i = 0; i < 1001; i++) {\n"
            + "    console.log('test log ' + i);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final Logs logs = driver.manage().logs();
        final LogEntries logEntries = logs.get(LogType.BROWSER);
        final List<LogEntry> logEntryList = logEntries.getAll();
        assertEquals(1000, logEntryList.size());

        LogEntry logEntry = logEntryList.get(0);
        assertEquals("test log 1", logEntry.getMessage());

        logEntry = logEntryList.get(999);
        assertEquals("test log 1000", logEntry.getMessage());
    }

    @Test
    public void logCycleBuffer2() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  for (i = 0; i < 12345; i++) {\n"
            + "    console.log('test log ' + i);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final Logs logs = driver.manage().logs();
        final LogEntries logEntries = logs.get(LogType.BROWSER);
        final List<LogEntry> logEntryList = logEntries.getAll();
        assertEquals(1000, logEntryList.size());

        LogEntry logEntry = logEntryList.get(0);
        assertEquals("test log 11345", logEntry.getMessage());

        logEntry = logEntryList.get(999);
        assertEquals("test log 12344", logEntry.getMessage());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void correctOrder() throws Exception {
        final String html
            = "<html>\n"
            + "<body>\n"
            + "<script>\n"
            + "  for (i = 0; i < 4; i++) {\n"
            + "    console.log('test log ' + i);\n"
            + "  }\n"
            + "</script>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);

        final Logs logs = driver.manage().logs();
        final LogEntries logEntries = logs.get(LogType.BROWSER);
        final List<LogEntry> logEntryList = logEntries.getAll();

        final int count = logEntryList.size();
        assertTrue(count > 0);

        final long timestamp = 0;
        for (int i = 0; i < 4; i++) {
            final LogEntry logEntry = logEntryList.get(i);
            assertTrue(logEntry.getMessage(), logEntry.getMessage().contains("test log " + i));
            assertTrue(logEntry.getTimestamp() >= timestamp);
        }
    }
}
