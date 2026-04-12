const AUTH_STORAGE_KEY = 'draftly.auth';

export function setAuthSession(session) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
}

export function getAuthSession() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

export function getSessionToken() {
  return getAuthSession()?.sessionToken || null;
}

export function getCurrentUser() {
  return getAuthSession();
}

export function getCurrentUserId() {
  return getAuthSession()?.id || null;
}

export function getCurrentUserRole() {
  return getAuthSession()?.role || null;
}
