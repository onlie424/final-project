import React from 'react';

import '../styles/VideoPlayer.css';

export default function VideoPlayer({ contentUrl, title }) {
  const getVideoId = (url) => {
    if (!url) return null;
    const patterns = [
      /youtube\.com\/watch\?v=([^&]+)/,
      /youtube\.com\/embed\/([^?]+)/,
      /youtu\.be\/([^?]+)/,
    ];
    for (let pattern of patterns) {
      const match = url.match(pattern);
      if (match) return match[1];
    }
    return null;
  };

  const videoId = getVideoId(contentUrl);

  if (!videoId) {
    return <div className="video-error">Invalid video URL</div>;
  }

  const embedUrl = `https://www.youtube.com/embed/${videoId}?rel=0&modestbranding=1`;
  const youtubeUrl = `https://www.youtube.com/watch?v=${videoId}`;

  return (
    <div className="video-player-container">
      <div className="video-header">
        <h3>{title}</h3>
      </div>

      <div className="video-wrapper">
        <iframe
          src={embedUrl}
          title={title}
          frameBorder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
          className="video-iframe"
        />
      </div>

      <div className="video-footer">
        <a 
          href={youtubeUrl} 
          target="_blank" 
          rel="noopener noreferrer"
          className="watch-on-youtube"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
          </svg>
          Watch on YouTube
        </a>
      </div>
    </div>
  );
}