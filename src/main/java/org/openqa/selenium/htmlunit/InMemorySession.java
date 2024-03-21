package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.Browser.HTMLUNIT;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.Dialect.W3C;
import static org.openqa.selenium.remote.http.Contents.asJson;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.grid.data.CreateSessionRequest;
import org.openqa.selenium.grid.node.ActiveSession;
import org.openqa.selenium.grid.node.SessionFactory;
import org.openqa.selenium.internal.Either;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

public class InMemorySession implements ActiveSession {

    private WebDriver driver;
    private Capabilities stereotype;
    private Capabilities capabilities;
    private SessionId sessionId;
    private Dialect downstream;
    private Instant startTime;

    private InMemorySession(final WebDriver driver, final Capabilities capabilities, final Dialect downstream) {
        this.driver = Objects.requireNonNull(driver);

        Capabilities caps;
        if (driver instanceof HasCapabilities) {
            caps = ((HasCapabilities) driver).getCapabilities();
        } else {
            caps = capabilities;
        }

        this.capabilities = new ImmutableCapabilities(caps);
        this.sessionId = new SessionId(UUID.randomUUID().toString());
        this.downstream = Objects.requireNonNull(downstream);
        this.startTime = Instant.now();
    }

    @Override
    public HttpResponse execute(final HttpRequest req) throws UncheckedIOException {
        HashMap<String, Object> response = new HashMap<>();
        
        Command command = W3C.getCommandCodec().decode(req);
        switch (command.getName()) {
        case DriverCommand.GET_CAPABILITIES:
            break;
        case DriverCommand.GET:
            driver.get(command.getParameters().get("url").toString());
            break;
            
        }
        
        return new HttpResponse().setContent(asJson(Map.of("value", response)));
    }

    @Override
    public SessionId getId() {
        return sessionId;
    }

    /**
     * Get the actual capabilities of the node for this session.
     * 
     * @return actual node {@link Capabilities}
     * @see #getCapabilities()
     */
    @Override
    public Capabilities getStereotype() {
        return stereotype;
    }

    /**
     * Get the requested capabilities for this session.
     * 
     * @return requested session {@link Capabilities}
     * @see #getStereotype()
     */
    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public URI getUri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dialect getUpstreamDialect() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dialect getDownstreamDialect() {
        return downstream;
    }

    @Override
    public void stop() {
        driver.close();
    }

    public static class HtmlUnitSessionFactory implements SessionFactory {

        @Override
        public Capabilities getStereotype() {
            return new ImmutableCapabilities(BROWSER_NAME, HTMLUNIT.browserName());
        }

        @Override
        public Either<WebDriverException, ActiveSession> apply(final CreateSessionRequest t) {
            WebDriver driver = new HtmlUnitDriver(t.getDesiredCapabilities());
            ActiveSession session = new InMemorySession(driver, t.getDesiredCapabilities(), W3C);
            return Either.right(session);
        }

        @Override
        public boolean test(final Capabilities t) {
            return HTMLUNIT.is(t);
        }
    }
}
