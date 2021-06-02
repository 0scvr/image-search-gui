module fr.devsta.jfxtest {
    requires javafx.controls;
    requires javafx.fxml;

    opens fr.devsta.jfxtest to javafx.fxml;
    exports fr.devsta.jfxtest;
}
