package com.example.phonebook;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service // Service réteg
public class ContactService {
    private final ContactRepository repository;
    
    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    // Listázás és szűrés név alapján
    public List<Contact> getContacts(String name) {
        if (name != null && !name.isEmpty()) {
            return repository.findByNameContainingIgnoreCase(name);
        }
        return repository.findAll();
    }

    // Keresés ID alapján - Segédfüggvény
    public Optional<Contact> findById(Long id) {
        return repository.findById(id);
    }

    // Hozzáadás
    public Contact addContact(Contact newContact) {
        return repository.save(newContact);
    }

    // Módosítás
    public Optional<Contact> updateContact(Long id, Contact updatedContact) {
        Optional<Contact> existingContact = repository.findById(id);
        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();
            contact.setName(updatedContact.getName());
            contact.setPhoneNumber(updatedContact.getPhoneNumber());
            contact.setCity(updatedContact.getCity());

            return Optional.of(repository.save(contact));
        }
        return Optional.empty();
    }

    // Törlés
    public boolean deleteContact(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // VCF formátum összeállítása
    public String generateVcfData(Contact contact) {
        return "BEGIN:VCARD\n" +
               "VERSION:3.0\n" +
               "FN:" + contact.getName() + "\n" +
               "N:" + contact.getName() + ";;;;\n" +
               "TEL:" + contact.getPhoneNumber() + "\n" +
               "ADR:;;" + contact.getCity() + ";;;;\n" +
               "END:VCARD";
    }


}
