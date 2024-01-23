package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.Browser.HTMLUNIT;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.ExecutableFinder;
import org.openqa.selenium.remote.service.DriverService;

import com.google.auto.service.AutoService;

public class HtmlUnitDriverService extends DriverService {

    protected HtmlUnitDriverService(File executable, int port, Duration timeout, List<String> args,
            Map<String, String> environment) throws IOException {
        super(executable, port, timeout, args, environment);
    }

    @Override
    public Capabilities getDefaultDriverOptions() {
        return new HtmlUnitDriverOptions();
    }

    /**
     * Configures and returns a new {@link HtmlUnitDriverService} using the default
     * configuration.
     *
     * @return A new HtmlUnitDriverService using the default configuration.
     */
    public static HtmlUnitDriverService createDefaultService() {
        return new Builder().build();
    }

    @Override
    public String getDriverName() {
        return HTMLUNIT.browserName();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start() throws IOException {
        // nothing to do here
    }

    @Override
    protected void waitUntilAvailable() {
        // nothing to do here
    }

    @Override
    public void stop() {
        // nothing to do here
    }

    @Override
    protected boolean hasShutdownEndpoint() {
        return false;
    }

    @Override
    public void close() {
        // nothing to do here
    }

    @AutoService(DriverService.Builder.class)
    public static class Builder extends DriverService.Builder<HtmlUnitDriverService, HtmlUnitDriverService.Builder> {

        @Override
        public int score(final Capabilities capabilities) {
            int score = 0;

            if (HTMLUNIT.is(capabilities.getBrowserName())) {
                score++;
            }

            if (capabilities.getCapability(HtmlUnitDriverOptions.HTMLUNIT_OPTIONS) != null) {
                score++;
            }

            return score;
        }

        @Override
        protected void loadSystemProperties() {
            // nothing to do here
        }

        @Override
        protected List<String> createArgs() {
            return Collections.emptyList();
        }

        @Override
        protected HtmlUnitDriverService createDriverService(final File exe, final int port, final Duration timeout,
                final List<String> args, final Map<String, String> environment) {
            try {
                String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                File executable = new File(new ExecutableFinder().find(javaPath));
                return new HtmlUnitDriverService(executable, port, timeout, args, environment);
            } catch (IOException e) {
                throw new WebDriverException(e);
            }
        }
    }
}
