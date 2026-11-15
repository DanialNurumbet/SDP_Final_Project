(() => {
    const form = document.getElementById('equipment-form');
    const fields = {
        type: document.getElementById('equipment-type'),
        name: document.getElementById('equipment-name'),
        dailyRate: document.getElementById('equipment-rate')
    };
    const list = document.getElementById('equipment-list');
    const message = document.getElementById('catalog-message');
    const listError = document.getElementById('catalog-error');
    const status = document.getElementById('catalog-status');
    const saveButton = document.getElementById('save-equipment');
    const cancelButton = document.getElementById('cancel-edit');
    let editingId = null;
    let busy = false;
    let loadVersion = 0;

    function element(tag, className, text) {
        const node = document.createElement(tag);
        node.className = className;
        node.textContent = text;
        return node;
    }

    function setBusy(value) {
        busy = value;
        document.getElementById('equipment-fields').disabled = value;
        list.querySelectorAll('button').forEach(button => {
            button.disabled = value || button.dataset.locked === 'true';
        });
    }

    function showFieldErrors(errors = {}) {
        Object.entries(fields).forEach(([name, field]) => {
            document.getElementById(name + '-error').textContent = errors[name] || '';
            field.setAttribute('aria-invalid', errors[name] ? 'true' : 'false');
        });
    }

    function resetEditor() {
        editingId = null;
        form.reset();
        fields.type.disabled = false;
        cancelButton.hidden = true;
        saveButton.textContent = 'Добавить предмет';
        document.getElementById('editor-title').textContent = 'Добавить предмет';
        document.getElementById('editor-note').textContent = 'Все поля обязательны. Цена указана за один день аренды.';
        showFieldErrors();
    }

    function render(items) {
        list.replaceChildren();
        items.forEach(item => {
            const card = element('article', 'card equipment-card', '');
            card.append(element('span', 'equipment-kind', TYPE_LABELS[item.type]),
                element('h3', '', item.name),
                element('span', 'availability ' + (item.available ? 'free' : 'rented'), item.available ? 'Свободен' : 'В аренде'),
                element('p', 'equipment-price', formatMoney(item.dailyRate) + ' / день'),
                element('p', 'equipment-hint', item.usageHint));
            const actions = element('div', 'equipment-actions', '');
            const edit = element('button', 'secondary', 'Редактировать');
            const remove = element('button', 'danger', 'Удалить');
            [edit, remove].forEach(button => {
                button.type = 'button';
                button.disabled = busy || !item.available;
                button.dataset.locked = String(!item.available);
                button.setAttribute('aria-label', button.textContent + ': ' + item.name);
            });
            edit.addEventListener('click', () => editItem(item.id));
            remove.addEventListener('click', () => deleteItem(item));
            actions.append(edit, remove);
            card.append(actions);
            if (!item.available) card.append(element('p', 'locked-note', 'Действия доступны после возврата.'));
            list.append(card);
        });
    }

    async function loadCatalog() {
        const version = ++loadVersion;
        const params = new URLSearchParams();
        const type = document.getElementById('filter-type').value;
        const available = document.getElementById('filter-availability').value;
        if (type) params.set('type', type);
        if (available) params.set('available', available);
        status.textContent = 'Загружаем каталог…';
        list.setAttribute('aria-busy', 'true');
        clearMessage(listError);
        try {
            const items = await api.get('/api/equipment?' + params.toString());
            if (!Array.isArray(items)) throw new Error('Сервер вернул некорректный список предметов.');
            // Медленный предыдущий запрос не должен подменять результат новых фильтров.
            if (version !== loadVersion) return;
            render(items);
            status.textContent = items.length ? 'Найдено предметов: ' + items.length : 'Предметов по этим фильтрам нет.';
        } catch (error) {
            if (version !== loadVersion) return;
            list.replaceChildren();
            status.textContent = 'Каталог не загружен.';
            showMessage(listError, error.message + ' Нажмите «Обновить», чтобы повторить.', 'error');
        } finally {
            if (version === loadVersion) list.setAttribute('aria-busy', 'false');
        }
    }

    async function editItem(id) {
        if (busy) return;
        setBusy(true);
        clearMessage(message);
        try {
            const item = await api.get('/api/equipment/' + id);
            if (!item.available) throw new Error('Предмет уже в аренде. Обновите каталог.');
            editingId = item.id;
            fields.type.value = item.type;
            fields.type.disabled = true;
            fields.name.value = item.name;
            fields.dailyRate.value = item.dailyRate;
            saveButton.textContent = 'Сохранить изменения';
            document.getElementById('editor-title').textContent = 'Редактировать предмет';
            document.getElementById('editor-note').textContent = 'Можно изменить название и цену. Тип предмета сохраняется.';
            cancelButton.hidden = false;
            showFieldErrors();
        } catch (error) {
            showMessage(message, error.message, 'error');
        } finally {
            setBusy(false);
        }
        if (editingId === id) fields.name.focus();
    }

    async function deleteItem(item) {
        if (busy || !window.confirm('Удалить «' + item.name + '» из каталога?')) return;
        setBusy(true);
        clearMessage(message);
        try {
            await api.delete('/api/equipment/' + item.id);
            if (editingId === item.id) resetEditor();
            showMessage(message, 'Предмет удалён из каталога.', 'success');
            await loadCatalog();
        } catch (error) {
            showMessage(message, error.message, 'error');
        } finally {
            setBusy(false);
        }
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (busy) return;
        const name = fields.name.value.trim();
        const dailyRate = fields.dailyRate.valueAsNumber;
        const errors = {};
        if (name.length < 2 || name.length > 100) errors.name = 'Введите от 2 до 100 символов.';
        if (!Number.isFinite(dailyRate) || dailyRate < 0.01 || dailyRate > 1000000 || fields.dailyRate.validity.stepMismatch) {
            errors.dailyRate = 'Введите цену от 0.01 до 1 000 000.00 KZT, не более двух знаков после точки.';
        }
        showFieldErrors(errors);
        if (Object.keys(errors).length) {
            fields[Object.keys(errors)[0]].focus();
            return;
        }
        setBusy(true);
        clearMessage(message);
        const isEditing = editingId !== null;
        try {
            const body = { type: fields.type.value, name, dailyRate };
            if (isEditing) await api.put('/api/equipment/' + editingId, body);
            else await api.post('/api/equipment', body);
            resetEditor();
            showMessage(message, isEditing ? 'Изменения сохранены.' : 'Предмет добавлен в каталог.', 'success');
            await loadCatalog();
        } catch (error) {
            // При отказе сервера введённые значения остаются в форме.
            showMessage(message, error.message, 'error');
            showFieldErrors(error.fieldErrors);
        } finally {
            setBusy(false);
        }
    });

    cancelButton.addEventListener('click', () => { resetEditor(); fields.name.focus(); });
    document.getElementById('catalog-filters').addEventListener('submit', event => { event.preventDefault(); loadCatalog(); });
    document.getElementById('filter-type').addEventListener('change', loadCatalog);
    document.getElementById('filter-availability').addEventListener('change', loadCatalog);
    loadCatalog();
})();
