module fr.devsta.jfxtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires pelican;
    requires java.sql;
    requires com.google.gson;
    requires javafx.base;

    opens fr.devsta.jfxtest to javafx.fxml;
    exports fr.devsta.jfxtest;
}
