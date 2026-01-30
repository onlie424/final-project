import React, { useEffect, useRef } from 'react';

import '../../styles/dashboard/DonutChart.css';

export default function DonutChart({ percentage, size = 200, strokeWidth = 20 }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const centerX = size / 2;
    const centerY = size / 2;
    const radius = (size - strokeWidth) / 2;

    // Clear canvas
    ctx.clearRect(0, 0, size, size);

    // Draw background circle
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = strokeWidth;
    ctx.stroke();

    // Calculate percentage colors
    const getColor = (percent) => {
      if (percent >= 85) return '#48bb78'; // Green
      if (percent >= 70) return '#ed8936'; // Orange
      return '#f56565'; // Red
    };

    // Draw progress arc
    const startAngle = -Math.PI / 2;
    const endAngle = startAngle + (2 * Math.PI * percentage) / 100;

    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, startAngle, endAngle);
    ctx.strokeStyle = getColor(percentage);
    ctx.lineWidth = strokeWidth;
    ctx.lineCap = 'round';
    ctx.stroke();

  }, [percentage, size, strokeWidth]);

  return (
    <div className="donut-chart">
      <canvas
        ref={canvasRef}
        width={size}
        height={size}
        className="donut-canvas"
      />
      <div className="donut-center">
        <div className="donut-percentage">{percentage}%</div>
        <div className="donut-label">Complete</div>
      </div>
    </div>
  );
}