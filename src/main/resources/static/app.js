// ── CONFIG ──────────────────────────────────────────────────────────
const API = 'http://localhost:8080/api';

// ── AUTH STATE ──────────────────────────────────────────────────────
const Auth = {
  get() {
    try { return JSON.parse(localStorage.getItem('train_user') || 'null'); } catch { return null; }
  },
  set(user) { localStorage.setItem('train_user', JSON.stringify(user)); },
  clear() { localStorage.removeItem('train_user'); },
  isAdmin() { return this.get()?.role === 'ADMINISTRATOR'; },
  isLoggedIn() { return !!this.get(); },
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
async function apiGet(path) {
  const res = await fetch(API + path);
  if (!res.ok) throw new Error(await res.text() || `Error ${res.status}`);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const text = await res.text();
  if (!res.ok) throw new Error(text || `Error ${res.status}`);
  return text ? JSON.parse(text) : null;
}

async function apiPut(path, body) {
  const res = await fetch(API + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const text = await res.text();
  if (!res.ok) throw new Error(text || `Error ${res.status}`);
  return text ? JSON.parse(text) : null;
}

async function apiDelete(path) {
  const res = await fetch(API + path, { method: 'DELETE' });
  if (!res.ok) throw new Error(`Error ${res.status}`);
}

// ── NAV RENDERING ────────────────────────────────────────────────────
function renderNav(activePage) {
  const user = Auth.get();
  const isAdmin = user?.role === 'ADMINISTRATOR';

  const links = [
    { href: 'index.html', label: 'Search Routes', page: 'search' },
    { href: 'book.html',  label: 'Book Tickets',  page: 'book',  requiresAuth: true },
    { href: 'bookings.html', label: 'My Bookings', page: 'bookings', requiresAuth: true },
  ];

  if (isAdmin) {
    links.push({ href: 'admin.html', label: 'Admin', page: 'admin' });
  }

  const linksHtml = links
    .filter(l => !l.requiresAuth || user)
    .map(l => `<a href="${l.href}" class="nav-link${activePage === l.page ? ' active' : ''}">${l.label}</a>`)
    .join('');

  const rightHtml = user
    ? `<span class="nav-user">${user.username}</span>
       <span class="nav-badge${isAdmin ? ' admin' : ''}">${isAdmin ? 'Admin' : 'Customer'}</span>
       <button class="btn btn-ghost btn-sm" onclick="logout()">Sign out</button>`
    : `<a href="login.html" class="btn btn-ghost btn-sm">Sign in</a>
       <a href="register.html" class="btn btn-primary btn-sm">Register</a>`;

  document.getElementById('nav-placeholder').innerHTML = `
    <nav class="nav">
      <div class="nav-inner">
        <a href="index.html" class="nav-logo">
          <span></span>Railwise
        </a>
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
    btnEl.disabled = true;
  } else {
    btnEl.innerHTML = label || btnEl.dataset.origLabel || btnEl.innerHTML;
    btnEl.disabled = false;
  }
}

function openModal(id) {
  document.getElementById(id).classList.add('open');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

function formatTime(t) {
  if (!t) return '—';
  return t.substring(0, 5);
}

function escHtml(s) {
  return String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

async function bookTrip(trainId, fromStation, toStation, departureTime, arrivalTime) {
  // 1. Ensure the user is logged in
  if (!Auth.isLoggedIn()) {
    alert("Please log in to book a trip.");
    window.location.href = 'login.html';
    return;
  }

  // 2. Get the logged-in user's ID dynamically
  const user = Auth.get();

  // 3. Prepare the request body using the new entity structure
  const requestBody = {
    userId: user.id,
    trainId: trainId,
    numberOfTickets: 1,
    departureStation: fromStation,
    arrivalStation: toStation,
    departureTime: departureTime,
    arrivalTime: arrivalTime
  };

  try {
    // 4. Use your existing apiPost helper
    await apiPost('/bookings', requestBody);
    alert("Trip Booked Successfully!");

    // Optional: Redirect them to the "My Bookings" page so they can see it
    window.location.href = 'bookings.html';
  } catch (error) {
    alert("Failed to book: " + error.message);
  }
}