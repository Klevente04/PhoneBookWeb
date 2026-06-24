<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios' // fetch helyett Axios-t használok

const contacts = ref([])

const isLoading = ref(true)
const error = ref(null)

const isFormVisible = ref(false)
const isEditing = ref(false)
const newContact = ref({ name: '', phoneNumber: '', city: '' })

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

// Az adatok betöltése a komponens mountolásakor
onMounted(() => {
  fetchContacts()
})


// Keresés a név alapján
const filteredContacts = computed(() => {
  // Másolat készítése a contacts tömbből, hogy ne módosítsuk az eredetit
  let result = [...contacts.value] 

  if (searchQuery.value !== '') {
    result = result.filter(contact =>
      contact.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  return result.sort((a, b) => a.name.localeCompare(b.name))
})

// Forms kontrollálása
// Forms Hozzáadás
function openFormAdd() {
  isFormVisible.value = true
  isEditing.value = false
  newContact.value = { name: '', phoneNumber: '', city: '' }
}
// Forms Módosítás
function openFormEdit(contact) {
  isFormVisible.value = true
  isEditing.value = true
  newContact.value = { ...contact }
}
// Forms Mentés
async function submitForm() {

  const phoneRegex = /^\+\d{1,3} \d{1,2} \d{3} \d{4}$/;
  const textRegex = /^[a-zA-ZáéíóöőúüűÁÉÍÓÖŐÚÜŰ\s]+$/;

  // Itt ténylegesen felülírjuk a Vue változókat: levágjuk a széleket és a dupla szóközöket szimplára cseréljük
  newContact.value.name = newContact.value.name.trim().replace(/\s+/g, ' ');
  newContact.value.city = newContact.value.city.trim().replace(/\s+/g, ' ');
  newContact.value.phoneNumber = newContact.value.phoneNumber.trim().replace(/\s+/g, ' ');
  
  // 1. Név ellenőrzése
  if (newContact.value.name === '' || newContact.value.name.length > 60 || !textRegex.test(newContact.value.name)) {
    alert('Hibás név! A mező nem lehet üres, és maximum 60 karakter hosszú lehet. Csak betűket és szóközt tartalmazhat.');
    return;
  }

  // 2. Város ellenőrzése
  if (newContact.value.city === '' || newContact.value.city.length > 40 || !textRegex.test(newContact.value.city)) {
    alert('Hibás város! A mező nem lehet üres, és maximum 40 karakter hosszú lehet. Csak betűket és szóközt tartalmazhat.');
    return;
  }

  // 3. Telefonszám ellenőrzése (Regex)
  if (!phoneRegex.test(newContact.value.phoneNumber)) {
    alert('A telefonszám formátuma hibás! Helyes formátum: +36 20 123 4567');
    return;
  }

  let isSuccess = false

  if (isEditing.value) {
    isSuccess = await updateContact(newContact.value.id, newContact.value)
  } else {
    await saveNewContact()
    isSuccess = true
  }

  if (isSuccess) {
    isFormVisible.value = false
    newContact.value = { name: '', phoneNumber: '', city: '' }
  }
}

// Új ügyfél hozzáadása
async function saveNewContact() {
  try {
    const response = await axios.post('http://localhost:8080/api/contacts', newContact.value)
    contacts.value.push(response.data)

    isFormVisible.value = false
    newContact.value = { name: '', phoneNumber: '', city: '' }
  } catch (err) {
    console.error('Hiba történt az ügyfél hozzáadásakor:', err)
    alert('Hiba történt az ügyfél hozzáadásakor!')
  }
}

// Törlés
async function deleteContact(contactId) {
  if (!confirm('Biztosan törölni szeretné az ügyfelet?')) {
    return 
  }

  try {
    await axios.delete(`http://localhost:8080/api/contacts/${contactId}`)
    contacts.value = contacts.value.filter(contact => contact.id !== contactId)
  } catch (err) {
    console.error('Hiba történt az ügyfél törlésekor:', err)
    alert('Hiba történt az ügyfél törlésekor!')
  }
}

// Módosítás
async function updateContact(contactId, updatedData) {
  if (!confirm('Biztosan módosítani szeretné az ügyfelet?')) {
    return false
  }

  try {
    const response = await axios.put(`http://localhost:8080/api/contacts/${contactId}`, updatedData)
    const index = contacts.value.findIndex(contact => contact.id === contactId)
    if (index !== -1) {
      contacts.value[index] = response.data
    }
    return true
  } catch (err) {
    console.error('Hiba történt az ügyfél módosításakor:', err)
    alert('Hiba történt az ügyfél módosításakor!')
  }
}

// Exportálás
async function exportToVCF() {
  const contactId = newContact.value.id;

  try {
    const response = await axios.get(`http://localhost:8080/api/contacts/${contactId}/export`, {
      responseType: 'blob' 
    });

    // Letöltött fájl létrehozása és letöltése
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    
    // Fájlnév generálása a letöltéshez
    const fileName = 'Contact_' + newContact.value.name.replace(/\s+/g, '_');
    link.setAttribute('download', `${fileName}.vcf`);
    
    // Rákattintunk a linkre a háttérben, majd eltakarítjuk
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

  } catch (err) {
    console.error('Hiba történt az exportálás során:', err);
    alert('Hiba történt a letöltés során!');
  }
}
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
      <button @click="openFormAdd()" class="btn btn-add">+</button>
    </div>

    <div v-if="isFormVisible" class="form-container">
      <h3>{{ isEditing ? 'Ügyfél módosítása' : 'Ügyfél rögzítése' }}</h3>
      
      <input v-model="newContact.name" placeholder="Név" class="base-input form-input" />
      <input v-model="newContact.phoneNumber" placeholder="Telefonszám (+36 1 123 4567)" class="base-input form-input" />
      <input v-model="newContact.city" placeholder="Város" class="base-input form-input" />
      
      <div class="form-actions">
        <button v-if="isEditing" @click="exportToVCF()" class="btn btn-export">.VCF Letöltése</button>
        <button @click="submitForm()" class="btn btn-add">{{ isEditing ? 'Módosítás' : 'Hozzáadás' }}</button>
        <button @click="isFormVisible = false; newContact = { name: '', phoneNumber: '', city: '' }" class="btn btn-delete">Mégse</button>
      </div>
    </div>

    <p v-else-if="isLoading">Adatok betöltése folyamatban...</p>
    <p v-else-if="error">Hiba történt az adatok lekérésekor: {{ error }}</p>
    <p v-else-if="filteredContacts.length === 0">A lista üres.</p>

    <ul v-else>
      <li v-for="contact in filteredContacts" :key="contact.id" class="contact-card">
        <div>
          <strong>{{ contact.name }}</strong> <br>
          {{ contact.phoneNumber }} | {{ contact.city }}
        </div>

        <div class="contact-actions">
          <button class="btn btn-edit" @click="openFormEdit(contact)">Módosítás</button>
          <button class="btn btn-delete" @click="deleteContact(contact.id)">Törlés</button>
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

.btn-export {
  background-color: #3498db;
}

.btn-export:hover {
  background-color: #2980b9;
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