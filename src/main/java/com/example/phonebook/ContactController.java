package com.example.phonebook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin // Engedélyezzük a CORS-t, hogy a frontend hozzáférhessen az API-hoz
public class ContactController {
    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    // Listázás és szűrés név alapján
    @GetMapping
    public List<Contact> getAllContacts(@RequestParam(required = false) String name) {
        return service.getContacts(name);
    }

    // Hozzáadás
    @PostMapping
    public Contact addContact(@Valid @RequestBody Contact newContact) {
        return service.addContact(newContact);
    }

    // Módosítás
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @Valid @RequestBody Contact updatedContact) {
        Optional<Contact> updated = service.updateContact(id, updatedContact);

        if(updated.isPresent()) {
            return ResponseEntity.ok(updated.get()); // Sikeres módosítás (200 OK)
        } else {
            return ResponseEntity.notFound().build(); // Sikertelen módosítás, (404 hiba)
        }
    }

    // Törlés
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        if(service.deleteContact(id)) {
            return ResponseEntity.ok().build(); // Sikeres törlés (200 OK)
        } else {
            return ResponseEntity.notFound().build(); // Sikertelen törlés, (404 hiba)
        }
    }

    // Exportálás VCF formátumban
    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportContactToVCF(@PathVariable Long id) {
        // Ellenőrizzük, hogy létezik-e ez az ID az adatbázisban
        Optional<Contact> existingContact = service.findById(id);

        if(existingContact.isPresent()) {
            Contact contact = existingContact.get();
            String vcfData = service.generateVcfData(contact);

            // VCF fájl letöltéséhez szükséges HTTP fejlécek beállítása
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/vcard;charset=utf-8");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contact.getName() + ".vcf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(vcfData);
        } else {
            return ResponseEntity.notFound().build(); // Sikertelen exportálás, (404 hiba)
        }
    }
}