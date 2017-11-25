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

package org.openqa.selenium.htmlunit.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.testing.JUnit4TestBase;

public class ElementFindingTest extends JUnit4TestBase {

  @Test
  public void canSetImplicitWaitTimeout() {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
  }

  @Test
  public void canFindElementByTagName() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.tagName("form"))
        .findElement(By.tagName("input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByTagName() {
    driver.get(appServer.whereIs("form.html"));
    List<WebElement> forms = driver.findElements(By.tagName("form"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.tagName("input"));
    assertThat(inputs.size(), equalTo(3));
  }

  @Test
  public void canFindElementByCssSelector() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.cssSelector("#form_id"))
        .findElement(By.cssSelector("input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByCssSelector() {
    driver.get(appServer.whereIs("form.html"));
    List<WebElement> forms = driver.findElements(By.cssSelector("#form_id"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.cssSelector("input"));
    assertThat(inputs.size(), equalTo(3));
  }

  @Test
  public void canFindElementByXpath() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.xpath("//form"))
        .findElement(By.xpath("./input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByXpathInHtmlDocument() {
    driver.get(appServer.whereIs("form.html"));
    canFindElementsByXpath();
  }

  @Test
  public void canFindElementsByXpathInXmlDocument() {
    driver.get(appServer.whereIs("form.xml"));
    canFindElementsByXpath();
  }
  
  public void canFindElementsByXpath() {
    List<WebElement> forms = driver.findElements(By.xpath("//form"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.xpath("./input"));
    assertThat(inputs.size(), equalTo(3));
  }
  
  @Test
  public void canFindElementByName() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.name("form_name"))
        .findElement(By.name("text"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByName() {
    driver.get(appServer.whereIs("form.html"));
    List<WebElement> forms = driver.findElements(By.name("form_name"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.name("text"));
    assertThat(inputs.size(), equalTo(1));
  }

  @Test
  public void canFindElementById() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.id("form_id"))
        .findElement(By.id("text"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsById() {
    driver.get(appServer.whereIs("form.html"));
    List<WebElement> forms = driver.findElements(By.id("form_id"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.id("text"));
    assertThat(inputs.size(), equalTo(1));
  }

  @Test
  public void canFindElementByLinkText() {
    driver.get(appServer.whereIs("alert.html"));
    WebElement link = driver.findElement(By.linkText("Click me to get an alert"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));

    link = driver.findElement(By.tagName("body")).findElement(By.linkText("Click me to get an alert"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));
  }

  @Test
  public void canFindElementsByLinkText() {
    driver.get(appServer.whereIs("alert.html"));
    List<WebElement> links = driver.findElements(By.linkText("Click me to get an alert"));
    assertThat(links.size(), equalTo(1));

    links = driver.findElement(By.tagName("body")).findElements(By.linkText("Click me to get an alert"));
    assertThat(links.size(), equalTo(1));
  }

  @Test
  public void canFindElementByPartialLinkText() {
    driver.get(appServer.whereIs("alert.html"));
    WebElement link = driver.findElement(By.partialLinkText("Click me"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));

    link = driver.findElement(By.tagName("body")).findElement(By.partialLinkText("Click me"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));
  }

  @Test
  public void canFindElementsByPartialLinkText() {
    driver.get(appServer.whereIs("alert.html"));
    List<WebElement> links = driver.findElements(By.partialLinkText("Click me"));
    assertThat(links.size(), equalTo(1));

    links = driver.findElement(By.tagName("body")).findElements(By.partialLinkText("Click me"));
    assertThat(links.size(), equalTo(1));
  }

  @Test(expected = NoSuchElementException.class)
  public void noElement() {
    driver.get(appServer.whereIs("form.html"));
    driver.findElement(By.id("nothing"));
  }

}
