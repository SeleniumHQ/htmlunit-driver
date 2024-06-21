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

package org.openqa.selenium.htmlunit.options;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Scott Babcock
 * @author Ronald Brill
 */
@SuppressWarnings("serial")
public class KeyStoreBean implements Serializable {
    private String url_;
    private String password_;
    private String type_;

    public String getUrl() {
        return url_;
    }

    public void setUrl(final String url) {
        url_ = url;
    }

    public String getPassword() {
        return password_;
    }

    public void setPassword(final String password) {
        password_ = password;
    }

    public String getType() {
        return type_;
    }

    public void setType(final String type) {
        type_ = type;
    }

    public URL createUrl() throws MalformedURLException {
        return new URL(url_);
    }
}
