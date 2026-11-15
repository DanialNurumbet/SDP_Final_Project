// не забудьте подкл   <script src="js/api.js"></script>

class ApiError extends Error {
    constructor(status, code, message, fieldErrors) {
        super(message);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors || {};
    }
}

async function apiRequest(method, url, body) {
    const options = { method, headers: {} };
    if (body !== undefined) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(body);
    }
    let response;
    try {
        response = await fetch(url, options);
    } catch (e) {
        throw new ApiError(0, 'NETWORK_ERROR', 'Сервер недоступен. Проверьте, что приложение запущено.', {});
    }
    if (response.status === 204) return null;
    let data = null;
    try { data = await response.json(); } catch (e) { /* тело не JSON */ }
    if (!response.ok) {
        throw new ApiError(response.status,
            data && data.code ? data.code : 'UNKNOWN_ERROR',
            data && data.message ? data.message : 'Не удалось выполнить операцию',
            data && data.fieldErrors ? data.fieldErrors : {});
    }
    return data;
}

const api = {
    get:    (url)       => apiRequest('GET', url),
    post:   (url, body) => apiRequest('POST', url, body === undefined ? {} : body),
    put:    (url, body) => apiRequest('PUT', url, body),
    delete: (url)       => apiRequest('DELETE', url)
};

//  Только textContent: пользовательский текст не исполняется как HTML.
function showMessage(element, text, type) {
    element.textContent = text;
    element.className = 'message ' + (type === 'error' ? 'error' : 'success');
}

function clearMessage(element) {
    element.textContent = '';
    element.className = 'hidden';
}

function formatMoney(value) {
    const number = Number(value);
    return number.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' KZT';
}

function formatDateTime(isoString) {
    return isoString ? new Date(isoString).toLocaleString('ru-RU') : '—';
}

const TYPE_LABELS = { BICYCLE: 'Велосипед', BALL: 'Мяч', RACKET: 'Ракетка' };
const TARIFF_LABELS = { STANDARD: 'Стандартный', LONG_TERM: 'Долгосрочный (−10% от 7 дней)' };
const EXTRA_LABELS = { INSURANCE: 'Страховка (200 KZT/день)', ACCESSORY_KIT: 'Комплект аксессуаров (300 KZT)' };
const STATUS_LABELS = { ACTIVE: 'Активна', COMPLETED: 'Завершена' };
