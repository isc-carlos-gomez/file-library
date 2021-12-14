package com.krloxz.flibrary.presentation;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import com.krloxz.flibrary.FLibraryApplication;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavafxApplication extends Application {

  private ConfigurableApplicationContext context;

  @Override
  public void init() throws Exception {
    final ApplicationContextInitializer<GenericApplicationContext> initializer =
        genericApplicationContext -> {
          genericApplicationContext.registerBean(Application.class, () -> this);
          genericApplicationContext.registerBean(Parameters.class, () -> getParameters());
          genericApplicationContext.registerBean(HostServices.class, () -> getHostServices());
        };

    this.context = new SpringApplicationBuilder()
        .sources(FLibraryApplication.class)
        .initializers(initializer)
        .build()
        .run(getParameters().getRaw().toArray(new String[0]));
  }

  @Override
  public void start(final Stage stage) throws Exception {
    this.context.publishEvent(new PrimaryStageReady(stage));
  }

  @Override
  public void stop() throws Exception {
    this.context.close();
    Platform.exit();
  }

  class PrimaryStageReady extends ApplicationEvent {

    public Stage getStage() {
      return Stage.class.cast(getSource());
    }

    public PrimaryStageReady(final Object source) {
      super(source);
    }
  }
}
