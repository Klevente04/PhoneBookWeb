package com.example.phonebook;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    // Keresés név alapján (részleges egyezés, kis-nagybetűtől független)
    List<Contact> findByNameContainingIgnoreCase(String name);
}