import { describe, it, expect } from 'vitest';
import { getInitials, truncateText } from './helpers';

// =========================================================
// getInitials
// =========================================================

describe('getInitials', () => {
  it('returns "A" when name is null', () => {
    expect(getInitials(null)).toBe('A');
  });

  it('returns "A" when name is undefined', () => {
    expect(getInitials(undefined)).toBe('A');
  });

  it('returns "A" when name is an empty string', () => {
    expect(getInitials('')).toBe('A');
  });

  it('returns single initial for a one-word name', () => {
    expect(getInitials('Alice')).toBe('A');
  });

  it('returns two initials for a two-word name', () => {
    expect(getInitials('John Doe')).toBe('JD');
  });

  it('returns uppercase initials regardless of input case', () => {
    expect(getInitials('john doe')).toBe('JD');
  });

  it('caps at two characters for names with more than two words', () => {
    expect(getInitials('Mary Jane Watson')).toBe('MJ');
  });
});

// =========================================================
// truncateText
// =========================================================

describe('truncateText', () => {
  it('returns empty string when text is null', () => {
    expect(truncateText(null)).toBe('');
  });

  it('returns empty string when text is undefined', () => {
    expect(truncateText(undefined)).toBe('');
  });

  it('returns empty string when text is empty', () => {
    expect(truncateText('')).toBe('');
  });

  it('returns text unchanged when shorter than default maxLength (50)', () => {
    expect(truncateText('Hello World')).toBe('Hello World');
  });

  it('returns text unchanged when exactly equal to maxLength', () => {
    const exactly50 = 'A'.repeat(50);
    expect(truncateText(exactly50)).toBe(exactly50);
  });

  it('truncates and appends "..." when text exceeds default maxLength', () => {
    const long = 'A'.repeat(60);
    expect(truncateText(long)).toBe('A'.repeat(50) + '...');
  });

  it('respects a custom maxLength', () => {
    expect(truncateText('Hello World', 5)).toBe('Hello...');
  });

  it('does not truncate when text length equals custom maxLength', () => {
    expect(truncateText('Hello', 5)).toBe('Hello');
  });

  it('truncated result is maxLength chars + "..."', () => {
    const result = truncateText('A'.repeat(20), 10);
    expect(result.length).toBe(13); // 10 + 3 for "..."
  });
});
