module ni.edu.uam.proyectofinalivsemestre {
    requires javafx.controls;
    requires javafx.fxml;
    opens ni.edu.uam.proyectofinalivsemestre.controller to javafx.fxml;
    exports ni.edu.uam.proyectofinalivsemestre.launcher;
    requires java.sql;
    requires java.net.http;
    requires com.google.gson;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    opens ni.edu.uam.proyectofinalivsemestre.model to org.hibernate.orm.core, jakarta.persistence, javafx.base, com.google.gson;
    opens ni.edu.uam.proyectofinalivsemestre.config to org.hibernate.orm.core, jakarta.persistence;
    opens ni.edu.uam.proyectofinalivsemestre.service to com.google.gson;

    exports ni.edu.uam.proyectofinalivsemestre.model;
    exports ni.edu.uam.proyectofinalivsemestre.dao;
    exports ni.edu.uam.proyectofinalivsemestre.config;
    exports ni.edu.uam.proyectofinalivsemestre.service;
}
