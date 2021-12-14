package com.krloxz.flibrary;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

import com.krloxz.flibrary.presentation.JavafxApplication;

import javafx.application.Application;

/**
 * @author Carlos Gomez
 */
@SpringBootApplication(exclude = { R2dbcAutoConfiguration.class })
public class FLibraryApplication {

  public static void main(final String[] args) {
    Application.launch(JavafxApplication.class, args);
  }

}
