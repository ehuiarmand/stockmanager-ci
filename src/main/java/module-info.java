module com.inphb.icgl.stocks {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;

    exports com.inphb.icgl.stocks;
    exports com.inphb.icgl.stocks.model;
    exports com.inphb.icgl.stocks.repository;
    exports com.inphb.icgl.stocks.dao;
    exports com.inphb.icgl.stocks.controller;
    exports com.inphb.icgl.stocks.utils;

    opens com.inphb.icgl.stocks to javafx.fxml;
    opens com.inphb.icgl.stocks.controller to javafx.fxml;
}
