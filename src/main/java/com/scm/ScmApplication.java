package com.scm;

import com.scm.util.DataStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScmApplication {

    public static void main(String[] args) {
        DataStore.initializeSampleData();
        SpringApplication.run(ScmApplication.class, args);
        System.out.println("[STARTUP] SCM Web running at http://localhost:8080");
    }
}
