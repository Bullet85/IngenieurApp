module com.example.mainapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires kernel;  // Für iText Kernel
    requires layout;  // Für iText Layout
    requires io;

    opens com.example.mainapp to javafx.fxml;
    exports com.example.mainapp;
}