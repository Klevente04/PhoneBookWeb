<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios' // fetch helyett Axios-t használok

axios.defaults.baseURL = 'http://localhost:8080/api/contacts'

const contacts = ref([])

const isLoading = ref(true)
const error = ref(null)

const isFormVisible = ref(false)
const isEditing = ref(false)
const newContact = ref({ name: '', phoneNumber: '', city: '' })

const searchQuery = ref('')

// -- FETCH FÜGGVÉNY --
// Adatbázis Beolvasása Axios-sal
async function fetchContacts() {
  try {
    // Paraméterként a kereső mező
    const response = await axios.get('', {
      params: {
        name: searchQuery.value
      }
    })
    contacts.value = response.data

  } catch (err) {
    error.value = err.message
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

// Keresőmotor figyelése, adatok betöltése
let timeout = null
watch(searchQuery, () => {
  //  300ms után végezzen csak keresést
  timeout = setTimeout(() => {
    fetchContacts();
  }, 300);
})

// Az adatok betöltése a komponens mountolásakor
onMounted(() => {
  fetchContacts()
})


// -- CRUD FÜGGVÉNYEK --
// Keresés a név alapján (Üres sor esetén minden)
const filteredContacts = computed(() => {
  // Másolat készítése a contacts tömbből, ABC sorrend
  let result = [...contacts.value]
  return result.sort((a, b) => a.name.localeCompare(b.name))
})

// Új ügyfél hozzáadása
async function saveNewContact() {
  try {
    const response = await axios.post('', newContact.value)
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
    await axios.delete(`/${contactId}`)
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
    const response = await axios.put(`/${contactId}`, updatedData)
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
    const response = await axios.get(`/${contactId}/export`, {responseType: 'blob'});

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


// -- FORMS KONTROL --
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
// Forms Mégse
function cancelForm() {
  isFormVisible.value = false
  newContact.value = { name: '', phoneNumber: '', city: '' }
}
// Forms Mentés, ellenőrzés
async function submitForm() {

  const phoneRegex = /^\+\d{1,3} \d{1,2} \d{3} \d{4}$/;
  const textRegex = /^[a-zA-ZáéíóöőúüűÁÉÍÓÖŐÚÜŰ\s]+$/;

  // Változó trim-elése és szóközök normalizálása
  newContact.value.name = newContact.value.name.trim().replace(/\s+/g, ' ');
  newContact.value.city = newContact.value.city.trim().replace(/\s+/g, ' ');
  newContact.value.phoneNumber = newContact.value.phoneNumber.trim().replace(/\s+/g, ' ');
  
  // Név ellenőrzése
  if (newContact.value.name === '' || newContact.value.name.length > 60 || !textRegex.test(newContact.value.name)) {
    alert('Hibás név! A mező nem lehet üres, és maximum 60 karakter hosszú lehet. Csak betűket és szóközt tartalmazhat.');
    return;
  }

  // Város ellenőrzése
  if (newContact.value.city === '' || newContact.value.city.length > 40 || !textRegex.test(newContact.value.city)) {
    alert('Hibás város! A mező nem lehet üres, és maximum 40 karakter hosszú lehet. Csak betűket és szóközt tartalmazhat.');
    return;
  }

  // Telefonszám ellenőrzése
  if (!phoneRegex.test(newContact.value.phoneNumber)) {
    alert('A telefonszám formátuma hibás! Helyes formátum: +36 20 123 4567');
    return;
  }

  let isSuccess = false

  // Megszakítás esetén nem lép ki a menüből
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
</script>

<template>
  <div class="container">
    <h2>Ügyfél lista</h2>
    
    <div class="header-container">  
      <input class="base-input search-input" placeholder="Keresés ügyfél neve alapján..." v-model="searchQuery" />
      <button @click="openFormAdd()" class="btn btn-add">+</button>
    </div>

    <div v-if="!isFormVisible" >
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
            <button class="btn btn-edit" @click="openFormEdit(contact)">Módosítás</button>
            <button class="btn btn-delete" @click="deleteContact(contact.id)">Törlés</button>
          </div>
        </li>
      </ul>
    </div>

    <div v-else class="form-container">
      <h3>{{ isEditing ? 'Ügyfél módosítása' : 'Ügyfél rögzítése' }}</h3>
      
      <input v-model="newContact.name" placeholder="Név" class="base-input form-input" />
      <input v-model="newContact.phoneNumber" placeholder="Telefonszám (+36 1 123 4567)" class="base-input form-input" />
      <input v-model="newContact.city" placeholder="Város" class="base-input form-input" />
      
      <div class="form-actions">
        <button v-if="isEditing" @click="exportToVCF()" class="btn btn-export">.VCF Letöltése</button>
        <button @click="submitForm()" class="btn btn-add">{{ isEditing ? 'Módosítás' : 'Hozzáadás' }}</button>
        <button @click="cancelForm()" class="btn btn-delete">Mégse</button>
      </div>
    </div>
    
  </div>
</template>

<style scoped src="./assets/App.css"></style>