
export const getInitials = (name) => {
  if (!name) return 'A';
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
};


export const truncateText = (text, maxLength = 50) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
