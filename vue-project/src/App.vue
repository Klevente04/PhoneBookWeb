<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios' // fetch helyett Axios-t használok

const contacts = ref([])

const isLoading = ref(true)
const error = ref(null)

const isFormVisible = ref(false)
const newContact = ref({
  name: '',
  phoneNumber: '',
  city: ''
})
const searchQuery = ref('')

// Adatbázis Beolvasása Axios-sal
async function fetchContacts() {
  try {
    const response = await axios.get('http://localhost:8080/api/contacts')
    contacts.value = response.data
  } catch (err) {
    error.value = err.message
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

// Keresés a név alapján
const filteredContacts = computed(() => {
  if (searchQuery.value === '') {return contacts.value.sort((a, b) => a.name.localeCompare(b.name))}

  return contacts.value.filter(contact =>
    contact.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  ).sort((a, b) => a.name.localeCompare(b.name))
})

// Új ügyfél hozzáadása
async function saveNewContact() {
  try {
    const response = await axios.post('http://localhost:8080/api/contacts', newContact.value)
    contacts.value.push(response.data)

    isFormVisible.value = false
    newContact.value = { name: '', phoneNumber: '', city: '' }
  } catch (err) {
    console.error('Hiba történt az ügyfél hozzáadásakor:', err)
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
        class="base-input search-input"
        placeholder="Keresés ügyfél neve alapján..."
        v-model="searchQuery"
      />
      <button @click="isFormVisible = true" class="btn btn-add">Új</button>
    </div>

    <div v-if="isFormVisible" class="form-container">
      <h3>Új ügyfél rögzítése</h3>
      
      <input v-model="newContact.name" placeholder="Név" class="base-input form-input" />
      <input v-model="newContact.phoneNumber" placeholder="Telefonszám" class="base-input form-input" />
      <input v-model="newContact.city" placeholder="Város" class="base-input form-input" />
      
      <div class="form-actions">
        <button @click="saveNewContact" class="btn btn-add">Mentés</button>
        <button @click="isFormVisible = false" class="btn btn-delete">Mégse</button>
      </div>
    </div>

    <p v-if="isLoading">Adatok betöltése folyamatban...</p>
    <p v-else-if="error">Hiba történt az adatok lekérésekor: {{ error }}</p>
    <p v-else-if="filteredContacts.length === 0">A lista üres.</p>

    <ul v-else>
      <li v-for="contact in filteredContacts" :key="contact.id" class="contact-card">
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
  color: #0b7410;
  max-width: 900px;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  margin: 0 auto;
}

/* CÍMSOR STÍLUSA */
h2 {
  text-align: center;
  font-size: 2.2em;
  margin: 10px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #0b7410;
}

/* FEJLÉC STÍLUSA */
.header-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  margin-bottom: 25px;
}

p {
  font-size: 1.5em;
  text-align: center;
  font-style: italic;
}

ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

/* KERESŐ MEZŐ STÍLUSA */
.base-input {
  outline: none;
  transition: all 0.3s ease;
  border: 1px solid #0b7410;
}

.base-input:focus {
  border-color: #084d0b;
}

.search-input {
  flex: 1; 
  padding: 10px 20px;
  border-radius: 25px;
  border-width: 2px;
  font-size: 1.5em;
}

/* KÁRTYÁK STÍLUSA */
.contact-card {
  background-color: #e2f1ee;
  margin-bottom: 8px;
  padding: 10px 15px;
  border-radius: 8px;
  border-left: 5px solid #0b7410;
  
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.contact-card:hover {
  background-color: #d1e8e4;
}

.contact-card strong {
  font-size: 1.5em;
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
  background-color: #0b7410;
  color: white;
  font-size: 1.25em;
  border: none;
  padding: 8px 16px;
  margin: 0 5px;
  cursor: pointer;
  border-radius: 4px;
}

.btn:hover {
  background-color: #084d0b;
}

.btn-add {
  margin: 0;
  padding: 10px 20px;
}

.btn-delete {
  background-color: #c0392b;
}

.btn-delete:hover {
  background-color: #962d22;
}

/* ŰRLAP STÍLUSA */
.form-container {
  background-color: #e2f1ee;
  border: 2px dashed #0b7410;
  border-radius: 8px;
  padding: 20px; 
  margin-bottom: 25px; 
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-container h3 {
  margin: 0 0 5px 0;
  font-size: 1.5em;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}

.form-input {
  padding: 10px 15px; 
  border-radius: 4px;
  font-size: 1.2em;
}
</style>