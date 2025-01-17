package com.example.lab6;

import com.example.lab6.controllers.LoginController;
import com.example.lab6.controllers.SignInController;
import com.example.lab6.domain.validators.UtilizatorValidator;
import com.example.lab6.repository.database.*;
import com.example.lab6.service.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        UtilizatorValidator validator = new UtilizatorValidator();

        UtilizatorDatabaseRepository usersDB = new UtilizatorDatabaseRepository("postgres", "noris2580"
                ,"jdbc:postgresql://localhost:5432/postgres", validator);
        PrietenieDatabaseRepository priteniDB = new PrietenieDatabaseRepository("postgres", "noris2580"
                ,"jdbc:postgresql://localhost:5432/postgres");
        RequestDatabaseRepository requestDB = new RequestDatabaseRepository("postgres", "noris2580"
                ,"jdbc:postgresql://localhost:5432/postgres");
        MessageDatabaseRepository messageDB = new MessageDatabaseRepository("postgres", "noris2580"
                ,"jdbc:postgresql://localhost:5432/postgres", usersDB);
        UserCredentialsDatabaseRepository credentialsDB= new UserCredentialsDatabaseRepository("postgres", "noris2580"
                ,"jdbc:postgresql://localhost:5432/postgres");


        LoginService loginService = new LoginService(credentialsDB, usersDB);
        Service service = new Service(priteniDB, usersDB, requestDB);
        AppService appService = new AppService(usersDB, priteniDB, requestDB);
        MessageService messageService = new MessageService(messageDB);
        ProfileService profileService = new ProfileService(usersDB);
        SignInService signInService = new SignInService(usersDB, credentialsDB);

        openUserWindow("ChillApp", loginService, appService, messageService, profileService, signInService);
        //openUserWindow("User 2 - Login", loginService, appService, service, messageService);
    }

    private void openUserWindow(String title, LoginService loginService, AppService appService, MessageService messageService, ProfileService profileService, SignInService signInService) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/login-view.fxml"));

        // Setează controller-ul folosind factory pentru dependențe
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == LoginController.class) {
                return new LoginController(loginService, appService, messageService, profileService, signInService);
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        AnchorPane root = loader.load();
        Stage stage = new Stage(); // Creează un nou stage pentru fereastra
        stage.setTitle(title); // Setează titlul ferestrei
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

