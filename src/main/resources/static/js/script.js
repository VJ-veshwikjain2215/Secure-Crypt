/* =========================================================
   SecureCrypt - Front-end logic
   Handles: theme toggle, API calls, validation, clipboard,
   download, session history, key strength, ripple, toasts.
   ========================================================= */

const API_BASE = '/api';
let historyCounter = 0;
const MAX_HISTORY_PREVIEW = 40;

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initRipple();
  initCharCounters();
  initKeyVisibilityToggles();
  initKeyStrengthMeter();
  bindEncrypt();
  bindDecrypt();
  bindGenerateKey();
  bindCopyButtons();
  bindDownloadButtons();
  bindClearButtons();
  bindHistoryClear();
});

/* ---------------- THEME TOGGLE ---------------- */
function initTheme() {
  const html = document.documentElement;
  const btn = document.getElementById('themeToggle');
  const saved = sessionStorage.getItem('sc-theme') || 'dark';
  html.setAttribute('data-theme', saved);
  updateThemeIcon(saved);

  btn.addEventListener('click', () => {
    const current = html.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', next);
    sessionStorage.setItem('sc-theme', next);
    updateThemeIcon(next);
  });
}

function updateThemeIcon(theme) {
  const btn = document.getElementById('themeToggle');
  btn.innerHTML = theme === 'dark' ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-stars-fill"></i>';
}

/* ---------------- BUTTON RIPPLE EFFECT ---------------- */
function initRipple() {
  document.querySelectorAll('.sc-ripple').forEach(btn => {
    btn.addEventListener('click', function (e) {
      const rect = this.getBoundingClientRect();
      const circle = document.createElement('span');
      const size = Math.max(rect.width, rect.height);
      circle.className = 'ripple-circle';
      circle.style.width = circle.style.height = `${size}px`;
      circle.style.left = `${e.clientX - rect.left - size / 2}px`;
      circle.style.top = `${e.clientY - rect.top - size / 2}px`;
      this.appendChild(circle);
      setTimeout(() => circle.remove(), 600);
    });
  });
}

/* ---------------- CHARACTER COUNTERS ---------------- */
function initCharCounters() {
  bindCounter('encryptInput', 'encryptCharCount');
  bindCounter('decryptInput', 'decryptCharCount');
}
function bindCounter(inputId, countId) {
  const input = document.getElementById(inputId);
  const count = document.getElementById(countId);
  input.addEventListener('input', () => {
    count.textContent = input.value.length;
  });
}

/* ---------------- SHOW/HIDE KEY ---------------- */
function initKeyVisibilityToggles() {
  toggleVisibility('toggleEncryptKeyVisibility', 'encryptKey');
  toggleVisibility('toggleDecryptKeyVisibility', 'decryptKey');
}
function toggleVisibility(btnId, inputId) {
  const btn = document.getElementById(btnId);
  const input = document.getElementById(inputId);
  btn.addEventListener('click', () => {
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    btn.innerHTML = isPassword ? '<i class="bi bi-eye-slash-fill"></i>' : '<i class="bi bi-eye-fill"></i>';
  });
}

/* ---------------- KEY STRENGTH METER ---------------- */
function initKeyStrengthMeter() {
  const algoSelect = document.getElementById('encryptAlgorithm');
  const keyInput = document.getElementById('encryptKey');
  const wrap = document.getElementById('strengthMeterWrap');
  const hint = document.getElementById('encryptKeyHint');
  const label = document.getElementById('encryptKeyLabel');

  const hints = {
    CAESAR: 'Enter a whole number shift value (e.g. 3). Values wrap automatically 0-25.',
    VIGENERE: 'Enter an alphabetic keyword (e.g. LEMON). Non-letters are ignored.',
    XOR: 'Enter any text key. Longer, more random keys are stronger.',
    AES: 'AES-128 uses your password to derive a secure encryption key (PBKDF2).',
    DES: 'DES uses your password to derive a 56-bit key (legacy algorithm, weak by modern standards).',
    TRIPLE_DES: '3DES uses your password to derive a 192-bit key via PBKDF2.'
  };
  const labels = {
    CAESAR: 'Shift Value', VIGENERE: 'Keyword', XOR: 'XOR Key',
    AES: 'Password / Key', DES: 'Password / Key', TRIPLE_DES: 'Password / Key'
  };

  function refreshHint() {
    const algo = algoSelect.value;
    hint.textContent = hints[algo] || '';
    label.textContent = labels[algo] || 'Password / Key';
    wrap.style.display = (algo === 'AES' || algo === 'DES' || algo === 'TRIPLE_DES') ? 'block' : 'none';
  }

  algoSelect.addEventListener('change', refreshHint);
  refreshHint();

  keyInput.addEventListener('input', debounce(async () => {
    if (wrap.style.display === 'none' || !keyInput.value) {
      return;
    }
    try {
      const res = await fetch(`${API_BASE}/key-strength?key=${encodeURIComponent(keyInput.value)}`);
      const data = await res.json();
      applyStrength(data.result);
    } catch (e) { /* silent - non-critical UX feature */ }
  }, 300));

  function applyStrength(strength) {
    const fill = document.getElementById('strengthFill');
    const label2 = document.getElementById('strengthLabel');
    const map = {
      WEAK: { w: '33%', c: '#EF4444' },
      MEDIUM: { w: '66%', c: '#F59E0B' },
      STRONG: { w: '100%', c: '#10B981' },
      NONE: { w: '0%', c: '#9CA3AF' }
    };
    const cfg = map[strength] || map.NONE;
    fill.style.width = cfg.w;
    fill.style.background = cfg.c;
    label2.textContent = strength;
  }
}

function debounce(fn, delay) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}

/* ---------------- ENCRYPT ---------------- */
function bindEncrypt() {
  const btn = document.getElementById('encryptBtn');
  btn.addEventListener('click', async () => {
    const text = document.getElementById('encryptInput').value;
    const algorithm = document.getElementById('encryptAlgorithm').value;
    const key = document.getElementById('encryptKey').value;

    if (!validateInput(text, 'Please enter some text to encrypt.')) return;
    if (!validateInput(key, 'Please enter a key/password.')) return;

    await runOperation(btn, `${API_BASE}/encrypt`, { text, algorithm, key }, (data) => {
      document.getElementById('encryptResultBox').style.display = 'block';
      document.getElementById('encryptOutput').value = data.result;
      addHistory('Encrypt', algorithm, text, data.result);
      showToast('Text encrypted successfully!', 'success');
    });
  });
}

/* ---------------- DECRYPT ---------------- */
function bindDecrypt() {
  const btn = document.getElementById('decryptBtn');
  btn.addEventListener('click', async () => {
    const text = document.getElementById('decryptInput').value;
    const algorithm = document.getElementById('decryptAlgorithm').value;
    const key = document.getElementById('decryptKey').value;

    if (!validateInput(text, 'Please enter cipher text to decrypt.')) return;
    if (!validateInput(key, 'Please enter the key/password used for encryption.')) return;

    await runOperation(btn, `${API_BASE}/decrypt`, { text, algorithm, key }, (data) => {
      document.getElementById('decryptResultBox').style.display = 'block';
      document.getElementById('decryptOutput').value = data.result;
      addHistory('Decrypt', algorithm, text, data.result);
      showToast('Text decrypted successfully!', 'success');
    });
  });
}

function validateInput(value, message) {
  if (!value || !value.trim()) {
    showToast(message, 'error');
    return false;
  }
  return true;
}

async function runOperation(btn, url, payload, onSuccess) {
  setLoading(btn, true);
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();

    if (!res.ok || !data.success) {
      showToast(data.message || 'Operation failed.', 'error');
      return;
    }
    onSuccess(data);
  } catch (err) {
    showToast('Network error - could not reach the server.', 'error');
  } finally {
    setLoading(btn, false);
  }
}

function setLoading(btn, isLoading) {
  const spinner = btn.querySelector('.sc-spinner');
  const label = btn.querySelector('.btn-label');
  btn.disabled = isLoading;
  spinner.classList.toggle('d-none', !isLoading);
  label.style.opacity = isLoading ? '0.6' : '1';
}

/* ---------------- RANDOM KEY GENERATOR ---------------- */
function bindGenerateKey() {
  const btn = document.getElementById('generateKeyBtn');
  btn.addEventListener('click', async () => {
    try {
      const res = await fetch(`${API_BASE}/generate-key?length=16`);
      const data = await res.json();
      document.getElementById('encryptKey').value = data.result;
      document.getElementById('encryptKey').dispatchEvent(new Event('input'));
      showToast('Random key generated!', 'info');
    } catch (e) {
      showToast('Could not generate key.', 'error');
    }
  });
}

/* ---------------- COPY TO CLIPBOARD ---------------- */
function bindCopyButtons() {
  bindCopy('copyEncryptBtn', 'encryptOutput');
  bindCopy('copyDecryptBtn', 'decryptOutput');
}
function bindCopy(btnId, outputId) {
  document.getElementById(btnId).addEventListener('click', async () => {
    const text = document.getElementById(outputId).value;
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      showToast('Copied to clipboard!', 'success');
    } catch (e) {
      showToast('Failed to copy text.', 'error');
    }
  });
}

/* ---------------- DOWNLOAD AS TXT ---------------- */
function bindDownloadButtons() {
  bindDownload('downloadEncryptBtn', 'encryptOutput', 'encrypted-output.txt');
  bindDownload('downloadDecryptBtn', 'decryptOutput', 'decrypted-output.txt');
}
function bindDownload(btnId, outputId, filename) {
  document.getElementById(btnId).addEventListener('click', () => {
    const text = document.getElementById(outputId).value;
    if (!text) return;
    const blob = new Blob([text], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    showToast('File downloaded!', 'success');
  });
}

/* ---------------- CLEAR BUTTONS ---------------- */
function bindClearButtons() {
  document.getElementById('clearEncryptBtn').addEventListener('click', () => {
    document.getElementById('encryptInput').value = '';
    document.getElementById('encryptKey').value = '';
    document.getElementById('encryptOutput').value = '';
    document.getElementById('encryptResultBox').style.display = 'none';
    document.getElementById('encryptCharCount').textContent = '0';
  });
  document.getElementById('clearDecryptBtn').addEventListener('click', () => {
    document.getElementById('decryptInput').value = '';
    document.getElementById('decryptKey').value = '';
    document.getElementById('decryptOutput').value = '';
    document.getElementById('decryptResultBox').style.display = 'none';
    document.getElementById('decryptCharCount').textContent = '0';
  });
}

/* ---------------- SESSION HISTORY ---------------- */
function addHistory(action, algorithm, input, output) {
  const emptyRow = document.getElementById('historyEmptyRow');
  if (emptyRow) emptyRow.remove();

  historyCounter++;
  const tbody = document.getElementById('historyBody');
  const row = document.createElement('tr');
  row.innerHTML = `
    <td>${historyCounter}</td>
    <td><span class="badge ${action === 'Encrypt' ? 'text-bg-primary' : 'text-bg-info'}">${action}</span></td>
    <td>${algorithm}</td>
    <td class="font-monospace">${truncate(input)}</td>
    <td class="font-monospace">${truncate(output)}</td>
    <td>${new Date().toLocaleTimeString()}</td>
  `;
  tbody.prepend(row);
}
function truncate(str) {
  const escaped = str.replace(/</g, '&lt;').replace(/>/g, '&gt;');
  return escaped.length > MAX_HISTORY_PREVIEW ? escaped.slice(0, MAX_HISTORY_PREVIEW) + '…' : escaped;
}
function bindHistoryClear() {
  document.getElementById('clearHistoryBtn').addEventListener('click', () => {
    historyCounter = 0;
    const tbody = document.getElementById('historyBody');
    tbody.innerHTML = '<tr id="historyEmptyRow"><td colspan="6" class="text-center text-muted py-3">No operations yet in this session.</td></tr>';
  });
}

/* ---------------- TOAST NOTIFICATIONS ---------------- */
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const icons = { success: 'bi-check-circle-fill', error: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };

  const toastEl = document.createElement('div');
  toastEl.className = `toast sc-toast ${type} align-items-center border-0`;
  toastEl.setAttribute('role', 'alert');
  toastEl.innerHTML = `
    <div class="d-flex">
      <div class="toast-body"><i class="bi ${icons[type]} me-2"></i>${message}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>`;
  container.appendChild(toastEl);

  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  toast.show();
  toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}
