package com.krloxz.forganizer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import javafx.application.Application;

/**
 * @author Carlos Gomez
 */
@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
@EnableAsync
public class FileOrganizerApplication {

  public static void main(final String[] args) {
    Application.launch(JavafxApplication.class, args);
  }

}
