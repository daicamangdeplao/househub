package org.codenot.househub;

import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.advisor.service.knowledgemanagement.Importer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {

    private final Importer importer;

    public Main(Importer importer) {
        this.importer = importer;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        importer.run();
        System.out.println();
    }
}
