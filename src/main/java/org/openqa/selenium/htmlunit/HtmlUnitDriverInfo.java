package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.Browser.HTMLUNIT;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverInfo;

import com.google.auto.service.AutoService;

@AutoService(WebDriverInfo.class)
public class HtmlUnitDriverInfo implements WebDriverInfo {

    @Override
    public String getDisplayName() {
        return "HtmlUnit";
    }

    @Override
    public Capabilities getCanonicalCapabilities() {
        return new ImmutableCapabilities(BROWSER_NAME, HTMLUNIT.browserName());
    }

    @Override
    public boolean isSupporting(final Capabilities capabilities) {
        return HTMLUNIT.is(capabilities);
    }

    @Override
    public boolean isSupportingCdp() {
        return false;
    }

    @Override
    public boolean isSupportingBiDi() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public int getMaximumSimultaneousSessions() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public Optional<WebDriver> createDriver(final Capabilities capabilities) throws SessionNotCreatedException {
        if (!isAvailable()) {
            return Optional.empty();
        }
        
        return Optional.of(new HtmlUnitDriver(new HtmlUnitDriverOptions().merge(capabilities)));
    }

}
