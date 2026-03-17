const API = '';

const shortenForm = document.getElementById('shortenForm');
const urlInput = document.getElementById('urlInput');
const aliasInput = document.getElementById('aliasInput');
const expiryInput = document.getElementById('expiryInput');
const shortenBtn = document.getElementById('shortenBtn');
const resultCard = document.getElementById('resultCard');
const shortUrlDisplay = document.getElementById('shortUrlDisplay');
const originalUrlDisplay = document.getElementById('originalUrlDisplay');
const expiryDisplay = document.getElementById('expiryDisplay');
const copyBtn = document.getElementById('copyBtn');
const errorMsg = document.getElementById('errorMsg');
const statsGrid = document.getElementById('statsGrid');
const statsEmpty = document.getElementById('statsEmpty');
const toast = document.getElementById('toast');

// shorten url on form submit
shortenForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError();
    resultCard.classList.add('hidden');

    const url = urlInput.value.trim();
    if (!url) return;

    shortenBtn.classList.add('loading');
    shortenBtn.querySelector('span').textContent = 'Creating...';

    try {
        const body = { url };
        const alias = aliasInput.value.trim();
        if (alias) body.customAlias = alias;
        const expiry = parseInt(expiryInput.value);
        if (!isNaN(expiry) && expiry > 0) body.expiryDays = expiry;

        const res = await fetch(`${API}/api/shorten`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await res.json();

        if (!res.ok) {
            showError(data.error || 'Something went wrong');
            return;
        }

        // show result
        shortUrlDisplay.textContent = data.shortUrl;
        originalUrlDisplay.textContent = '→ ' + data.originalUrl;
        if (data.expiresAt) {
            expiryDisplay.textContent = 'Expires: ' + new Date(data.expiresAt).toLocaleDateString('en-IN', {
                day: 'numeric', month: 'short', year: 'numeric'
            });
        } else {
            expiryDisplay.textContent = 'No expiration';
        }
        resultCard.classList.remove('hidden');
        loadAllUrls();

        urlInput.value = '';
        aliasInput.value = '';
        expiryInput.value = '';
    } catch (err) {
        showError('Network error. Is the server running?');
    } finally {
        shortenBtn.classList.remove('loading');
        shortenBtn.querySelector('span').textContent = 'Shorten';
    }
});

// copy short url
copyBtn.addEventListener('click', async () => {
    const url = shortUrlDisplay.textContent;
    try {
        await navigator.clipboard.writeText(url);
        showToast('Copied to clipboard!');
    } catch {
        // fallback
        const ta = document.createElement('textarea');
        ta.value = url;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        showToast('Copied!');
    }
});

// load all urls for the analytics section
async function loadAllUrls() {
    try {
        const res = await fetch(`${API}/api/urls`);
        const urls = await res.json();

        if (!urls.length) {
            statsGrid.innerHTML = '';
            statsEmpty.classList.remove('hidden');
            return;
        }

        statsEmpty.classList.add('hidden');
        statsGrid.innerHTML = urls.map(u => buildCard(u)).join('');
        attachCardListeners();
    } catch (err) {
        console.error('Failed to load URLs:', err);
    }
}

function buildCard(url) {
    const created = new Date(url.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    const expired = url.expiresAt && new Date(url.expiresAt) < new Date();
    const expiryText = expired
        ? '<span style="color:var(--danger)">Expired</span>'
        : url.expiresAt
            ? 'Expires: ' + new Date(url.expiresAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })
            : 'No expiry';

    return `
        <div class="link-card" data-code="${url.shortCode}">
            <div class="link-card__header">
                <a href="${url.shortUrl}" target="_blank" class="link-card__code">${url.shortUrl}</a>
                <div class="link-card__clicks">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                    ${url.clickCount} click${url.clickCount !== 1 ? 's' : ''}
                </div>
            </div>
            <div class="link-card__url">${url.originalUrl}</div>
            <div class="link-card__footer">
                <span class="link-card__date">${created} · ${expiryText}</span>
                <div class="link-card__actions">
                    <button class="link-card__action copy-action" data-url="${url.shortUrl}" title="Copy">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                            <rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                        </svg>
                    </button>
                    <button class="link-card__action link-card__action--delete delete-action" data-code="${url.shortCode}" title="Delete">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                            <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                    </button>
                </div>
            </div>
        </div>`;
}

function attachCardListeners() {
    document.querySelectorAll('.copy-action').forEach(btn => {
        btn.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(btn.dataset.url);
                showToast('Copied!');
            } catch { showToast('Failed to copy'); }
        });
    });

    document.querySelectorAll('.delete-action').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (!confirm('Delete this link?')) return;
            try {
                const res = await fetch(`${API}/api/urls/${btn.dataset.code}`, { method: 'DELETE' });
                if (res.ok) { showToast('Deleted'); loadAllUrls(); }
                else showToast('Failed to delete');
            } catch { showToast('Network error'); }
        });
    });
}

function showError(msg) {
    errorMsg.textContent = msg;
    errorMsg.classList.remove('hidden');
}

function hideError() { errorMsg.classList.add('hidden'); }

function showToast(msg) {
    toast.textContent = msg;
    toast.classList.remove('hidden');
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.classList.add('hidden'), 300);
    }, 2000);
}

// load on page ready
document.addEventListener('DOMContentLoaded', loadAllUrls);
