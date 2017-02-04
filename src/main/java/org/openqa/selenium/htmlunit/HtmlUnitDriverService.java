package org.openqa.selenium.htmlunit;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.server.Status;
import org.openqa.selenium.remote.service.DriverService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class HtmlUnitDriverService extends DriverService {

  private Server server;
  
  protected HtmlUnitDriverService(File executable, int port, ImmutableList<String> args,
      ImmutableMap<String, String> environment) throws IOException {
    super(executable, port, args, environment);
  }

  public static HtmlUnitDriverService createDefaultService() {
    return new Builder().usingAnyFreePort().build();
  }

  @Override
  public void start() throws IOException {
    server = new Server(getUrl().getPort());

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    context.setContextPath("/");

    ServletHolder servlet = context.addServlet(ServletContainer.class, "/*");
    servlet.setInitOrder(0);
    servlet.setInitParameter("jersey.config.server.provider.packages", Status.class.getPackage().getName());

    server.setHandler(context);
    try {
      server.start();
    }
    catch(IOException e) {
      throw e;
    }
    catch(Exception e) {
      throw new WebDriverException(e);
    }
  }

  @Override
  public void stop() {
    try {
      server.stop();
    }
    catch(Exception e) {
      throw new WebDriverException(e);
    }
    server = null;
  }

  public static class Builder extends DriverService.Builder<HtmlUnitDriverService, HtmlUnitDriverService.Builder> {

    @Override
    protected File findDefaultExecutable() {
      return new File(".");
    }

    @Override
    protected ImmutableList<String> createArgs() {
      return ImmutableList.of();
    }

    @Override
    protected HtmlUnitDriverService createDriverService(File exe, int port, ImmutableList<String> args,
        ImmutableMap<String, String> environment) {
      try {
        return new HtmlUnitDriverService(exe, port, args, environment);
      } catch (IOException e) {
        throw new WebDriverException(e);
      }
    }
  }
}
