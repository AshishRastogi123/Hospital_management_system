document.addEventListener('DOMContentLoaded', () => {
    // Navigation Logic
    const menuLinks = document.querySelectorAll('.sidebar-menu a[data-target]');
    const sections = document.querySelectorAll('.section');

    menuLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();

            // Remove active from all links
            menuLinks.forEach(l => l.classList.remove('active'));
            // Add active to clicked link
            link.classList.add('active');

            // Hide all sections
            sections.forEach(sec => sec.classList.remove('active'));

            // Show target section
            const targetId = link.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');

            // On mobile, close sidebar after clicking
            if (window.innerWidth <= 768) {
                document.querySelector('.sidebar').classList.remove('show');
            }
        });
    });

    // Mobile Sidebar Toggle
    const menuToggle = document.getElementById('menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            document.querySelector('.sidebar').classList.toggle('show');
        });
    }

    // Modal Logic
    const modals = {
        'patient': document.getElementById('patient-modal'),
        'doctor': document.getElementById('doctor-modal')
    };

    // Open modals
    document.querySelectorAll('[data-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalType = btn.getAttribute('data-modal');
            if (modals[modalType]) {
                if (modalType === 'patient') editingPatientId = null; // reset if adding new
                modals[modalType].classList.add('active');
            }
        });
    });

    // Close modals
    document.querySelectorAll('.close-modal, .btn-cancel').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            Object.values(modals).forEach(modal => {
                if (modal) modal.classList.remove('active');
            });
            editingPatientId = null;
        });
    });

    // Setup Charts
    setupCharts();

    // Setup dummy billing
    setupBilling();
    
    // API INITIALIZATION
    setTimeout(fetchAllData, 300);
});

// ==========================================
// API INTEGRATION WITH JAVA BACKEND
// ==========================================

const API_BASE = 'http://localhost:8080';
let editingPatientId = null;

async function fetchAllData() {
    fetchPatients();
    fetchDoctors();
    fetchAppointments();
}

// 1. Fetch all patients
async function fetchPatients() {
    try {
        const response = await fetch(`${API_BASE}/patients`);
        const patients = await response.json();
        
        const tableBody = document.querySelector('#patients-section tbody');
        if (!tableBody) return;
        tableBody.innerHTML = '';
        
        const patientSelects = document.querySelectorAll('select[id*="patient"]');
        patientSelects.forEach(select => {
            if(select.id !== 'patientGender') select.innerHTML = '<option value="">Select...</option>';
        });

        patients.forEach(patient => {
            // Populate table
            const row = `
                <tr>
                    <td>#PT-${patient.id}</td>
                    <td>${patient.name}</td>
                    <td>${patient.age}</td>
                    <td>${patient.gender || '-'}</td>
                    <td>${patient.phone || '-'}</td>
                    <td><span class="badge badge-success">Registered</span></td>
                    <td>
                        <div class="action-btns">
                            <button class="btn-icon edit" onclick="editPatient(${patient.id}, '${patient.name.replace(/'/g, "\\'")}', ${patient.age}, '${patient.gender || 'Other'}', '${patient.phone || ''}')"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-icon delete" onclick="deletePatient(${patient.id})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
            
            // Populate dropdowns (Billing, Appointments)
            patientSelects.forEach(select => {
                if(select.id !== 'patientGender') {
                    select.innerHTML += `<option value="${patient.id}">${patient.name} - #PT-${patient.id}</option>`;
                }
            });
        });
    } catch (error) {
        console.error('Error fetching patients:', error);
    }
}

function editPatient(id, name, age, gender, phone) {
    editingPatientId = id;
    document.getElementById('patientName').value = name;
    document.getElementById('patientAge').value = age;
    document.getElementById('patientGender').value = gender;
    document.getElementById('patientPhone').value = phone;
    document.getElementById('patient-modal').classList.add('active');
}

// 2. Add or Update patient
async function addPatientToDB(event) {
    event.preventDefault();
    
    const name = document.getElementById('patientName').value;
    const age = document.getElementById('patientAge').value;
    const gender = document.getElementById('patientGender').value;
    const phone = document.getElementById('patientPhone').value;
    
    const endpoint = editingPatientId ? '/update-patient' : '/add-patient';
    const payload = { name, age: parseInt(age), gender, phone };
    if(editingPatientId) payload.id = editingPatientId;
    
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            alert(editingPatientId ? 'Patient updated successfully!' : 'Patient added successfully!');
            document.getElementById('patient-modal').classList.remove('active');
            document.getElementById('addPatientForm').reset();
            editingPatientId = null;
            fetchPatients();
        } else {
            alert('Failed to save patient.');
        }
    } catch (error) {
        console.error('Error saving patient:', error);
    }
}

// Delete Patient
async function deletePatient(id) {
    if(!confirm("Are you sure you want to delete this patient?")) return;
    try {
        await fetch(`${API_BASE}/delete-patient`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });
        fetchPatients();
    } catch (e) {
        console.error(e);
    }
}

// 3. Fetch all doctors
async function fetchDoctors() {
    try {
        const response = await fetch(`${API_BASE}/doctors`);
        const doctors = await response.json();
        
        const tableBody = document.querySelector('#doctors-section tbody');
        if (!tableBody) return;
        tableBody.innerHTML = '';
        
        const doctorSelect = document.querySelector('#appointment-doctor-select');
        if(doctorSelect) doctorSelect.innerHTML = '<option value="">Select...</option>';

        doctors.forEach(doctor => {
            const row = `
                <tr>
                    <td>#DR-${doctor.id}</td>
                    <td>${doctor.name}</td>
                    <td>${doctor.specialty}</td>
                    <td><span class="badge badge-success">Available</span></td>
                    <td>${doctor.phone || '-'}</td>
                    <td>
                        <div class="action-btns">
                            <button class="btn-icon delete"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
            
            if(doctorSelect) {
                doctorSelect.innerHTML += `<option value="${doctor.id}">${doctor.name} (${doctor.specialty})</option>`;
            }
        });
    } catch (error) {
        console.error('Error fetching doctors:', error);
    }
}

// Add new doctor
async function addDoctorToDB(event) {
    event.preventDefault();
    
    const name = document.getElementById('doctorName').value;
    const specialty = document.getElementById('doctorSpecialty').value;
    const phone = document.getElementById('doctorPhone').value;
    const email = document.getElementById('doctorEmail').value;
    
    try {
        const response = await fetch(`${API_BASE}/add-doctor`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, specialty, phone, email })
        });
        
        if (response.ok) {
            alert('Doctor added successfully!');
            document.getElementById('doctor-modal').classList.remove('active');
            document.getElementById('addDoctorForm').reset();
            fetchDoctors();
        } else {
            alert('Failed to add doctor.');
        }
    } catch (error) {
        console.error('Error adding doctor:', error);
    }
}

// 4. Fetch all appointments
async function fetchAppointments() {
    try {
        const response = await fetch(`${API_BASE}/appointments`);
        const appointments = await response.json();
        
        const tableBody = document.querySelector('#appointments-section tbody');
        if (!tableBody) return;
        tableBody.innerHTML = '';

        appointments.forEach(app => {
            let badgeClass = 'badge-success';
            if(app.status === 'RESCHEDULED') badgeClass = 'badge-warning';
            
            const row = `
                <tr>
                    <td>${app.patient}</td>
                    <td>${app.doctor}</td>
                    <td>${app.date} - ${app.time}</td>
                    <td><span class="badge ${badgeClass}">${app.status || 'Scheduled'}</span></td>
                    <td>
                        <div class="action-btns">
                            <button class="btn-icon edit" onclick="rescheduleAppt(${app.id})" title="Reschedule"><i class="fa-solid fa-calendar-days"></i></button>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    } catch (error) {
        console.error('Error fetching appointments:', error);
    }
}

async function rescheduleAppt(id) {
    const input = prompt("Enter new Date and Time (YYYY-MM-DD HH:MM):");
    if(!input) return;
    
    const parts = input.trim().split(" ");
    if(parts.length !== 2) {
        alert("Invalid format! Please use YYYY-MM-DD HH:MM");
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/reschedule-appointment`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: id, date: parts[0], time: parts[1] })
        });

        if (response.ok) {
            alert('Appointment rescheduled successfully!');
            fetchAppointments();
        } else {
            alert('Failed to reschedule appointment.');
        }
    } catch (error) {
        console.error('Error rescheduling appointment:', error);
    }
}

// Book Appointment
async function bookAppointment() {
    const patientId = document.getElementById('appointment-patient-select').value;
    const doctorId = document.getElementById('appointment-doctor-select').value;
    const datetimeStr = document.getElementById('appointment-datetime').value; // format: "YYYY-MM-DDTHH:MM"

    if (!patientId || !doctorId || !datetimeStr) {
        alert("Please fill all fields to book an appointment.");
        return;
    }

    const [date, time] = datetimeStr.split('T');

    try {
        const response = await fetch(`${API_BASE}/book-appointment`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ patient_id: parseInt(patientId), doctor_id: parseInt(doctorId), date, time })
        });

        if (response.ok) {
            alert('Appointment booked successfully!');
            document.getElementById('appointment-patient-select').value = '';
            document.getElementById('appointment-doctor-select').value = '';
            document.getElementById('appointment-datetime').value = '';
            fetchAppointments(); // Refresh the appointments table
        } else {
            alert('Failed to book appointment.');
        }
    } catch (error) {
        console.error('Error booking appointment:', error);
    }
}

// Attach Form events
document.addEventListener('DOMContentLoaded', () => {
    const addPatientForm = document.getElementById('addPatientForm');
    if (addPatientForm) addPatientForm.addEventListener('submit', addPatientToDB);
    
    const addDoctorForm = document.getElementById('addDoctorForm');
    if (addDoctorForm) addDoctorForm.addEventListener('submit', addDoctorToDB);

    const bookBtn = document.getElementById('book-appointment-btn');
    if (bookBtn) bookBtn.addEventListener('click', bookAppointment);
});


// Charts and Billing Logic
function setupCharts() {
    if (typeof Chart === 'undefined') return;

    const patientCtx = document.getElementById('patientsChart');
    if (patientCtx) {
        new Chart(patientCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'New Patients',
                    data: [65, 59, 80, 81, 56, 85],
                    borderColor: '#0b5cff',
                    backgroundColor: 'rgba(11, 92, 255, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
        });
    }

    const revenueCtx = document.getElementById('revenueChart');
    if (revenueCtx) {
        new Chart(revenueCtx, {
            type: 'doughnut',
            data: {
                labels: ['Consultation', 'Pharmacy', 'Laboratory', 'Surgery'],
                datasets: [{
                    data: [30, 25, 20, 25],
                    backgroundColor: ['#0b5cff', '#00b884', '#ffc107', '#8a2be2']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }
}

function setupBilling() {
    const patientSelect = document.getElementById('bill-patient-select');
    const generateBtn = document.getElementById('generate-bill-btn');
    const billPreview = document.getElementById('bill-preview');
    const printBtn = document.getElementById('print-bill-btn');

    if (generateBtn) {
        generateBtn.addEventListener('click', () => {
            if (patientSelect && patientSelect.value) {
                // Set patient name in bill
                const patientName = patientSelect.options[patientSelect.selectedIndex].text;
                document.querySelector('.bill-info p strong').innerText = patientName.split(' - ')[0];
                document.querySelector('.bill-info p:nth-child(3)').innerText = 'Patient ID: ' + patientName.split(' - ')[1];
                
                billPreview.style.display = 'block';
            } else {
                alert('Please select a patient first.');
            }
        });
    }

    if (printBtn) {
        printBtn.addEventListener('click', () => {
            window.print();
        });
    }
}
