const form = document.getElementById('rental-form');
const equipmentSelect = document.getElementById('equipment');
const activeRentals = document.getElementById('active-rentals');
const message = document.getElementById('message');

async function loadEquipment() {
    const items = await api.get('/api/equipment?available=true');
    equipmentSelect.replaceChildren();
    items.forEach(item => {
        const option = document.createElement('option');
        option.value = item.id;
        option.textContent = `${item.name} — ${formatMoney(item.dailyRate)}`;
        equipmentSelect.append(option);
    });
}

async function loadActiveRentals() {
    const rentals = await api.get('/api/rentals?status=ACTIVE');
    activeRentals.replaceChildren();
    rentals.forEach(rental => {
        const card = document.createElement('article');
        card.className = 'rental-card';
        const text = document.createElement('p');
        text.textContent = `${rental.equipmentSnapshot.name}, ${rental.customer.name}, ${rental.days} дн.`;
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = 'Принять возврат';
        button.addEventListener('click', () => returnRental(rental.id));
        card.append(text, button);
        activeRentals.append(card);
    });
}

async function returnRental(id) {
    try {
        await api.post(`/api/rentals/${id}/return`);
        showMessage(message, 'Возврат принят.', 'success');
        await Promise.all([loadEquipment(), loadActiveRentals()]);
    } catch (error) {
        showMessage(message, error.message, 'error');
    }
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    const extras = [...form.querySelectorAll('input[type="checkbox"]:checked')].map(input => input.value);
    try {
        await api.post('/api/rentals', {
            equipmentId: equipmentSelect.value,
            customer: {
                name: document.getElementById('customer-name').value,
                phone: document.getElementById('customer-phone').value
            },
            days: Number(document.getElementById('days').value),
            tariff: document.getElementById('tariff').value,
            extras
        });
        showMessage(message, 'Аренда оформлена.', 'success');
        form.reset();
        await Promise.all([loadEquipment(), loadActiveRentals()]);
    } catch (error) {
        showMessage(message, error.message, 'error');
    }
});

Promise.all([loadEquipment(), loadActiveRentals()]).catch(error => showMessage(message, error.message, 'error'));
