import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Recommendations from './Recommendations';

// =========================================================
// Empty state
// =========================================================

describe('Recommendations — empty state', () => {
  it('shows empty message when no masteryGaps or suggestions', () => {
    render(<Recommendations />);
    expect(screen.getByText(/enroll in courses/i)).toBeInTheDocument();
  });

  it('shows empty message when both arrays are empty', () => {
    render(<Recommendations masteryGaps={[]} suggestions={[]} />);
    expect(screen.getByText(/enroll in courses/i)).toBeInTheDocument();
  });

  it('does not render "Needs Attention" section when empty', () => {
    render(<Recommendations masteryGaps={[]} suggestions={[]} />);
    expect(screen.queryByText('Needs Attention')).not.toBeInTheDocument();
  });

  it('does not render "Suggested Next Steps" section when empty', () => {
    render(<Recommendations masteryGaps={[]} suggestions={[]} />);
    expect(screen.queryByText('Suggested Next Steps')).not.toBeInTheDocument();
  });
});

// =========================================================
// masteryGaps — rendering
// =========================================================

describe('Recommendations — masteryGaps', () => {
  const gap = {
    id: 1,
    topic: 'Java Variables',
    moduleLabel: 'Module 1',
    mastery: 20,
    severity: 'STRONG_REVIEW',
    courseId: 10,
  };

  it('renders "Needs Attention" section when masteryGaps is non-empty', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText('Needs Attention')).toBeInTheDocument();
  });

  it('renders the gap topic name', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText('Java Variables')).toBeInTheDocument();
  });

  it('renders the module label', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText('Module 1')).toBeInTheDocument();
  });

  it('renders mastery percentage', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText('20% progress')).toBeInTheDocument();
  });

  it('shows "Strongly" prefix for STRONG_REVIEW severity', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText(/strongly/i)).toBeInTheDocument();
  });

  it('does not show "Strongly" prefix for non-STRONG_REVIEW severity', () => {
    const reviewGap = { ...gap, severity: 'REVIEW' };
    render(<Recommendations masteryGaps={[reviewGap]} />);
    expect(screen.queryByText(/strongly/i)).not.toBeInTheDocument();
  });

  it('renders "Go to Course" button when courseId is present', () => {
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByRole('button', { name: /go to course/i })).toBeInTheDocument();
  });

  it('does not render "Go to Course" button when courseId is absent', () => {
    const noCoursGap = { ...gap, courseId: null };
    render(<Recommendations masteryGaps={[noCoursGap]} />);
    expect(screen.queryByRole('button', { name: /go to course/i })).not.toBeInTheDocument();
  });

  it('calls onNavigate with courseId when "Go to Course" is clicked', () => {
    const onNavigate = vi.fn();
    render(<Recommendations masteryGaps={[gap]} onNavigate={onNavigate} />);
    fireEvent.click(screen.getByRole('button', { name: /go to course/i }));
    expect(onNavigate).toHaveBeenCalledWith(10);
  });

  it('renders multiple gaps', () => {
    const gap2 = { ...gap, id: 2, topic: 'Java Loops' };
    render(<Recommendations masteryGaps={[gap, gap2]} />);
    expect(screen.getByText('Java Variables')).toBeInTheDocument();
    expect(screen.getByText('Java Loops')).toBeInTheDocument();
  });
});

// =========================================================
// masteryGaps — status labels from mastery score
// =========================================================

describe('Recommendations — status label logic', () => {
  it('shows "Needs Review" when mastery < 25', () => {
    const gap = { id: 1, topic: 'Topic', mastery: 10, severity: 'REVIEW', courseId: null };
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText(/needs review/i)).toBeInTheDocument();
  });

  it('shows "Needs Practice" when mastery is between 25 and 49', () => {
    const gap = { id: 1, topic: 'Topic', mastery: 35, severity: 'REVIEW', courseId: null };
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText(/needs practice/i)).toBeInTheDocument();
  });

  it('shows "Making Progress" when mastery is 50 or above', () => {
    const gap = { id: 1, topic: 'Topic', mastery: 60, severity: 'PRACTICE', courseId: null };
    render(<Recommendations masteryGaps={[gap]} />);
    expect(screen.getByText(/making progress/i)).toBeInTheDocument();
  });
});

// =========================================================
// suggestions — rendering
// =========================================================

describe('Recommendations — suggestions', () => {
  const suggestion = {
    id: 1,
    icon: '📘',
    text: 'Continue Java Fundamentals',
    courseId: 10,
  };

  it('renders "Suggested Next Steps" section when suggestions is non-empty', () => {
    render(<Recommendations suggestions={[suggestion]} />);
    expect(screen.getByText('Suggested Next Steps')).toBeInTheDocument();
  });

  it('renders suggestion text', () => {
    render(<Recommendations suggestions={[suggestion]} />);
    expect(screen.getByText('Continue Java Fundamentals')).toBeInTheDocument();
  });

  it('renders suggestion icon', () => {
    render(<Recommendations suggestions={[suggestion]} />);
    expect(screen.getByText('📘')).toBeInTheDocument();
  });

  it('calls onNavigate with courseId when clickable suggestion is clicked', () => {
    const onNavigate = vi.fn();
    render(<Recommendations suggestions={[suggestion]} onNavigate={onNavigate} />);
    fireEvent.click(screen.getByText('Continue Java Fundamentals'));
    expect(onNavigate).toHaveBeenCalledWith(10);
  });

  it('does not call onNavigate when suggestion has no courseId', () => {
    const onNavigate = vi.fn();
    const noCourseSuggestion = { ...suggestion, courseId: null };
    render(<Recommendations suggestions={[noCourseSuggestion]} onNavigate={onNavigate} />);
    fireEvent.click(screen.getByText('Continue Java Fundamentals'));
    expect(onNavigate).not.toHaveBeenCalled();
  });

  it('renders multiple suggestions', () => {
    const s2 = { ...suggestion, id: 2, text: 'Review Module 2' };
    render(<Recommendations suggestions={[suggestion, s2]} />);
    expect(screen.getByText('Continue Java Fundamentals')).toBeInTheDocument();
    expect(screen.getByText('Review Module 2')).toBeInTheDocument();
  });
});

// =========================================================
// Both sections present
// =========================================================

describe('Recommendations — both sections', () => {
  it('renders both sections when both arrays have data', () => {
    const gap = { id: 1, topic: 'Java Variables', mastery: 20, severity: 'REVIEW', courseId: null };
    const suggestion = { id: 1, icon: '📘', text: 'Continue Java', courseId: null };
    render(<Recommendations masteryGaps={[gap]} suggestions={[suggestion]} />);
    expect(screen.getByText('Needs Attention')).toBeInTheDocument();
    expect(screen.getByText('Suggested Next Steps')).toBeInTheDocument();
  });
});
