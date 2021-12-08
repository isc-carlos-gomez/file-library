package com.krloxz.forganizer;

import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class PrimaryStageInitializer implements ApplicationListener<JavafxApplication.StageReadyEvent> {

  private final String applicationTitle;
  private final Resource fxml;
  private final ApplicationContext applicationContext;

  public PrimaryStageInitializer(@Value("${spring.application.ui.title}") final String applicationTitle,
      @Value("classpath:/ui/file-explorer.fxml") final Resource fxml, final ApplicationContext applicationContext) {
    this.applicationTitle = applicationTitle;
    this.fxml = fxml;
    this.applicationContext = applicationContext;
  }

  @Override
  public void onApplicationEvent(final JavafxApplication.StageReadyEvent stageReadyEvent) {
    try {
      final Stage stage = stageReadyEvent.getStage();

      

      final URL url = this.fxml.getURL();
      final FXMLLoader fxmlLoader = new FXMLLoader(url);
      fxmlLoader.setControllerFactory(this.applicationContext::getBean);
      final Parent root = fxmlLoader.load();
      final Scene scene = new Scene(root, 600, 600);
      stage.setScene(scene);
      stage.setTitle(this.applicationTitle);

      stage.show();

    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

}
