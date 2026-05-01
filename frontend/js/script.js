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
        });
    });

    // Setup Charts
    setupCharts();
    
    // Setup dummy billing
    setupBilling();
});

function setupCharts() {
    // Check if Chart.js is loaded
    if (typeof Chart === 'undefined') return;

    // Patients Chart
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
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

    // Revenue Chart
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
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
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
            if (patientSelect.value) {
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
