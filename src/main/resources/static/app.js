// ── CONFIG ──────────────────────────────────────────────────────────
const API = 'http://localhost:8080/api';

// ── AUTH STATE ──────────────────────────────────────────────────────
const Auth = {
  get()        { try { return JSON.parse(localStorage.getItem('train_user') || 'null'); } catch { return null; } },
  getToken()   { return localStorage.getItem('train_token') || null; },
  set(user, token) {
    localStorage.setItem('train_user',  JSON.stringify(user));
    localStorage.setItem('train_token', token);
  },
  clear() {
    localStorage.removeItem('train_user');
    localStorage.removeItem('train_token');
  },
  isAdmin()    { return this.get()?.role === 'ADMINISTRATOR'; },
  isLoggedIn() { return !!this.get() && !!this.getToken(); },
  requireAuth() {
    if (!this.isLoggedIn()) { window.location.href = 'login.html'; return false; }
    return true;
  },
  requireAdmin() {
    if (!this.isAdmin()) { window.location.href = 'index.html'; return false; }
    return true;
  }
};

// ── HTTP HELPERS ─────────────────────────────────────────────────────
function authHeaders() {
  const token = Auth.getToken();
  return token
      ? { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }
      : { 'Content-Type': 'application/json' };
}

async function apiGet(path) {
  const res = await fetch(API + path, { headers: authHeaders() });
  if (!res.ok) throw new Error(await res.text() || `Error ${res.status}`);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method:  'POST',
    headers: authHeaders(),
    body:    JSON.stringify(body)
  });
  const text = await res.text();
  if (!res.ok) throw new Error(text || `Error ${res.status}`);
  return text ? JSON.parse(text) : null;
}

async function apiPut(path, body) {
  const res = await fetch(API + path, {
    method:  'PUT',
    headers: authHeaders(),
    body:    JSON.stringify(body)
  });
  const text = await res.text();
  if (!res.ok) throw new Error(text || `Error ${res.status}`);
  return text ? JSON.parse(text) : null;
}

async function apiDelete(path) {
  const res = await fetch(API + path, { method: 'DELETE', headers: authHeaders() });
  if (!res.ok) throw new Error(`Error ${res.status}`);
}

// ── NAV RENDERING ────────────────────────────────────────────────────
function renderNav(activePage) {
  const user    = Auth.get();
  const isAdmin = user?.role === 'ADMINISTRATOR';

  const links = [
    { href: 'index.html',    label: 'Search Routes', page: 'search' },
    { href: 'book.html',     label: 'Book Tickets',  page: 'book',     requiresAuth: true },
    { href: 'bookings.html', label: 'My Bookings',   page: 'bookings', requiresAuth: true },
  ];
  if (isAdmin) links.push({ href: 'admin.html', label: 'Admin', page: 'admin' });

  const linksHtml = links
      .filter(l => !l.requiresAuth || user)
      .map(l => `<a href="${l.href}" class="nav-link${activePage === l.page ? ' active' : ''}">${l.label}</a>`)
      .join('');

  const rightHtml = user
      ? `<span class="nav-user">${user.username}</span>
       <span class="nav-badge${isAdmin ? ' admin' : ''}">${isAdmin ? 'Admin' : 'Customer'}</span>
       <button class="btn btn-ghost btn-sm" onclick="logout()">Sign out</button>`
      : `<a href="login.html"    class="btn btn-ghost btn-sm">Sign in</a>
       <a href="register.html" class="btn btn-primary btn-sm">Register</a>`;

  document.getElementById('nav-placeholder').innerHTML = `
    <nav class="nav">
      <div class="nav-inner">
        <a href="index.html" class="nav-logo"><span></span>Railwise</a>
        <div class="nav-links">${linksHtml}</div>
        <div class="nav-right">${rightHtml}</div>
      </div>
    </nav>`;
}

function logout() {
  Auth.clear();
  window.location.href = 'login.html';
}

// ── UI HELPERS ────────────────────────────────────────────────────────
function showAlert(containerId, message, type = 'error') {
  const el = document.getElementById(containerId);
  if (!el) return;
  const icons = { error: '✕', success: '✓', info: 'ℹ', warning: '⚠' };
  el.innerHTML = `<div class="alert alert-${type}"><span>${icons[type]}</span><span>${message}</span></div>`;
  el.classList.remove('hidden');
}

function clearAlert(containerId) {
  const el = document.getElementById(containerId);
  if (el) { el.innerHTML = ''; el.classList.add('hidden'); }
}

function setLoading(btnEl, loading, label = null) {
  if (loading) {
    btnEl.dataset.origLabel = btnEl.innerHTML;
    btnEl.innerHTML = `<span class="spinner"></span>`;
    btnEl.disabled  = true;
  } else {
    btnEl.innerHTML = label || btnEl.dataset.origLabel || btnEl.innerHTML;
    btnEl.disabled  = false;
  }
}

function openModal(id)  { document.getElementById(id).classList.add('open');    }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function formatTime(t) { return t ? t.substring(0, 5) : '—'; }

function escHtml(s) {
  return String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── LOGIN HELPER (called by login.html) ──────────────────────────────
async function doLogin(username, password) {
  const data = await apiPost('/home-page/login', { username, password });
  // Server now returns { token, user }
  Auth.set(data.user, data.token);
  return data.user;
}

// ── BOOK TRIP ────────────────────────────────────────────────────────
async function bookTrip(trainId, fromStation, toStation, departureTime, arrivalTime) {
  if (!Auth.isLoggedIn()) {
    alert('Please log in to book a trip.');
    window.location.href = 'login.html';
    return;
  }

  const user = Auth.get();
  const requestBody = {
    userId:           user.id,
    trainId:          trainId,
    numberOfTickets:  1,
    departureStation: fromStation,
    arrivalStation:   toStation,
    departureTime:    departureTime,
    arrivalTime:      arrivalTime
  };

  try {
    await apiPost('/bookings', requestBody);
    alert('Trip Booked Successfully!');
    window.location.href = 'bookings.html';
  } catch (error) {
    alert('Failed to book: ' + error.message);
  }
}
