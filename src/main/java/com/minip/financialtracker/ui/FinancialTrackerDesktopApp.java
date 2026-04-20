package com.minip.financialtracker.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FinancialTrackerDesktopApp extends Application {

    @Override
    public void start(Stage stage) {
        FinancialTrackerController controller = new FinancialTrackerController();
        Scene scene = new Scene(controller.buildRoot(), 1200, 780);
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());

        stage.setTitle("Financial Tracker - Desktop");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
