package com.example.phonebook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin // Engedélyezzük a CORS-t, hogy a frontend hozzáférhessen az API-hoz
public class ContactController {

    private final ContactRepository repository;

    public ContactController(ContactRepository repository) {
        this.repository = repository;
    }

    // Listázás és szűrés név alapján
    @GetMapping
    public List<Contact> getAllContacts(@RequestParam(required = false) String name) {
        // Paraméterként kapott név alapján keresünk, ha van ilyen paraméter
        if (name != null && !name.isEmpty()) {
            return repository.findByNameContainingIgnoreCase(name);
        }
        // Ha nem, visszaadjuk az összeset
        return repository.findAll();
    }

    // Hozzáadás
    @PostMapping
    public Contact addContact(@RequestBody Contact newContact) {
        return repository.save(newContact);
    }

    // Módosítás
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact updatedContact) {
        Optional<Contact> existingContact = repository.findById(id);
        
        // Ellenőrizzük, hogy létezik-e ez az ID az adatbázisban
        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();

            // Kicseréljük a régi adatokat az újakra
            contact.setName(updatedContact.getName());
            contact.setPhoneNumber(updatedContact.getPhoneNumber());
            contact.setCity(updatedContact.getCity());
            
            return ResponseEntity.ok(repository.save(contact)); // Sikeres módosítás (200 OK)
        } else {
            return ResponseEntity.notFound().build(); // Sikertelen módosítás, (404 hiba)
        }
    }

    // Törlés
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        // Ellenőrizzük, hogy létezik-e ez az ID az adatbázisban
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build(); // Sikeres törlés (200 OK)
        } else {
            return ResponseEntity.notFound().build(); // Sikertelen törlés, (404 hiba)
        }
    }
}