package com.example.phonebook;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cant be empty")
    @Size(max = 60, message = "Name can be at most 60 characters")
    @Pattern(
        regexp = "^[a-zA-ZáéíóöőúüűÁÉÍÓÖŐÚÜŰ\\s]+$",
        message = "Name can only contain letters and spaces")
    private String name;

    @NotBlank(message = "Phone number cant be empty")
    @Pattern(
        regexp ="^\\+\\d{1,3} \\d{1,2} \\d{3} \\d{4}$",
        message = "Phone number format is invalid (+36 1 123 4567)")
    private String phoneNumber;

    @NotBlank(message = "City cant be empty")
    @Size(max = 40, message = "City can be at most 40 characters")
    @Pattern(
        regexp = "^[a-zA-ZáéíóöőúüűÁÉÍÓÖŐÚÜŰ\\s]+$",
        message = "City can only contain letters and spaces")
    private String city;

    // Üres konstruktor
    public Contact() {}

    // Konstruktor az adatokkal
    public Contact(String name, String phoneNumber, String city) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.city = city;
    }

    // Getterek és Setterek
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id;}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}