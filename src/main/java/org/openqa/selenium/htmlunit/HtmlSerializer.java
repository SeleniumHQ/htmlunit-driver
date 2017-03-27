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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlBreak;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

/**
 * Supports specific WebDriver requirements.
 */
class HtmlSerializer {

  static String getText(final DomElement element) {
    if (element instanceof HtmlInput) {
      return "";
    }
    if (element instanceof HtmlLabel) {
      return getDirectChildren(element);
    }
    String text = element.asText();
    if (element instanceof HtmlTextArea && element.isDisplayed()) {
      text = ((HtmlTextArea) element).getDefaultValue();
      if (text.endsWith("\n")) {
        text = text.substring(0,  text.length() - 1);
      }
    }
    text = text.replace('\t', ' ');
    text = text.replace("\r", "");
    if (!(element instanceof HtmlElement)) {
      text = text.replaceAll("\\p{javaWhitespace}+", " ").trim();
    }
    return text;
  }

  static private String getDirectChildren(final DomElement element) {
    final StringBuilder builder = new StringBuilder();
    for (DomNode e : element.getChildNodes()) {
      if (e instanceof DomText) {
        builder.append(e.asText());
      }
      else if (e instanceof HtmlBreak) {
        builder.append('\n');
      }
    }
    return builder.toString();
  }
}
