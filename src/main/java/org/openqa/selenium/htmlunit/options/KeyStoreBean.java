// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
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
 * Represents a key store configuration.
 * 
 * <p>Holds information about the key store URL, password, and type.</p>
 * 
 * @author Scott Babcock
 * @author Ronald Brill
 */
@SuppressWarnings("serial")
public class KeyStoreBean implements Serializable {

    /** The URL of the key store as a string. */
    private String url_;
    /** The password used to access the key store. */
    private String password_;
    /** The type of the key store (e.g., "JKS", "PKCS12"). */
    private String type_;

    /**
     * Gets the key store URL as a string.
     * 
     * @return the key store URL
     */
    public String getUrl() {
        return url_;
    }

    /**
     * Sets the key store URL.
     * 
     * @param url the key store URL
     */
    public void setUrl(final String url) {
        url_ = url;
    }

    /**
     * Gets the password for the key store.
     * 
     * @return the key store password
     */
    public String getPassword() {
        return password_;
    }

    /**
     * Sets the password for the key store.
     * 
     * @param password the key store password
     */
    public void setPassword(final String password) {
        password_ = password;
    }

    /**
     * Gets the type of the key store.
     * 
     * @return the key store type
     */
    public String getType() {
        return type_;
    }

    /**
     * Sets the type of the key store.
     * 
     * @param type the key store type
     */
    public void setType(final String type) {
        type_ = type;
    }

    /**
     * Creates a {@link URL} object from the key store URL string.
     * 
     * @return a {@link URL} object representing the key store URL
     * @throws MalformedURLException if the URL string is not valid
     */
    public URL createUrl() throws MalformedURLException {
        return new URL(url_);
    }
}
