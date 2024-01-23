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

@SuppressWarnings("serial")
public class KeyStoreBean implements Serializable {
    private String url;
    private String password;
    private String type;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(final String url) {
       this.url = url; 
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public URL createUrl() throws MalformedURLException {
        return new URL(url);
    }
}
