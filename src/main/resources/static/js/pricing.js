const form = document.getElementById('pricing-form');
const equipmentSelect = document.getElementById('equipment');
const calculateButton = document.getElementById('calculate');
const breakdown = document.getElementById('breakdown');
const statusText = document.getElementById('pricing-status');
const errorMessage = document.getElementById('pricing-error');
const quotePrompt = 'Выберите предмет и нажмите «Рассчитать».';
let requestVersion = 0;

function addPriceLine(label, amount, isTotal = false) {
    const row = document.createElement('div');
    const term = document.createElement('dt');
    const value = document.createElement('dd');
    term.textContent = label;
    value.textContent = formatMoney(amount);
    row.classList.toggle('pricing-total', isTotal);
    row.append(term, value);
    breakdown.append(row);
}

async function loadEquipment() {
    try {
        const items = await api.get('/api/equipment');
        equipmentSelect.options[0].textContent = items.length ? 'Выберите предмет' : 'В каталоге нет предметов';
        for (const item of items) {
            const option = document.createElement('option');
            option.value = item.id;
            option.textContent = item.name + ' — ' + formatMoney(item.dailyRate) + '/день';
            equipmentSelect.append(option);
        }
        equipmentSelect.disabled = items.length === 0;
        calculateButton.disabled = items.length === 0;
        if (!items.length) {
            statusText.textContent = 'Добавьте предмет в каталоге, чтобы рассчитать стоимость.';
        }
    } catch (error) {
        equipmentSelect.options[0].textContent = 'Каталог недоступен';
        statusText.textContent = '';
        showMessage(errorMessage, error.message, 'error');
    }
}

form.addEventListener('input', () => {
    requestVersion++;
    breakdown.hidden = true;
    statusText.textContent = quotePrompt;
    clearMessage(errorMessage);
    calculateButton.disabled = equipmentSelect.disabled;
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const version = ++requestVersion;
    const fields = new FormData(form);
    clearMessage(errorMessage);
    breakdown.hidden = true;
    calculateButton.disabled = true;
    statusText.textContent = 'Рассчитываем стоимость…';

    try {
        const price = await api.post('/api/pricing/quote', {
            equipmentId: fields.get('equipmentId'),
            days: Number(fields.get('days')),
            tariff: fields.get('tariff'),
            extras: fields.getAll('extras')
        });
        if (version !== requestVersion) return;
        breakdown.replaceChildren();
        addPriceLine('Базовая стоимость', price.baseCost);
        addPriceLine('Скидка', price.discount);
        addPriceLine('Стоимость после скидки', price.discountedCost);
        for (const { extra, cost } of price.extras) {
            addPriceLine(EXTRA_LABELS[extra] || extra, cost);
        }
        addPriceLine('Итого', price.total, true);
        breakdown.hidden = false;
        statusText.textContent = 'Расчёт готов. Итого ' + formatMoney(price.total) + '.';
    } catch (error) {
        if (version !== requestVersion) return;
        statusText.textContent = '';
        const details = Object.values(error.fieldErrors || {});
        showMessage(errorMessage, details.length ? details.join(' ') : error.message, 'error');
    } finally {
        if (version === requestVersion) calculateButton.disabled = false;
    }
});

loadEquipment();
