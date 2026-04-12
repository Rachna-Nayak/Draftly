const AUTH_STORAGE_KEY = 'draftly.auth';

const ROLE_ALIASES = {
  AUTHOR: 'AUTHOR',
  STUDENT_RESEARCHER: 'AUTHOR',
  REVIEWER: 'REVIEWER',
  FACULTY_SUPERVISOR: 'REVIEWER',
  ADMIN: 'ADMIN',
  SYSTEM_ADMINISTRATOR: 'ADMIN',
};

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
  const rawRole = getAuthSession()?.role;
  return rawRole ? (ROLE_ALIASES[String(rawRole).toUpperCase()] || null) : null;
}

export function hasAnyRole(roles = []) {
  const currentRole = getCurrentUserRole();
  if (!currentRole) {
    return false;
  }
  if (!roles.length) {
    return true;
  }

  return roles
    .map((role) => ROLE_ALIASES[String(role).toUpperCase()] || String(role).toUpperCase())
    .includes(currentRole);
}
