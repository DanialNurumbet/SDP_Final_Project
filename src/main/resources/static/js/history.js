// History and reports page. Depends on js/api.js.

const messageBox = document.getElementById('message');
const summaryBox = document.getElementById('summary');
const rentalsBody = document.getElementById('rentalsBody');
const emptyNote = document.getElementById('emptyNote');
const detailsBox = document.getElementById('details');
const statusFilter = document.getElementById('statusFilter');

const EXTRA_NAMES = { INSURANCE: 'Страховка', ACCESSORY_KIT: 'Комплект аксессуаров' };

function el(tag, text, className) {
    const node = document.createElement(tag);
    if (text !== undefined && text !== null) node.textContent = text;
    if (className) node.className = className;
    return node;
}

function showError(error) {
    showMessage(messageBox, error.message || 'Не удалось выполнить операцию', 'error');
}

async function loadSummary() {
    const summary = await api.get('/api/reports/summary');
    summaryBox.replaceChildren();
    const stats = [
        ['Всего аренд', summary.totalCount],
        ['Активных', summary.activeCount],
        ['Завершённых', summary.completedCount],
        ['Сумма оформленных аренд', formatMoney(summary.totalRentalsAmount)]
    ];
    for (const [label, value] of stats) {
        const stat = el('div', null, 'stat');
        stat.append(el('div', label, 'label'), el('div', value, 'value'));
        summaryBox.append(stat);
    }
}

async function loadRentals() {
    const status = statusFilter.value;
    const rentals = await api.get('/api/rentals' + (status ? '?status=' + encodeURIComponent(status) : ''));
    rentalsBody.replaceChildren();
    emptyNote.classList.toggle('hidden', rentals.length > 0);
    for (const rental of rentals) {
        const row = document.createElement('tr');
        const badge = el('span', STATUS_LABELS[rental.status] || rental.status, 'badge ' + rental.status);
        const statusCell = document.createElement('td');
        statusCell.append(badge);
        const detailsButton = el('button', 'Подробнее', 'secondary');
        detailsButton.addEventListener('click', () => showDetails(rental.id));
        const actionCell = document.createElement('td');
        actionCell.append(detailsButton);
        row.append(
            el('td', formatDateTime(rental.createdAt)),
            el('td', rental.customer.name),
            el('td', rental.equipmentSnapshot.name),
            el('td', rental.days + ' дн.'),
            el('td', formatMoney(rental.priceBreakdown.total)),
            statusCell,
            actionCell);
        rentalsBody.append(row);
    }
}

async function showDetails(id) {
    try {
        const r = await api.get('/api/rentals/' + encodeURIComponent(id));
        detailsBox.replaceChildren(el('h3', 'Аренда ' + r.id));
        const list = document.createElement('dl');
        const add = (label, value) => list.append(el('dt', label), el('dd', value));
        add('Статус', STATUS_LABELS[r.status] || r.status);
        add('Клиент', r.customer.name + ', ' + r.customer.phone);
        add('Предмет на момент оформления', r.equipmentSnapshot.name + ' (' + (TYPE_LABELS[r.equipmentSnapshot.type] || r.equipmentSnapshot.type) + ')');
        add('Цена за день', formatMoney(r.equipmentSnapshot.dailyRate));
        add('Срок', r.days + ' дн.');
        add('Тариф', TARIFF_LABELS[r.tariff] || r.tariff);
        add('Оформлена', formatDateTime(r.createdAt));
        add('Возвращена', formatDateTime(r.returnedAt));
        detailsBox.append(list);

        const b = r.priceBreakdown;
        const breakdown = document.createElement('dl');
        const addB = (label, value) => breakdown.append(el('dt', label), el('dd', value));
        addB('Базовая стоимость', formatMoney(b.baseCost));
        addB('Скидка', formatMoney(b.discount));
        addB('После скидки', formatMoney(b.discountedCost));
        for (const line of b.extras) {
            addB(EXTRA_NAMES[line.extra] || line.extra, formatMoney(line.cost));
        }
        addB('Итого', formatMoney(b.total));
        detailsBox.append(el('h4', 'Разбивка стоимости'), breakdown);
        detailsBox.classList.remove('hidden');
        detailsBox.scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        showError(error);
    }
}

async function refresh() {
    clearMessage(messageBox);
    try {
        await Promise.all([loadSummary(), loadRentals()]);
    } catch (error) {
        showError(error);
    }
}

statusFilter.addEventListener('change', refresh);
refresh();
