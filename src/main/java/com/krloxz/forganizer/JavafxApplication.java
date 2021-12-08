package com.krloxz.forganizer;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
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
        .sources(FileOrganizerApplication.class)
        .initializers(initializer)
        .build()
        .run(getParameters().getRaw().toArray(new String[0]));
  }

  @Override
  public void start(final Stage stage) throws Exception {
    this.context.publishEvent(new StageReadyEvent(stage));
  }

  @Override
  public void stop() throws Exception {
    this.context.close();
    Platform.exit();
  }

  class StageReadyEvent extends ApplicationEvent {

    public Stage getStage() {
      return Stage.class.cast(getSource());
    }

    public StageReadyEvent(final Object source) {
      super(source);
    }
  }
}
