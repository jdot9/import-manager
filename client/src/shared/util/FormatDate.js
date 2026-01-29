/**
 * Format a date string for display
 * @param {string} dateString - The date string to format
 * @returns {string} Formatted date string or '-' if no date provided
 */
export const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
};
