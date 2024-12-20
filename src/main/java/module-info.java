module com.example.lab6 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jdk.jshell;
    requires java.desktop;


    opens com.example.lab6 to javafx.fxml;
    exports com.example.lab6;
}