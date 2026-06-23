package com.example.phonebook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    // Exportálás CSV formátumban
    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportContactToVCF(@PathVariable Long id) {
        // Ellenőrizzük, hogy létezik-e ez az ID az adatbázisban
        Optional<Contact> existingContact = repository.findById(id);

        if(existingContact.isPresent()) {
            Contact contact = existingContact.get();
            // VCF formátumú adat előállítása
            String vcfData = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "FN:" + contact.getName() + "\n" +
                "N:" + contact.getName() + ";;;;\n" +
                "TEL:" + contact.getPhoneNumber() + "\n" +
                "ADR:;;" + contact.getCity() + ";;;;\n" +
                "END:VCARD";

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