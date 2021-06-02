module fr.devsta.jfxtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens fr.devsta.jfxtest to javafx.fxml;
    exports fr.devsta.jfxtest;
}
