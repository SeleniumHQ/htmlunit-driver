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

package org.openqa.selenium.htmlunit;

import org.junit.Test;

/**
 * Executor tests.
 *
 * @author Rob Winch
 * @author Ronald Brill
 */
public class HtmlUnitExecutorTest {

    /*
     * There is a race condition within HtmlUnitDriver.runAsync such that the
     * wrapped Runnable executes mainCondition.signal() before
     * mainCondition.awaitUninterruptibly() is invoked. While this can be reproduced
     * consistently when using an Executor that invokes Runnable.run on the main
     * Thread, this issue has existed for some time as a race condition.
     */
    @Test
    public void testExecutorImmediate() {
        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setExecutor(r -> r.run());

        driver.runAsync(() -> {
        });
    }
}
