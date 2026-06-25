package com.example.phonebook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PhonebookApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhonebookApplication.class, args);
    }

    // Ez fut le a szerver indulásakor, és feltölti a MySQL-t kezdőadatokkal
    @Bean
    public CommandLineRunner initDatabase(ContactRepository repository) {
        return args -> {
            // Csak akkor töltjük fel, ha még üres az adatbázis
            if (repository.count() == 0) {
                repository.save(new Contact("Kovács Gábor", "+36 30 123 4567", "Budapest"));
                repository.save(new Contact("Nagy Anna", "+36 20 987 6543", "Debrecen"));
                repository.save(new Contact("Kiss Péter", "+36 70 555 4444", "Gödöllő"));
                System.out.println("---- TESZT ADATOK BETÖLTVE A MYSQL-BE! ----");
            }
        };
    }
}