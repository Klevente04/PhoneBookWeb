<script setup>
import { ref, onMounted } from 'vue'

const contacts = ref([])
const loading = ref(true)
const error = ref(null)

// Adatbázis lekérése
async function fetchContacts() {
  try {
    const response = await fetch('http://localhost:8080/api/contacts')

    if (!response.ok) {
      throw new Error(`HTTP error: ${response.status}`)
    }

    contacts.value = await response.json()

  } catch (err) {
    error.value = err.message
    console.error(err)

  } finally {
    loading.value = false
  }
}

// Az adatok betöltése a komponens mountolásakor
onMounted(() => {
  fetchContacts()
})
</script>

<template>
  <div class="container">
    
    <h2>Ügyfél lista</h2>
    
    <div class="header-container">  
      <input
        placeholder="Keresés ügyfél neve alapján..."
        v-model="searchQuery"
        class="search-input"
      />
      <button class="btn btn-add">Új</button>
    </div>

    <p v-if="loading">Adatok betöltése folyamatban...</p>
    <p v-else-if="error">Hiba történt az adatok lekérésekor: {{ error }}</p>
    <p v-else-if="contacts.length === 0">A lista üres.</p>

    <ul v-else>
      <li v-for="contact in contacts" :key="contact.id" class="contact-card">
        <div>
          <strong>{{ contact.name }}</strong> <br>
          {{ contact.phoneNumber }} | {{ contact.city }}
        </div>

        <div class="contact-actions">
          <button class="btn btn-edit">Módosítás</button>
          <button class="btn btn-delete">Törlés</button>
        </div>
      </li>
    </ul>

  </div>
</template>

<style scoped>

/* OLDAL STÍLUSA */
.container {
  max-width: 900px;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  margin: 0 auto;
}

/* CÍMSOR STÍLUSA */
h2 {
  color: #2e7d32;
  text-align: center;
  font-size: 2.2em;
  margin: 10px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #a5d6a7;
}

/* FEJLÉC STÍLUSA */
.header-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  margin-bottom: 25px;
}

/* KERESŐ STÍLUSA */
.search-input {
  flex: 1; 
  padding: 10px 20px;
  border-radius: 25px;
  border: 2px solid #81c784;
  font-size: 1.5em;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #439b46;
  outline: none;
}

p {
  color: #388e3c;
  text-align: center;
  font-style: italic;
}

ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

/* KÁRTYÁK STÍLUSA */
.contact-card {
  background-color: #c8e6c9;
  color: #1b5e20;
  margin-bottom: 8px;
  padding: 10px 15px;
  border-radius: 8px;
  border-left: 5px solid #4caf50;
  
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.contact-card:hover {
  background-color: #a5d6a7;
}

.contact-card strong {
  font-size: 1.5em;
  color: #0b7462;
}

/* GOMBOK STÍLUSA */
.contact-actions {
  visibility: hidden;
}

.contact-card:hover .contact-actions {
  visibility: visible;
}

/* GOMBOK ALAP STÍLUSA */
.btn {
  background-color: #4caf50;
  color: white;
  font-size: 1.25em;
  border: none;
  padding: 8px 16px;
  margin: 0 5px;
  cursor: pointer;
  border-radius: 4px;
}

.btn:hover {
  background-color: #45a049;
}

.btn-add {
  background-color: #2e7d32;
  margin: 0;
  padding: 10px 20px;
}

.btn-add:hover {
  background-color: #1b5e20;
}

.btn-edit {
  background-color: #2196f3;
}

.btn-edit:hover {
  background-color: #1976d2;
}

.btn-delete {
  background-color: #f44336;
}

.btn-delete:hover {
  background-color: #d32f2f;
}
</style>