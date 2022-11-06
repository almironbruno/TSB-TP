module tsb.tsbtp {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens tsb.TSB to javafx.fxml;
    exports tsb.TSB;
}