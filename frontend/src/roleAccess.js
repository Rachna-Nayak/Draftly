export const USER_ROLES = {
  AUTHOR: 'STUDENT_RESEARCHER',
  REVIEWER: 'FACULTY_SUPERVISOR',
  ADMIN: 'SYSTEM_ADMINISTRATOR',
};

export const ROLE_HOME_PATHS = {
  [USER_ROLES.AUTHOR]: '/projects',
  [USER_ROLES.REVIEWER]: '/review-queue',
  [USER_ROLES.ADMIN]: '/metrics',
};

export function getRoleHomePath(role) {
  return ROLE_HOME_PATHS[role] || '/projects';
}

export function getRoleDisplayName(role) {
  switch (role) {
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
  const baseItems = [
    { to: '/search', label: 'Search Literature' },
  ];

  if (role === USER_ROLES.REVIEWER) {
    return [
      ...baseItems,
      { to: '/submissions', label: 'Submissions' },
      { to: '/review-queue', label: 'Review Queue' },
    ];
  }

  if (role === USER_ROLES.ADMIN) {
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