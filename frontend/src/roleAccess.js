export const USER_ROLES = {
  AUTHOR: 'AUTHOR',
  REVIEWER: 'REVIEWER',
  ADMIN: 'ADMIN',
};

const LEGACY_TO_CANONICAL_ROLE = {
  STUDENT_RESEARCHER: USER_ROLES.AUTHOR,
  FACULTY_SUPERVISOR: USER_ROLES.REVIEWER,
  SYSTEM_ADMINISTRATOR: USER_ROLES.ADMIN,
};

export function normalizeRole(role) {
  if (!role) {
    return null;
  }
  return LEGACY_TO_CANONICAL_ROLE[role] || role;
}

export const ROLE_HOME_PATHS = {
  [USER_ROLES.AUTHOR]: '/projects',
  [USER_ROLES.REVIEWER]: '/review-queue',
  [USER_ROLES.ADMIN]: '/metrics',
};

export function getRoleHomePath(role) {
  return ROLE_HOME_PATHS[normalizeRole(role)] || '/projects';
}

export function getRoleDisplayName(role) {
  switch (normalizeRole(role)) {
    case USER_ROLES.AUTHOR:
      return 'Author';
    case USER_ROLES.REVIEWER:
      return 'Reviewer';
    case USER_ROLES.ADMIN:
      return 'Administrator';
    default:
      return 'Member';
  }
}

export function getNavItemsForRole(role) {
  const normalizedRole = normalizeRole(role);
  const baseItems = [
    { to: '/search', label: 'Search Literature' },
  ];

  if (normalizedRole === USER_ROLES.REVIEWER) {
    return [
      ...baseItems,
      { to: '/submissions', label: 'Submissions' },
      { to: '/review-queue', label: 'Review Queue' },
    ];
  }

  if (normalizedRole === USER_ROLES.ADMIN) {
    return [
      { to: '/projects', label: 'Projects' },
      ...baseItems,
      { to: '/submissions', label: 'Submissions' },
      { to: '/review-queue', label: 'Review Queue' },
      { to: '/reviewer-assignment', label: 'Reviewer Assignment' },
      { to: '/metrics', label: 'Metrics' },
      { to: '/analytics', label: 'Analytics' },
      { to: '/notifications', label: 'Notifications' },
    ];
  }

  return [
    { to: '/projects', label: 'Projects' },
    ...baseItems,
    { to: '/submissions', label: 'Submissions' },
  ];
}