// C:\Users\saipr\Desktop\sairam-tea-backend\sairam-tea-backend\src\main\java\com\example\sairam_tea_backend\SairamTeaBackendApplication.java
package com.example.sairam_tea_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration; // This import is no longer needed

@SpringBootApplication // Removed the exclude attribute
public class SairamTeaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SairamTeaBackendApplication.class, args);
    }

}
