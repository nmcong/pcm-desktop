# Custom Tray Widget với Time Tracking Display

## Table of Contents
1. [Overview](#overview)
2. [Dynamic Tray Icon Implementation](#dynamic-tray-icon-implementation)
3. [Time Tracking Integration](#time-tracking-integration)
4. [Custom Graphics Rendering](#custom-graphics-rendering)
5. [Platform-Specific Optimizations](#platform-specific-optimizations)
6. [Performance Considerations](#performance-considerations)
7. [Configuration & Customization](#configuration--customization)
8. [Testing & Validation](#testing--validation)

## Overview

### Concept
Thay vì sử dụng static icon, chúng ta sẽ tạo **dynamic tray widget** hiển thị thông tin real-time:
- **Check-in time**: Thời gian vào hệ thống  
- **Current session duration**: Thời gian đã làm việc
- **Status indicator**: Working/Break/Idle state
- **Visual progress**: Progress bar cho work session

### Technical Approach
- **Dynamic Image Generation**: Tạo BufferedImage real-time
- **Timer-based Updates**: Cập nhật icon theo schedule
- **Text Rendering**: Custom font và layout
- **Color Coding**: Visual indicators cho different states
- **Tooltip Information**: Detailed info on hover

### Supported Information Display
```yaml
Primary Display:
  - Current time (HH:MM)
  - Session duration (elapsed time)
  - Status indicator (color/icon)

Secondary Display (Tooltip):
  - Check-in time: "Started at 09:15"
  - Total session time: "Active for 2h 45m"
  - Break time: "Break: 15m"
  - Status: "Working / Break / Away"

Advanced Display (Optional):
  - Daily progress bar
  - Productivity score
  - Next break suggestion
```

## Dynamic Tray Icon Implementation

### 1. Dynamic Tray Icon Manager

```java
package com.noteflix.pcm.tray;

import lombok.extern.slf4j.Slf4j;
import com.noteflix.pcm.time.TimeTrackingService;
import com.noteflix.pcm.time.WorkSession;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DynamicTrayIconManager {
    
    private static DynamicTrayIconManager instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private TimeTrackingService timeTrackingService;
    
    // Display configuration
    private TrayDisplayMode displayMode = TrayDisplayMode.TIME_AND_DURATION;
    private TrayDisplayTheme theme = TrayDisplayTheme.AUTO;
    private boolean showSeconds = false;
    private boolean showProgressBar = true;
    
    // Graphics resources
    private Font primaryFont;
    private Font secondaryFont;
    private Color primaryColor;
    private Color backgroundColor;
    private Color accentColor;
    
    // Animation state
    private boolean isBlinking = false;
    private int animationFrame = 0;
    
    public static DynamicTrayIconManager getInstance() {
        if (instance == null) {
            synchronized (DynamicTrayIconManager.class) {
                if (instance == null) {
                    instance = new DynamicTrayIconManager();
                }
            }
        }
        return instance;
    }
    
    private DynamicTrayIconManager() {
        initializeGraphicsResources();
    }
    
    /**
     * Initialize with time tracking service
     */
    public boolean initialize(TimeTrackingService timeService) {
        this.timeTrackingService = timeService;
        
        if (!SystemTray.isSupported()) {
            log.warn("System tray not supported");
            return false;
        }
        
        try {
            systemTray = SystemTray.getSystemTray();
            createInitialTrayIcon();
            systemTray.add(trayIcon);
            
            // Start update scheduler
            startUpdateScheduler();
            
            log.info("Dynamic tray icon initialized successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize dynamic tray icon", e);
            return false;
        }
    }
    
    /**
     * Create initial tray icon with popup menu
     */
    private void createInitialTrayIcon() {
        Dimension traySize = systemTray.getTrayIconSize();
        BufferedImage initialImage = generateTrayImage(traySize);
        
        PopupMenu popup = createPopupMenu();
        trayIcon = new TrayIcon(initialImage, generateTooltipText(), popup);
        trayIcon.setImageAutoSize(true);
        
        // Add click listeners
        trayIcon.addActionListener(e -> handleTrayClick());
        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleTrayDoubleClick();
                }
            }
        });
    }
    
    /**
     * Generate dynamic tray image based on current state
     */
    private BufferedImage generateTrayImage(Dimension size) {
        BufferedImage image = new BufferedImage(
            size.width, size.height, BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = image.createGraphics();
        
        try {
            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Clear background
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, size.width, size.height);
            g2d.setComposite(AlphaComposite.SrcOver);
            
            // Determine colors based on theme
            updateColorsForCurrentTheme();
            
            // Render based on display mode
            switch (displayMode) {
                case CURRENT_TIME:
                    renderCurrentTime(g2d, size);
                    break;
                case SESSION_DURATION:
                    renderSessionDuration(g2d, size);
                    break;
                case TIME_AND_DURATION:
                    renderTimeAndDuration(g2d, size);
                    break;
                case STATUS_INDICATOR:
                    renderStatusIndicator(g2d, size);
                    break;
                case COMPACT_INFO:
                    renderCompactInfo(g2d, size);
                    break;
            }
            
            // Add progress bar if enabled
            if (showProgressBar && timeTrackingService != null) {
                renderProgressBar(g2d, size);
            }
            
            // Add status indicator
            renderSessionStatusIndicator(g2d, size);
            
        } finally {
            g2d.dispose();
        }
        
        return image;
    }
    
    /**
     * Render current time display
     */
    private void renderCurrentTime(Graphics2D g2d, Dimension size) {
        LocalTime now = LocalTime.now();
        String timeStr = now.format(showSeconds ? 
            DateTimeFormatter.ofPattern("HH:mm:ss") : 
            DateTimeFormatter.ofPattern("HH:mm")
        );
        
        g2d.setFont(primaryFont);
        g2d.setColor(primaryColor);
        
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(timeStr, g2d);
        
        int x = (size.width - (int) bounds.getWidth()) / 2;
        int y = (size.height + fm.getAscent()) / 2;
        
        // Add background for better readability
        if (backgroundColor != null) {
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(x - 2, y - fm.getAscent() - 1, 
                (int) bounds.getWidth() + 4, fm.getHeight() + 2, 4, 4);
            g2d.setColor(primaryColor);
        }
        
        g2d.drawString(timeStr, x, y);
    }
    
    /**
     * Render session duration
     */
    private void renderSessionDuration(Graphics2D g2d, Dimension size) {
        if (timeTrackingService == null) {
            renderFallbackIcon(g2d, size);
            return;
        }
        
        WorkSession currentSession = timeTrackingService.getCurrentSession();
        if (currentSession == null) {
            renderNoSessionIcon(g2d, size);
            return;
        }
        
        Duration elapsed = currentSession.getElapsedTime();
        String durationStr = formatDuration(elapsed);
        
        g2d.setFont(primaryFont);
        g2d.setColor(primaryColor);
        
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(durationStr, g2d);
        
        int x = (size.width - (int) bounds.getWidth()) / 2;
        int y = (size.height + fm.getAscent()) / 2;
        
        // Background
        if (backgroundColor != null) {
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(x - 2, y - fm.getAscent() - 1, 
                (int) bounds.getWidth() + 4, fm.getHeight() + 2, 4, 4);
            g2d.setColor(primaryColor);
        }
        
        g2d.drawString(durationStr, x, y);
        
        // Add small clock icon
        renderSmallClockIcon(g2d, size);
    }
    
    /**
     * Render time and duration combined
     */
    private void renderTimeAndDuration(Graphics2D g2d, Dimension size) {
        // Current time (top)
        LocalTime now = LocalTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Session duration (bottom)
        String durationStr = "";
        if (timeTrackingService != null) {
            WorkSession currentSession = timeTrackingService.getCurrentSession();
            if (currentSession != null) {
                Duration elapsed = currentSession.getElapsedTime();
                durationStr = formatDurationCompact(elapsed);
            }
        }
        
        g2d.setFont(secondaryFont); // Smaller font for dual display
        FontMetrics fm = g2d.getFontMetrics();
        
        // Render time
        g2d.setColor(primaryColor);
        Rectangle2D timeBounds = fm.getStringBounds(timeStr, g2d);
        int timeX = (size.width - (int) timeBounds.getWidth()) / 2;
        int timeY = (size.height / 2) - 2;
        g2d.drawString(timeStr, timeX, timeY);
        
        // Render duration if available
        if (!durationStr.isEmpty()) {
            g2d.setColor(accentColor);
            Rectangle2D durationBounds = fm.getStringBounds(durationStr, g2d);
            int durationX = (size.width - (int) durationBounds.getWidth()) / 2;
            int durationY = timeY + fm.getHeight() + 2;
            g2d.drawString(durationStr, durationX, durationY);
        }
    }
    
    /**
     * Render status indicator with basic info
     */
    private void renderStatusIndicator(Graphics2D g2d, Dimension size) {
        // Get current status
        WorkSessionStatus status = getCurrentWorkStatus();
        
        // Status color
        Color statusColor = getStatusColor(status);
        
        // Draw status circle
        int circleSize = Math.min(size.width, size.height) - 4;
        int circleX = (size.width - circleSize) / 2;
        int circleY = (size.height - circleSize) / 2;
        
        g2d.setColor(statusColor);
        g2d.fillOval(circleX, circleY, circleSize, circleSize);
        
        // Add border
        g2d.setColor(primaryColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(circleX, circleY, circleSize, circleSize);
        
        // Add status symbol
        renderStatusSymbol(g2d, status, circleX + circleSize/4, circleY + circleSize/4, circleSize/2);
    }
    
    /**
     * Render compact information display
     */
    private void renderCompactInfo(Graphics2D g2d, Dimension size) {
        if (timeTrackingService == null) {
            renderFallbackIcon(g2d, size);
            return;
        }
        
        WorkSession currentSession = timeTrackingService.getCurrentSession();
        if (currentSession == null) {
            renderNoSessionIcon(g2d, size);
            return;
        }
        
        // Get session info
        Duration elapsed = currentSession.getElapsedTime();
        LocalTime checkInTime = currentSession.getStartTime().toLocalTime();
        
        // Format for compact display
        String checkInStr = checkInTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        String elapsedStr = formatDurationCompact(elapsed);
        
        g2d.setFont(secondaryFont);
        FontMetrics fm = g2d.getFontMetrics();
        
        // Background
        if (backgroundColor != null) {
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(1, 1, size.width - 2, size.height - 2, 6, 6);
        }
        
        // Check-in time (top line)
        g2d.setColor(primaryColor);
        int line1Y = (size.height / 3) + fm.getAscent() / 2;
        drawCenteredText(g2d, "IN: " + checkInStr, size.width, line1Y, fm);
        
        // Elapsed time (bottom line)
        g2d.setColor(accentColor);
        int line2Y = (2 * size.height / 3) + fm.getAscent() / 2;
        drawCenteredText(g2d, elapsedStr, size.width, line2Y, fm);
    }
    
    /**
     * Render progress bar for work session
     */
    private void renderProgressBar(Graphics2D g2d, Dimension size) {
        if (timeTrackingService == null) return;
        
        WorkSession currentSession = timeTrackingService.getCurrentSession();
        if (currentSession == null) return;
        
        // Calculate progress (e.g., towards 8-hour workday)
        Duration elapsed = currentSession.getElapsedTime();
        Duration targetDuration = Duration.ofHours(8); // Configurable
        
        float progress = Math.min(1.0f, (float) elapsed.toMinutes() / targetDuration.toMinutes());
        
        // Progress bar dimensions
        int barWidth = size.width - 4;
        int barHeight = 2;
        int barX = 2;
        int barY = size.height - barHeight - 2;
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Progress fill
        Color progressColor = getProgressColor(progress);
        g2d.setColor(progressColor);
        int fillWidth = (int) (barWidth * progress);
        g2d.fillRect(barX, barY, fillWidth, barHeight);
    }
    
    /**
     * Render session status indicator (small dot)
     */
    private void renderSessionStatusIndicator(Graphics2D g2d, Dimension size) {
        if (timeTrackingService == null) return;
        
        WorkSessionStatus status = getCurrentWorkStatus();
        Color statusColor = getStatusColor(status);
        
        // Small indicator in top-right corner
        int dotSize = Math.max(3, size.width / 8);
        int dotX = size.width - dotSize - 2;
        int dotY = 2;
        
        // Add pulsing effect for active status
        if (status == WorkSessionStatus.ACTIVE && isBlinking) {
            int alpha = 128 + (int) (127 * Math.sin(animationFrame * 0.3));
            statusColor = new Color(statusColor.getRed(), statusColor.getGreen(), 
                                  statusColor.getBlue(), alpha);
        }
        
        g2d.setColor(statusColor);
        g2d.fillOval(dotX, dotY, dotSize, dotSize);
    }
    
    /**
     * Start the update scheduler
     */
    private void startUpdateScheduler() {
        // Update icon every second for time display
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateTrayIcon();
            } catch (Exception e) {
                log.error("Error updating tray icon", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
        
        // Animation timer for effects
        scheduler.scheduleAtFixedRate(() -> {
            animationFrame++;
            if (animationFrame % 2 == 0) {
                isBlinking = !isBlinking;
                if (displayMode == TrayDisplayMode.STATUS_INDICATOR) {
                    updateTrayIcon();
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Update tray icon with current information
     */
    private void updateTrayIcon() {
        if (trayIcon == null) return;
        
        try {
            Dimension traySize = systemTray.getTrayIconSize();
            BufferedImage newImage = generateTrayImage(traySize);
            trayIcon.setImage(newImage);
            trayIcon.setToolTip(generateTooltipText());
        } catch (Exception e) {
            log.debug("Failed to update tray icon", e);
        }
    }
    
    /**
     * Generate detailed tooltip text
     */
    private String generateTooltipText() {
        StringBuilder tooltip = new StringBuilder("PCM Desktop");
        
        if (timeTrackingService != null) {
            WorkSession currentSession = timeTrackingService.getCurrentSession();
            if (currentSession != null) {
                LocalTime checkInTime = currentSession.getStartTime().toLocalTime();
                Duration elapsed = currentSession.getElapsedTime();
                
                tooltip.append("\n");
                tooltip.append("Check-in: ").append(checkInTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                tooltip.append("\nElapsed: ").append(formatDurationLong(elapsed));
                
                WorkSessionStatus status = getCurrentWorkStatus();
                tooltip.append("\nStatus: ").append(getStatusDisplayName(status));
                
                // Add break information if available
                Duration breakTime = currentSession.getTotalBreakTime();
                if (breakTime.toMinutes() > 0) {
                    tooltip.append("\nBreak time: ").append(formatDurationLong(breakTime));
                }
                
                // Add productivity info
                double productivity = calculateProductivityScore(currentSession);
                tooltip.append("\nProductivity: ").append(String.format("%.0f%%", productivity * 100));
            } else {
                tooltip.append("\nNot checked in");
            }
        }
        
        tooltip.append("\n\nDouble-click to open");
        
        return tooltip.toString();
    }
    
    // === Utility Methods ===
    
    private void initializeGraphicsResources() {
        try {
            // Load fonts
            primaryFont = new Font("SansSerif", Font.BOLD, 9);
            secondaryFont = new Font("SansSerif", Font.PLAIN, 7);
            
            // Default colors (will be updated based on theme)
            primaryColor = Color.BLACK;
            backgroundColor = new Color(255, 255, 255, 200);
            accentColor = new Color(0, 120, 215); // Windows blue
            
        } catch (Exception e) {
            log.error("Failed to initialize graphics resources", e);
            // Fallback to system defaults
            primaryFont = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
            secondaryFont = primaryFont;
            primaryColor = Color.BLACK;
            backgroundColor = null;
            accentColor = Color.BLUE;
        }
    }
    
    private void updateColorsForCurrentTheme() {
        switch (theme) {
            case DARK:
                primaryColor = Color.WHITE;
                backgroundColor = new Color(0, 0, 0, 180);
                accentColor = new Color(100, 200, 255);
                break;
            case LIGHT:
                primaryColor = Color.BLACK;
                backgroundColor = new Color(255, 255, 255, 200);
                accentColor = new Color(0, 120, 215);
                break;
            case AUTO:
                if (isSystemDarkTheme()) {
                    updateColorsForCurrentTheme(); // Recursive call with detected theme
                    return;
                }
                // Default to light theme
                primaryColor = Color.BLACK;
                backgroundColor = new Color(255, 255, 255, 200);
                accentColor = new Color(0, 120, 215);
                break;
        }
    }
    
    private boolean isSystemDarkTheme() {
        // Platform-specific dark theme detection
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return WindowsThemeDetector.isDarkTheme();
        } else if (os.contains("mac")) {
            return MacOSThemeDetector.isDarkTheme();
        } else if (os.contains("linux")) {
            return LinuxThemeDetector.isDarkTheme();
        }
        
        return false; // Default to light theme
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours > 0) {
            return String.format("%dh%02dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    private String formatDurationCompact(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d", hours, minutes);
        } else {
            return String.format("0:%02d", minutes);
        }
    }
    
    private String formatDurationLong(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }
    
    private WorkSessionStatus getCurrentWorkStatus() {
        if (timeTrackingService == null) return WorkSessionStatus.INACTIVE;
        
        WorkSession currentSession = timeTrackingService.getCurrentSession();
        if (currentSession == null) return WorkSessionStatus.INACTIVE;
        
        return currentSession.getCurrentStatus();
    }
    
    private Color getStatusColor(WorkSessionStatus status) {
        switch (status) {
            case ACTIVE:
                return new Color(34, 197, 94); // Green
            case BREAK:
                return new Color(251, 191, 36); // Yellow
            case AWAY:
                return new Color(239, 68, 68); // Red
            case INACTIVE:
            default:
                return new Color(156, 163, 175); // Gray
        }
    }
    
    private Color getProgressColor(float progress) {
        if (progress < 0.5f) {
            return new Color(239, 68, 68); // Red
        } else if (progress < 0.8f) {
            return new Color(251, 191, 36); // Yellow
        } else {
            return new Color(34, 197, 94); // Green
        }
    }
    
    private String getStatusDisplayName(WorkSessionStatus status) {
        switch (status) {
            case ACTIVE: return "Working";
            case BREAK: return "On Break";
            case AWAY: return "Away";
            case INACTIVE: return "Inactive";
            default: return "Unknown";
        }
    }
    
    private double calculateProductivityScore(WorkSession session) {
        // Simplified productivity calculation
        Duration totalTime = session.getElapsedTime();
        Duration activeTime = session.getActiveTime();
        
        if (totalTime.toMinutes() == 0) return 0.0;
        
        return (double) activeTime.toMinutes() / totalTime.toMinutes();
    }
    
    private void drawCenteredText(Graphics2D g2d, String text, int width, int y, FontMetrics fm) {
        Rectangle2D bounds = fm.getStringBounds(text, g2d);
        int x = (width - (int) bounds.getWidth()) / 2;
        g2d.drawString(text, x, y);
    }
    
    private void renderFallbackIcon(Graphics2D g2d, Dimension size) {
        // Render default PCM icon when no time tracking data
        g2d.setColor(primaryColor);
        g2d.setFont(primaryFont);
        FontMetrics fm = g2d.getFontMetrics();
        drawCenteredText(g2d, "PCM", size.width, (size.height + fm.getAscent()) / 2, fm);
    }
    
    private void renderNoSessionIcon(Graphics2D g2d, Dimension size) {
        // Render "not checked in" indicator
        g2d.setColor(new Color(156, 163, 175)); // Gray
        g2d.setFont(secondaryFont);
        FontMetrics fm = g2d.getFontMetrics();
        drawCenteredText(g2d, "OFF", size.width, (size.height + fm.getAscent()) / 2, fm);
    }
    
    private void renderSmallClockIcon(Graphics2D g2d, Dimension size) {
        // Small clock icon in corner
        int iconSize = Math.max(4, size.width / 6);
        int iconX = size.width - iconSize - 1;
        int iconY = 1;
        
        g2d.setColor(accentColor);
        g2d.drawOval(iconX, iconY, iconSize, iconSize);
        
        // Clock hands
        int centerX = iconX + iconSize / 2;
        int centerY = iconY + iconSize / 2;
        g2d.drawLine(centerX, centerY, centerX, iconY + 2);
        g2d.drawLine(centerX, centerY, iconX + iconSize - 2, centerY);
    }
    
    private void renderStatusSymbol(Graphics2D g2d, WorkSessionStatus status, int x, int y, int size) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        
        switch (status) {
            case ACTIVE:
                // Checkmark
                g2d.drawLine(x + size/4, y + size/2, x + size/2, y + 3*size/4);
                g2d.drawLine(x + size/2, y + 3*size/4, x + 3*size/4, y + size/4);
                break;
            case BREAK:
                // Pause symbol
                int barWidth = size / 6;
                g2d.fillRect(x + size/3 - barWidth/2, y + size/4, barWidth, size/2);
                g2d.fillRect(x + 2*size/3 - barWidth/2, y + size/4, barWidth, size/2);
                break;
            case AWAY:
                // Cross
                g2d.drawLine(x + size/4, y + size/4, x + 3*size/4, y + 3*size/4);
                g2d.drawLine(x + 3*size/4, y + size/4, x + size/4, y + 3*size/4);
                break;
            case INACTIVE:
                // Question mark
                g2d.drawString("?", x + size/3, y + 2*size/3);
                break;
        }
    }
    
    // === Event Handlers ===
    
    private void handleTrayClick() {
        // Single click action - could toggle between display modes
        cycleDisplayMode();
    }
    
    private void handleTrayDoubleClick() {
        // Double click - restore main window
        Platform.runLater(() -> {
            SystemTrayManager.getInstance().restoreWindow();
        });
    }
    
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        
        // Display mode submenu
        Menu displayModeMenu = new Menu("Display Mode");
        for (TrayDisplayMode mode : TrayDisplayMode.values()) {
            MenuItem modeItem = new MenuItem(mode.getDisplayName());
            modeItem.addActionListener(e -> {
                displayMode = mode;
                updateTrayIcon();
            });
            displayModeMenu.add(modeItem);
        }
        popup.add(displayModeMenu);
        
        popup.addSeparator();
        
        // Quick actions
        MenuItem checkInOut = new MenuItem();
        if (getCurrentWorkStatus() == WorkSessionStatus.INACTIVE) {
            checkInOut.setLabel("Check In");
            checkInOut.addActionListener(e -> timeTrackingService.startSession());
        } else {
            checkInOut.setLabel("Check Out");
            checkInOut.addActionListener(e -> timeTrackingService.endSession());
        }
        popup.add(checkInOut);
        
        MenuItem takeBreak = new MenuItem("Take Break");
        takeBreak.addActionListener(e -> timeTrackingService.startBreak());
        popup.add(takeBreak);
        
        popup.addSeparator();
        
        // Standard menu items
        MenuItem showApp = new MenuItem("Show Application");
        showApp.addActionListener(e -> Platform.runLater(() -> 
            SystemTrayManager.getInstance().restoreWindow()));
        popup.add(showApp);
        
        MenuItem settings = new MenuItem("Settings");
        settings.addActionListener(e -> Platform.runLater(() -> 
            openSettings()));
        popup.add(settings);
        
        popup.addSeparator();
        
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> Platform.runLater(() -> 
            exitApplication()));
        popup.add(exit);
        
        return popup;
    }
    
    private void cycleDisplayMode() {
        TrayDisplayMode[] modes = TrayDisplayMode.values();
        int currentIndex = displayMode.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        displayMode = modes[nextIndex];
        updateTrayIcon();
        
        // Show brief notification about new mode
        if (trayIcon != null) {
            trayIcon.displayMessage("Display Mode", 
                "Switched to: " + displayMode.getDisplayName(), 
                TrayIcon.MessageType.INFO);
        }
    }
    
    // === Configuration ===
    
    public void setDisplayMode(TrayDisplayMode mode) {
        this.displayMode = mode;
        updateTrayIcon();
    }
    
    public void setTheme(TrayDisplayTheme theme) {
        this.theme = theme;
        updateTrayIcon();
    }
    
    public void setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
        updateTrayIcon();
    }
    
    public void setShowProgressBar(boolean showProgressBar) {
        this.showProgressBar = showProgressBar;
        updateTrayIcon();
    }
    
    // === Cleanup ===
    
    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            trayIcon = null;
        }
        
        log.info("Dynamic tray icon manager cleaned up");
    }
    
    // === Enums ===
    
    public enum TrayDisplayMode {
        CURRENT_TIME("Current Time"),
        SESSION_DURATION("Session Duration"), 
        TIME_AND_DURATION("Time & Duration"),
        STATUS_INDICATOR("Status Indicator"),
        COMPACT_INFO("Compact Info");
        
        private final String displayName;
        
        TrayDisplayMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum TrayDisplayTheme {
        LIGHT, DARK, AUTO
    }
    
    public enum WorkSessionStatus {
        ACTIVE, BREAK, AWAY, INACTIVE
    }
    
    // === Platform-specific theme detection placeholder classes ===
    
    private static class WindowsThemeDetector {
        static boolean isDarkTheme() {
            // Implementation for Windows theme detection
            return false;
        }
    }
    
    private static class MacOSThemeDetector {
        static boolean isDarkTheme() {
            // Implementation for macOS theme detection
            return false;
        }
    }
    
    private static class LinuxThemeDetector {
        static boolean isDarkTheme() {
            // Implementation for Linux theme detection
            return false;
        }
    }
    
    // Placeholder methods
    private void openSettings() {
        log.info("Opening settings from tray menu");
    }
    
    private void exitApplication() {
        cleanup();
        Platform.exit();
        System.exit(0);
    }
}
```

## Time Tracking Integration

### 1. Time Tracking Service Interface

```java
package com.noteflix.pcm.time;

import lombok.Data;
import lombok.Builder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TimeTrackingService {
    
    /**
     * Start a new work session
     */
    WorkSession startSession();
    
    /**
     * End the current work session
     */
    void endSession();
    
    /**
     * Start a break in the current session
     */
    void startBreak();
    
    /**
     * End the current break
     */
    void endBreak();
    
    /**
     * Get the current active session
     */
    WorkSession getCurrentSession();
    
    /**
     * Check if user is currently checked in
     */
    boolean isCheckedIn();
    
    /**
     * Get today's work sessions
     */
    List<WorkSession> getTodaySessions();
    
    /**
     * Get total work time for today
     */
    Duration getTodayWorkTime();
    
    /**
     * Get total break time for today
     */
    Duration getTodayBreakTime();
    
    /**
     * Add session event listener
     */
    void addSessionEventListener(SessionEventListener listener);
    
    /**
     * Remove session event listener
     */
    void removeSessionEventListener(SessionEventListener listener);
}

@Data
@Builder
public class WorkSession {
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration totalBreakTime;
    private List<BreakPeriod> breaks;
    private WorkSessionStatus currentStatus;
    private String userId;
    private String location;
    private String device;
    
    public Duration getElapsedTime() {
        LocalDateTime endTime = this.endTime != null ? this.endTime : LocalDateTime.now();
        return Duration.between(startTime, endTime);
    }
    
    public Duration getActiveTime() {
        return getElapsedTime().minus(totalBreakTime);
    }
    
    public boolean isActive() {
        return endTime == null;
    }
    
    public double getProductivityScore() {
        Duration elapsed = getElapsedTime();
        Duration active = getActiveTime();
        
        if (elapsed.toMinutes() == 0) return 0.0;
        return (double) active.toMinutes() / elapsed.toMinutes();
    }
}

@Data
@Builder
public class BreakPeriod {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason; // lunch, coffee, meeting, etc.
    
    public Duration getDuration() {
        LocalDateTime endTime = this.endTime != null ? this.endTime : LocalDateTime.now();
        return Duration.between(startTime, endTime);
    }
    
    public boolean isActive() {
        return endTime == null;
    }
}

public interface SessionEventListener {
    void onSessionStarted(WorkSession session);
    void onSessionEnded(WorkSession session);
    void onBreakStarted(WorkSession session, BreakPeriod breakPeriod);
    void onBreakEnded(WorkSession session, BreakPeriod breakPeriod);
    void onStatusChanged(WorkSession session, WorkSessionStatus oldStatus, WorkSessionStatus newStatus);
}
```

### 2. Time Tracking Service Implementation

```java
package com.noteflix.pcm.time.impl;

import com.noteflix.pcm.time.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class TimeTrackingServiceImpl implements TimeTrackingService {
    
    private WorkSession currentSession;
    private final List<WorkSession> todaySessions = new ArrayList<>();
    private final List<SessionEventListener> listeners = new CopyOnWriteArrayList<>();
    
    @Override
    public WorkSession startSession() {
        // End previous session if exists
        if (currentSession != null && currentSession.isActive()) {
            endSession();
        }
        
        // Create new session
        currentSession = WorkSession.builder()
            .id(UUID.randomUUID().toString())
            .startTime(LocalDateTime.now())
            .totalBreakTime(Duration.ZERO)
            .breaks(new ArrayList<>())
            .currentStatus(WorkSessionStatus.ACTIVE)
            .userId(getCurrentUserId())
            .location(getCurrentLocation())
            .device(getDeviceInfo())
            .build();
            
        todaySessions.add(currentSession);
        
        // Notify listeners
        notifySessionStarted(currentSession);
        
        log.info("Work session started: {}", currentSession.getId());
        return currentSession;
    }
    
    @Override
    public void endSession() {
        if (currentSession == null || !currentSession.isActive()) {
            log.warn("No active session to end");
            return;
        }
        
        // End any active break
        if (currentSession.getCurrentStatus() == WorkSessionStatus.BREAK) {
            endBreak();
        }
        
        // End session
        currentSession.setEndTime(LocalDateTime.now());
        currentSession.setCurrentStatus(WorkSessionStatus.INACTIVE);
        
        // Notify listeners
        notifySessionEnded(currentSession);
        
        log.info("Work session ended: {} (Duration: {})", 
            currentSession.getId(), currentSession.getElapsedTime());
        
        // Save session data
        persistSession(currentSession);
        
        currentSession = null;
    }
    
    @Override
    public void startBreak() {
        if (currentSession == null || !currentSession.isActive()) {
            log.warn("No active session for break");
            return;
        }
        
        // Don't start break if already on break
        if (currentSession.getCurrentStatus() == WorkSessionStatus.BREAK) {
            log.debug("Already on break");
            return;
        }
        
        // Create break period
        BreakPeriod breakPeriod = BreakPeriod.builder()
            .startTime(LocalDateTime.now())
            .reason("break") // Can be made configurable
            .build();
            
        currentSession.getBreaks().add(breakPeriod);
        
        WorkSessionStatus oldStatus = currentSession.getCurrentStatus();
        currentSession.setCurrentStatus(WorkSessionStatus.BREAK);
        
        // Notify listeners
        notifyBreakStarted(currentSession, breakPeriod);
        notifyStatusChanged(currentSession, oldStatus, WorkSessionStatus.BREAK);
        
        log.info("Break started for session: {}", currentSession.getId());
    }
    
    @Override
    public void endBreak() {
        if (currentSession == null || currentSession.getCurrentStatus() != WorkSessionStatus.BREAK) {
            log.warn("No active break to end");
            return;
        }
        
        // Find and end the active break
        List<BreakPeriod> breaks = currentSession.getBreaks();
        if (!breaks.isEmpty()) {
            BreakPeriod activeBreak = breaks.get(breaks.size() - 1);
            if (activeBreak.isActive()) {
                activeBreak.setEndTime(LocalDateTime.now());
                
                // Update total break time
                Duration breakDuration = activeBreak.getDuration();
                currentSession.setTotalBreakTime(
                    currentSession.getTotalBreakTime().plus(breakDuration)
                );
                
                // Change status back to active
                WorkSessionStatus oldStatus = currentSession.getCurrentStatus();
                currentSession.setCurrentStatus(WorkSessionStatus.ACTIVE);
                
                // Notify listeners
                notifyBreakEnded(currentSession, activeBreak);
                notifyStatusChanged(currentSession, oldStatus, WorkSessionStatus.ACTIVE);
                
                log.info("Break ended for session: {} (Break duration: {})", 
                    currentSession.getId(), breakDuration);
            }
        }
    }
    
    @Override
    public WorkSession getCurrentSession() {
        return currentSession;
    }
    
    @Override
    public boolean isCheckedIn() {
        return currentSession != null && currentSession.isActive();
    }
    
    @Override
    public List<WorkSession> getTodaySessions() {
        return new ArrayList<>(todaySessions);
    }
    
    @Override
    public Duration getTodayWorkTime() {
        return todaySessions.stream()
            .map(WorkSession::getActiveTime)
            .reduce(Duration.ZERO, Duration::plus);
    }
    
    @Override
    public Duration getTodayBreakTime() {
        return todaySessions.stream()
            .map(WorkSession::getTotalBreakTime)
            .reduce(Duration.ZERO, Duration::plus);
    }
    
    @Override
    public void addSessionEventListener(SessionEventListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeSessionEventListener(SessionEventListener listener) {
        listeners.remove(listener);
    }
    
    // === Notification methods ===
    
    private void notifySessionStarted(WorkSession session) {
        listeners.forEach(listener -> {
            try {
                listener.onSessionStarted(session);
            } catch (Exception e) {
                log.error("Error notifying session started", e);
            }
        });
    }
    
    private void notifySessionEnded(WorkSession session) {
        listeners.forEach(listener -> {
            try {
                listener.onSessionEnded(session);
            } catch (Exception e) {
                log.error("Error notifying session ended", e);
            }
        });
    }
    
    private void notifyBreakStarted(WorkSession session, BreakPeriod breakPeriod) {
        listeners.forEach(listener -> {
            try {
                listener.onBreakStarted(session, breakPeriod);
            } catch (Exception e) {
                log.error("Error notifying break started", e);
            }
        });
    }
    
    private void notifyBreakEnded(WorkSession session, BreakPeriod breakPeriod) {
        listeners.forEach(listener -> {
            try {
                listener.onBreakEnded(session, breakPeriod);
            } catch (Exception e) {
                log.error("Error notifying break ended", e);
            }
        });
    }
    
    private void notifyStatusChanged(WorkSession session, WorkSessionStatus oldStatus, WorkSessionStatus newStatus) {
        listeners.forEach(listener -> {
            try {
                listener.onStatusChanged(session, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("Error notifying status changed", e);
            }
        });
    }
    
    // === Helper methods ===
    
    private String getCurrentUserId() {
        // Get from security context or configuration
        return "current-user";
    }
    
    private String getCurrentLocation() {
        // Could determine from IP, GPS, or manual configuration
        return "office"; // or "home", "remote", etc.
    }
    
    private String getDeviceInfo() {
        return System.getProperty("os.name") + " - " + 
               System.getProperty("user.name") + "@" + 
               getHostname();
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private void persistSession(WorkSession session) {
        // Save to database or file
        // Implementation depends on your persistence strategy
        log.debug("Persisting session: {}", session.getId());
    }
}
```

### 3. Integration with Tray Manager

```java
package com.noteflix.pcm.tray;

import com.noteflix.pcm.time.*;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrayTimeTrackingIntegration implements SessionEventListener {
    
    @Autowired
    private TimeTrackingService timeTrackingService;
    
    @Autowired
    private DynamicTrayIconManager trayIconManager;
    
    @PostConstruct
    public void initialize() {
        // Register as session event listener
        timeTrackingService.addSessionEventListener(this);
        
        // Initialize tray with time tracking service
        trayIconManager.initialize(timeTrackingService);
    }
    
    @Override
    public void onSessionStarted(WorkSession session) {
        Platform.runLater(() -> {
            // Update tray icon
            trayIconManager.updateTrayIcon();
            
            // Show check-in notification
            trayIconManager.showTrayNotification(
                "Checked In",
                "Work session started at " + session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                TrayIcon.MessageType.INFO
            );
        });
        
        log.info("Session started notification sent to tray");
    }
    
    @Override
    public void onSessionEnded(WorkSession session) {
        Platform.runLater(() -> {
            // Update tray icon
            trayIconManager.updateTrayIcon();
            
            // Show check-out notification with session summary
            Duration sessionDuration = session.getElapsedTime();
            Duration activeTime = session.getActiveTime();
            
            String message = String.format(
                "Session ended. Total: %s, Active: %s",
                formatDuration(sessionDuration),
                formatDuration(activeTime)
            );
            
            trayIconManager.showTrayNotification(
                "Checked Out",
                message,
                TrayIcon.MessageType.INFO
            );
        });
        
        log.info("Session ended notification sent to tray");
    }
    
    @Override
    public void onBreakStarted(WorkSession session, BreakPeriod breakPeriod) {
        Platform.runLater(() -> {
            // Update tray icon to show break status
            trayIconManager.updateTrayIcon();
            
            // Show break notification
            trayIconManager.showTrayNotification(
                "Break Started",
                "Taking a break. Current session: " + 
                formatDuration(session.getElapsedTime()),
                TrayIcon.MessageType.INFO
            );
        });
    }
    
    @Override
    public void onBreakEnded(WorkSession session, BreakPeriod breakPeriod) {
        Platform.runLater(() -> {
            // Update tray icon back to active status
            trayIconManager.updateTrayIcon();
            
            // Show back to work notification
            trayIconManager.showTrayNotification(
                "Break Ended",
                "Back to work! Break duration: " + 
                formatDuration(breakPeriod.getDuration()),
                TrayIcon.MessageType.INFO
            );
        });
    }
    
    @Override
    public void onStatusChanged(WorkSession session, WorkSessionStatus oldStatus, WorkSessionStatus newStatus) {
        Platform.runLater(() -> {
            // Update tray icon for status change
            trayIconManager.updateTrayIcon();
        });
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
```

## Custom Graphics Rendering

### 1. Advanced Text Rendering

```java
package com.noteflix.pcm.tray.graphics;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

public class AdvancedTextRenderer {
    
    /**
     * Render text with outline for better visibility
     */
    public static void renderTextWithOutline(Graphics2D g2d, String text, Font font, 
                                           Color textColor, Color outlineColor, 
                                           int x, int y, float outlineWidth) {
        
        // Create outlined font
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Shape textShape = gv.getOutline(x, y);
        
        // Render outline
        g2d.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(outlineColor);
        g2d.draw(textShape);
        
        // Render text fill
        g2d.setColor(textColor);
        g2d.fill(textShape);
    }
    
    /**
     * Render text with gradient
     */
    public static void renderGradientText(Graphics2D g2d, String text, Font font,
                                        Color startColor, Color endColor,
                                        int x, int y, int width) {
        
        // Create gradient paint
        GradientPaint gradient = new GradientPaint(
            x, y, startColor,
            x + width, y, endColor
        );
        
        // Set font and paint
        g2d.setFont(font);
        g2d.setPaint(gradient);
        
        // Render text
        g2d.drawString(text, x, y);
    }
    
    /**
     * Render text with shadow
     */
    public static void renderTextWithShadow(Graphics2D g2d, String text, Font font,
                                          Color textColor, Color shadowColor,
                                          int x, int y, int shadowOffset) {
        
        g2d.setFont(font);
        
        // Render shadow
        g2d.setColor(shadowColor);
        g2d.drawString(text, x + shadowOffset, y + shadowOffset);
        
        // Render text
        g2d.setColor(textColor);
        g2d.drawString(text, x, y);
    }
    
    /**
     * Render multiline text with proper alignment
     */
    public static void renderMultilineText(Graphics2D g2d, String[] lines, Font font,
                                         Color textColor, int x, int y, int lineHeight,
                                         TextAlignment alignment, int maxWidth) {
        
        g2d.setFont(font);
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        
        int currentY = y + fm.getAscent();
        
        for (String line : lines) {
            int textX = calculateAlignmentX(line, fm, x, maxWidth, alignment);
            g2d.drawString(line, textX, currentY);
            currentY += lineHeight;
        }
    }
    
    /**
     * Auto-fit text to available space
     */
    public static Font getAutoFitFont(Graphics2D g2d, String text, Font baseFont, 
                                    int maxWidth, int maxHeight) {
        
        Font currentFont = baseFont;
        FontMetrics fm = g2d.getFontMetrics(currentFont);
        
        // Reduce font size until text fits
        while ((fm.stringWidth(text) > maxWidth || fm.getHeight() > maxHeight) && 
               currentFont.getSize() > 6) {
            
            currentFont = currentFont.deriveFont((float) (currentFont.getSize() - 1));
            fm = g2d.getFontMetrics(currentFont);
        }
        
        return currentFont;
    }
    
    private static int calculateAlignmentX(String text, FontMetrics fm, int x, 
                                         int maxWidth, TextAlignment alignment) {
        int textWidth = fm.stringWidth(text);
        
        switch (alignment) {
            case CENTER:
                return x + (maxWidth - textWidth) / 2;
            case RIGHT:
                return x + maxWidth - textWidth;
            case LEFT:
            default:
                return x;
        }
    }
    
    public enum TextAlignment {
        LEFT, CENTER, RIGHT
    }
}
```

### 2. Icon and Shape Rendering

```java
package com.noteflix.pcm.tray.graphics;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class IconRenderer {
    
    /**
     * Render clock icon
     */
    public static void renderClockIcon(Graphics2D g2d, int x, int y, int size, 
                                     Color clockColor, Color handColor) {
        
        // Clock face
        g2d.setColor(clockColor);
        g2d.fillOval(x, y, size, size);
        
        // Clock border
        g2d.setColor(handColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, size, size);
        
        // Clock hands
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int radius = size / 2 - 2;
        
        // Hour hand (shorter, thicker)
        int hourAngle = (java.time.LocalTime.now().getHour() % 12) * 30 - 90; // -90 to start from 12 o'clock
        int hourX = centerX + (int) (radius * 0.5 * Math.cos(Math.toRadians(hourAngle)));
        int hourY = centerY + (int) (radius * 0.5 * Math.sin(Math.toRadians(hourAngle)));
        
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY, hourX, hourY);
        
        // Minute hand (longer, thinner)
        int minuteAngle = java.time.LocalTime.now().getMinute() * 6 - 90;
        int minuteX = centerX + (int) (radius * 0.8 * Math.cos(Math.toRadians(minuteAngle)));
        int minuteY = centerY + (int) (radius * 0.8 * Math.sin(Math.toRadians(minuteAngle)));
        
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY, minuteX, minuteY);
        
        // Center dot
        g2d.fillOval(centerX - 2, centerY - 2, 4, 4);
    }
    
    /**
     * Render status indicator icon
     */
    public static void renderStatusIcon(Graphics2D g2d, WorkSessionStatus status,
                                      int x, int y, int size) {
        
        Color statusColor = getStatusColor(status);
        
        switch (status) {
            case ACTIVE:
                renderPlayIcon(g2d, x, y, size, statusColor);
                break;
            case BREAK:
                renderPauseIcon(g2d, x, y, size, statusColor);
                break;
            case AWAY:
                renderAwayIcon(g2d, x, y, size, statusColor);
                break;
            case INACTIVE:
                renderStopIcon(g2d, x, y, size, statusColor);
                break;
        }
    }
    
    private static void renderPlayIcon(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(color);
        
        // Triangle (play button)
        int[] xPoints = {x + size/4, x + 3*size/4, x + size/4};
        int[] yPoints = {y + size/4, y + size/2, y + 3*size/4};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private static void renderPauseIcon(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(color);
        
        // Two vertical bars
        int barWidth = size / 6;
        int barHeight = size / 2;
        int barY = y + size / 4;
        
        g2d.fillRect(x + size/3 - barWidth/2, barY, barWidth, barHeight);
        g2d.fillRect(x + 2*size/3 - barWidth/2, barY, barWidth, barHeight);
    }
    
    private static void renderAwayIcon(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Clock with X
        g2d.drawOval(x + 2, y + 2, size - 4, size - 4);
        
        // X mark
        g2d.drawLine(x + size/3, y + size/3, x + 2*size/3, y + 2*size/3);
        g2d.drawLine(x + 2*size/3, y + size/3, x + size/3, y + 2*size/3);
    }
    
    private static void renderStopIcon(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(color);
        
        // Square (stop button)
        int squareSize = size / 2;
        int squareX = x + size / 4;
        int squareY = y + size / 4;
        
        g2d.fillRect(squareX, squareY, squareSize, squareSize);
    }
    
    /**
     * Render progress arc
     */
    public static void renderProgressArc(Graphics2D g2d, int x, int y, int size,
                                       float progress, Color backgroundColor,
                                       Color progressColor, float strokeWidth) {
        
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Background arc
        g2d.setColor(backgroundColor);
        g2d.drawArc(x, y, size, size, 0, 360);
        
        // Progress arc (starting from 12 o'clock)
        g2d.setColor(progressColor);
        int arcAngle = (int) (360 * progress);
        g2d.drawArc(x, y, size, size, 90, -arcAngle);
    }
    
    /**
     * Render notification badge
     */
    public static void renderNotificationBadge(Graphics2D g2d, int x, int y, 
                                             String text, Color backgroundColor,
                                             Color textColor, Font font) {
        
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int badgeWidth = textWidth + 8;
        int badgeHeight = textHeight + 4;
        
        // Badge background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(x, y, badgeWidth, badgeHeight, badgeHeight/2, badgeHeight/2);
        
        // Badge text
        g2d.setColor(textColor);
        int textX = x + (badgeWidth - textWidth) / 2;
        int textY = y + (badgeHeight + fm.getAscent()) / 2;
        g2d.drawString(text, textX, textY);
    }
    
    private static Color getStatusColor(WorkSessionStatus status) {
        switch (status) {
            case ACTIVE: return new Color(34, 197, 94); // Green
            case BREAK: return new Color(251, 191, 36); // Yellow
            case AWAY: return new Color(239, 68, 68); // Red
            case INACTIVE: return new Color(156, 163, 175); // Gray
            default: return Color.GRAY;
        }
    }
}
```

## Platform-Specific Optimizations

### 1. Windows Optimizations

```java
package com.noteflix.pcm.tray.platform;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class WindowsTrayOptimization {
    
    /**
     * Detect Windows theme and return appropriate colors
     */
    public static TrayColorScheme getWindowsColorScheme() {
        try {
            // Check if dark mode is enabled
            boolean isDarkTheme = isWindowsDarkTheme();
            
            if (isDarkTheme) {
                return TrayColorScheme.builder()
                    .primaryColor(Color.WHITE)
                    .backgroundColor(new Color(32, 32, 32, 180))
                    .accentColor(new Color(0, 120, 215))
                    .build();
            } else {
                return TrayColorScheme.builder()
                    .primaryColor(Color.BLACK)
                    .backgroundColor(new Color(240, 240, 240, 180))
                    .accentColor(new Color(0, 120, 215))
                    .build();
            }
        } catch (Exception e) {
            log.warn("Failed to detect Windows theme, using default", e);
            return getDefaultColorScheme();
        }
    }
    
    private static boolean isWindowsDarkTheme() {
        try {
            String keyPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
            int value = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER, keyPath, "AppsUseLightTheme"
            );
            return value == 0; // 0 = dark theme, 1 = light theme
        } catch (Exception e) {
            return false; // Default to light theme
        }
    }
    
    /**
     * Optimize icon for Windows high-DPI displays
     */
    public static BufferedImage optimizeForWindowsHighDPI(BufferedImage originalImage, Dimension traySize) {
        // Windows may request different sizes for different DPI settings
        int targetSize = Math.max(traySize.width, traySize.height);
        
        // Create high-quality scaled image
        BufferedImage optimizedImage = new BufferedImage(
            targetSize, targetSize, BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = optimizedImage.createGraphics();
        
        // High-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        
        // Draw scaled image
        g2d.drawImage(originalImage, 0, 0, targetSize, targetSize, null);
        g2d.dispose();
        
        return optimizedImage;
    }
    
    /**
     * Get Windows notification area preferences
     */
    public static WindowsTrayPreferences getWindowsTrayPreferences() {
        // This could read from Windows registry to determine user preferences
        return WindowsTrayPreferences.builder()
            .showIconAndNotifications(true)
            .hideWhenInactive(false)
            .notificationDuration(5) // seconds
            .build();
    }
    
    @lombok.Builder
    @lombok.Data
    public static class WindowsTrayPreferences {
        private boolean showIconAndNotifications;
        private boolean hideWhenInactive;
        private int notificationDuration;
    }
    
    private static TrayColorScheme getDefaultColorScheme() {
        return TrayColorScheme.builder()
            .primaryColor(Color.BLACK)
            .backgroundColor(new Color(240, 240, 240, 180))
            .accentColor(new Color(0, 120, 215))
            .build();
    }
}
```

### 2. macOS Optimizations

```java
package com.noteflix.pcm.tray.platform;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
public class MacOSTrayOptimization {
    
    /**
     * Create macOS-style template image
     * Template images are black with transparency and automatically adapt to dark/light mode
     */
    public static BufferedImage createMacOSTemplateImage(int size, String text, 
                                                        boolean isCompact) {
        
        BufferedImage templateImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = templateImage.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Use black color for template (macOS will invert automatically)
        g2d.setColor(Color.BLACK);
        
        if (isCompact) {
            renderCompactMacOSTemplate(g2d, text, size);
        } else {
            renderStandardMacOSTemplate(g2d, text, size);
        }
        
        g2d.dispose();
        return templateImage;
    }
    
    private static void renderCompactMacOSTemplate(Graphics2D g2d, String text, int size) {
        // macOS prefers very compact icons in menu bar
        Font font = new Font("SF Pro Display", Font.BOLD, size - 4); // macOS system font
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (size - textWidth) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
    }
    
    private static void renderStandardMacOSTemplate(Graphics2D g2d, String text, int size) {
        // Standard template with more visual elements
        Font font = new Font("SF Pro Display", Font.MEDIUM, size - 6);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (size - textWidth) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
        
        // Add small indicator dot
        int dotSize = Math.max(2, size / 10);
        g2d.fillOval(size - dotSize - 1, 1, dotSize, dotSize);
    }
    
    /**
     * Detect macOS appearance (Dark/Light mode)
     */
    public static boolean isMacOSDarkMode() {
        try {
            // Use AppleScript to detect dark mode
            Process process = Runtime.getRuntime().exec(new String[]{
                "osascript", "-e", 
                "tell application \"System Events\" to tell appearance preferences to get dark mode"
            });
            
            process.waitFor();
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
                
                String result = reader.readLine();
                return "true".equals(result);
            }
        } catch (Exception e) {
            log.debug("Failed to detect macOS dark mode", e);
            return false;
        }
    }
    
    /**
     * Get macOS-appropriate color scheme
     */
    public static TrayColorScheme getMacOSColorScheme() {
        // For template images, colors are managed by macOS
        // These colors are used for fallback or non-template elements
        return TrayColorScheme.builder()
            .primaryColor(Color.BLACK) // Will be inverted by macOS if needed
            .backgroundColor(null) // Transparent background for template
            .accentColor(new Color(0, 122, 255)) // iOS/macOS blue
            .build();
    }
    
    /**
     * Handle Retina display scaling
     */
    public static BufferedImage createRetinaImage(BufferedImage standardImage) {
        int retinaSize = standardImage.getWidth() * 2;
        BufferedImage retinaImage = new BufferedImage(
            retinaSize, retinaSize, BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = retinaImage.createGraphics();
        
        // High-quality scaling for Retina
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(standardImage, 0, 0, retinaSize, retinaSize, null);
        g2d.dispose();
        
        return retinaImage;
    }
    
    /**
     * Configure for macOS menu bar behavior
     */
    public static void configureMacOSMenuBarBehavior() {
        // Set system properties for better macOS integration
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "PCM Desktop");
        System.setProperty("apple.awt.application.appearance", "system");
        
        // Hide dock icon when using menu bar only
        System.setProperty("apple.awt.UIElement", "false"); // Set to "true" to hide dock icon
    }
}
```

### 3. Linux Optimizations

```java
package com.noteflix.pcm.tray.platform;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class LinuxTrayOptimization {
    
    /**
     * Detect Linux desktop environment and optimize accordingly
     */
    public static LinuxDesktopEnvironment detectDesktopEnvironment() {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        String session = System.getenv("XDG_SESSION_DESKTOP");
        String gdmSession = System.getenv("GDMSESSION");
        
        if (desktop != null) {
            String desktopLower = desktop.toLowerCase();
            if (desktopLower.contains("gnome")) return LinuxDesktopEnvironment.GNOME;
            if (desktopLower.contains("kde")) return LinuxDesktopEnvironment.KDE;
            if (desktopLower.contains("xfce")) return LinuxDesktopEnvironment.XFCE;
            if (desktopLower.contains("unity")) return LinuxDesktopEnvironment.UNITY;
            if (desktopLower.contains("mate")) return LinuxDesktopEnvironment.MATE;
            if (desktopLower.contains("cinnamon")) return LinuxDesktopEnvironment.CINNAMON;
        }
        
        // Fallback detection methods
        if (session != null) {
            String sessionLower = session.toLowerCase();
            if (sessionLower.contains("gnome")) return LinuxDesktopEnvironment.GNOME;
            if (sessionLower.contains("kde")) return LinuxDesktopEnvironment.KDE;
        }
        
        return LinuxDesktopEnvironment.UNKNOWN;
    }
    
    /**
     * Get optimal tray icon size for detected desktop environment
     */
    public static Dimension getOptimalTraySize(LinuxDesktopEnvironment de, Dimension systemTraySize) {
        switch (de) {
            case GNOME:
                // GNOME typically uses 22x22 or 24x24
                return new Dimension(22, 22);
            case KDE:
                // KDE can handle various sizes, use system preference
                return systemTraySize;
            case XFCE:
                // XFCE typically uses 24x24
                return new Dimension(24, 24);
            case UNITY:
                // Unity uses 22x22
                return new Dimension(22, 22);
            default:
                // Use system tray size as fallback
                return systemTraySize;
        }
    }
    
    /**
     * Create optimized icon for Linux desktop environment
     */
    public static BufferedImage createLinuxOptimizedIcon(String text, int size, 
                                                        LinuxDesktopEnvironment de,
                                                        boolean isDarkTheme) {
        
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Configure colors based on theme and DE
        TrayColorScheme colors = getLinuxColorScheme(de, isDarkTheme);
        
        // Render based on desktop environment preferences
        switch (de) {
            case GNOME:
                renderGnomeStyleIcon(g2d, text, size, colors);
                break;
            case KDE:
                renderKDEStyleIcon(g2d, text, size, colors);
                break;
            case XFCE:
                renderXfceStyleIcon(g2d, text, size, colors);
                break;
            default:
                renderGenericLinuxIcon(g2d, text, size, colors);
        }
        
        g2d.dispose();
        return image;
    }
    
    private static void renderGnomeStyleIcon(Graphics2D g2d, String text, int size, 
                                           TrayColorScheme colors) {
        // GNOME prefers clean, minimalist icons
        Font font = new Font("Ubuntu", Font.MEDIUM, size - 6);
        g2d.setFont(font);
        g2d.setColor(colors.getPrimaryColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
    }
    
    private static void renderKDEStyleIcon(Graphics2D g2d, String text, int size,
                                         TrayColorScheme colors) {
        // KDE allows more colorful and detailed icons
        if (colors.getBackgroundColor() != null) {
            g2d.setColor(colors.getBackgroundColor());
            g2d.fillRoundRect(1, 1, size - 2, size - 2, 4, 4);
        }
        
        Font font = new Font("Noto Sans", Font.BOLD, size - 4);
        g2d.setFont(font);
        g2d.setColor(colors.getPrimaryColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
        
        // Add accent border
        g2d.setColor(colors.getAccentColor());
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRoundRect(1, 1, size - 2, size - 2, 4, 4);
    }
    
    private static void renderXfceStyleIcon(Graphics2D g2d, String text, int size,
                                          TrayColorScheme colors) {
        // XFCE style - simple but clear
        Font font = new Font("DejaVu Sans", Font.PLAIN, size - 5);
        g2d.setFont(font);
        g2d.setColor(colors.getPrimaryColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
    }
    
    private static void renderGenericLinuxIcon(Graphics2D g2d, String text, int size,
                                             TrayColorScheme colors) {
        // Generic Linux icon - conservative approach
        Font font = new Font("SansSerif", Font.PLAIN, size - 4);
        g2d.setFont(font);
        g2d.setColor(colors.getPrimaryColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
    }
    
    /**
     * Detect if Linux system is using dark theme
     */
    public static boolean isLinuxDarkTheme(LinuxDesktopEnvironment de) {
        try {
            switch (de) {
                case GNOME:
                    return detectGnomeDarkTheme();
                case KDE:
                    return detectKDEDarkTheme();
                default:
                    return detectGtkDarkTheme();
            }
        } catch (Exception e) {
            log.debug("Failed to detect Linux dark theme", e);
            return false;
        }
    }
    
    private static boolean detectGnomeDarkTheme() throws Exception {
        Process process = Runtime.getRuntime().exec(
            "gsettings get org.gnome.desktop.interface gtk-theme"
        );
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            
            String theme = reader.readLine();
            return theme != null && theme.toLowerCase().contains("dark");
        }
    }
    
    private static boolean detectKDEDarkTheme() throws Exception {
        // KDE stores theme info in config files
        String configPath = System.getProperty("user.home") + "/.config/kdeglobals";
        
        try {
            Process process = Runtime.getRuntime().exec(
                "grep -i 'colorscheme' " + configPath
            );
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line = reader.readLine();
                return line != null && line.toLowerCase().contains("dark");
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean detectGtkDarkTheme() throws Exception {
        Process process = Runtime.getRuntime().exec(
            "gsettings get org.gnome.desktop.interface color-scheme"
        );
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            
            String colorScheme = reader.readLine();
            return "prefer-dark".equals(colorScheme);
        }
    }
    
    private static TrayColorScheme getLinuxColorScheme(LinuxDesktopEnvironment de, boolean isDark) {
        if (isDark) {
            return TrayColorScheme.builder()
                .primaryColor(Color.WHITE)
                .backgroundColor(new Color(46, 52, 64, 180))
                .accentColor(new Color(136, 192, 208))
                .build();
        } else {
            return TrayColorScheme.builder()
                .primaryColor(Color.BLACK)
                .backgroundColor(new Color(236, 239, 244, 180))
                .accentColor(new Color(94, 129, 172))
                .build();
        }
    }
    
    /**
     * Check if system tray is supported in current Linux environment
     */
    public static boolean isSystemTraySupported(LinuxDesktopEnvironment de) {
        switch (de) {
            case GNOME:
                // GNOME 3.26+ removed native tray support, needs extension
                return checkGnomeTraySupport();
            case KDE:
                // KDE has good native support
                return true;
            case XFCE:
                // XFCE has good native support
                return true;
            case UNITY:
                // Unity has native support
                return true;
            default:
                // Check generic support
                return SystemTray.isSupported();
        }
    }
    
    private static boolean checkGnomeTraySupport() {
        try {
            // Check if TopIcons or similar extension is installed
            Process process = Runtime.getRuntime().exec(
                "gnome-extensions list --enabled"
            );
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("topicons") || 
                        line.contains("appindicator") ||
                        line.contains("tray")) {
                        return true;
                    }
                }
            }
            
            // Check if running under X11 (has better tray support than Wayland)
            String sessionType = System.getenv("XDG_SESSION_TYPE");
            return !"wayland".equals(sessionType);
            
        } catch (Exception e) {
            return SystemTray.isSupported();
        }
    }
    
    public enum LinuxDesktopEnvironment {
        GNOME, KDE, XFCE, UNITY, MATE, CINNAMON, UNKNOWN
    }
}

@lombok.Builder
@lombok.Data
class TrayColorScheme {
    private Color primaryColor;
    private Color backgroundColor;
    private Color accentColor;
}
```

## Performance Considerations

### 1. Update Optimization

```java
package com.noteflix.pcm.tray.performance;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TrayUpdateOptimizer {
    
    private final ReentrantLock updateLock = new ReentrantLock();
    private final AtomicReference<BufferedImage> cachedImage = new AtomicReference<>();
    private volatile long lastUpdateTime = 0;
    private volatile String lastDisplayData = "";
    
    // Configuration
    private final long MIN_UPDATE_INTERVAL = 1000; // 1 second minimum
    private final long FAST_UPDATE_INTERVAL = 100; // For animations
    
    /**
     * Check if tray icon needs updating
     */
    public boolean shouldUpdate(String currentDisplayData, boolean forceUpdate) {
        long currentTime = System.currentTimeMillis();
        
        if (forceUpdate) {
            return true;
        }
        
        // Check if enough time has passed
        if (currentTime - lastUpdateTime < MIN_UPDATE_INTERVAL) {
            return false;
        }
        
        // Check if display data has changed
        return !currentDisplayData.equals(lastDisplayData);
    }
    
    /**
     * Cache and return optimized image
     */
    public BufferedImage getCachedOrUpdate(String displayData, 
                                          ImageGenerator generator) {
        
        if (!shouldUpdate(displayData, false)) {
            BufferedImage cached = cachedImage.get();
            if (cached != null) {
                return cached;
            }
        }
        
        if (updateLock.tryLock()) {
            try {
                // Double-check after acquiring lock
                if (!shouldUpdate(displayData, false)) {
                    BufferedImage cached = cachedImage.get();
                    if (cached != null) {
                        return cached;
                    }
                }
                
                // Generate new image
                BufferedImage newImage = generator.generateImage();
                
                // Cache the result
                cachedImage.set(newImage);
                lastDisplayData = displayData;
                lastUpdateTime = System.currentTimeMillis();
                
                return newImage;
                
            } catch (Exception e) {
                log.error("Error generating tray image", e);
                return cachedImage.get(); // Return cached version on error
            } finally {
                updateLock.unlock();
            }
        } else {
            // If we can't get the lock, return cached image
            BufferedImage cached = cachedImage.get();
            if (cached != null) {
                return cached;
            }
            
            // Fallback: generate directly (should be rare)
            return generator.generateImage();
        }
    }
    
    /**
     * Force immediate update
     */
    public BufferedImage forceUpdate(String displayData, ImageGenerator generator) {
        updateLock.lock();
        try {
            BufferedImage newImage = generator.generateImage();
            cachedImage.set(newImage);
            lastDisplayData = displayData;
            lastUpdateTime = System.currentTimeMillis();
            return newImage;
        } finally {
            updateLock.unlock();
        }
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        updateLock.lock();
        try {
            cachedImage.set(null);
            lastDisplayData = "";
            lastUpdateTime = 0;
        } finally {
            updateLock.unlock();
        }
    }
    
    @FunctionalInterface
    public interface ImageGenerator {
        BufferedImage generateImage();
    }
}
```

### 2. Memory Management

```java
package com.noteflix.pcm.tray.performance;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TrayImageCache {
    
    private final ConcurrentHashMap<String, SoftReference<BufferedImage>> imageCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> accessTimes = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    private final int MAX_CACHE_SIZE = 50;
    private final long MAX_AGE_MS = 60_000; // 1 minute
    
    /**
     * Get cached image or generate new one
     */
    public BufferedImage getOrGenerate(String key, ImageGenerator generator) {
        // Try to get from cache first
        SoftReference<BufferedImage> ref = imageCache.get(key);
        if (ref != null) {
            BufferedImage cached = ref.get();
            if (cached != null && !isExpired(key)) {
                accessTimes.put(key, System.currentTimeMillis());
                cacheHits.incrementAndGet();
                return cached;
            } else {
                // Remove expired or garbage collected entry
                imageCache.remove(key);
                accessTimes.remove(key);
            }
        }
        
        // Generate new image
        cacheMisses.incrementAndGet();
        BufferedImage newImage = generator.generateImage();
        
        // Cache the new image
        cacheImage(key, newImage);
        
        return newImage;
    }
    
    /**
     * Cache an image with soft reference
     */
    public void cacheImage(String key, BufferedImage image) {
        // Clean up expired entries before adding new one
        cleanupExpiredEntries();
        
        // Enforce cache size limit
        if (imageCache.size() >= MAX_CACHE_SIZE) {
            evictOldestEntry();
        }
        
        imageCache.put(key, new SoftReference<>(image));
        accessTimes.put(key, System.currentTimeMillis());
    }
    
    /**
     * Check if cache entry is expired
     */
    private boolean isExpired(String key) {
        Long accessTime = accessTimes.get(key);
        if (accessTime == null) {
            return true;
        }
        
        return System.currentTimeMillis() - accessTime > MAX_AGE_MS;
    }
    
    /**
     * Clean up expired cache entries
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        accessTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > MAX_AGE_MS) {
                imageCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Evict oldest cache entry
     */
    private void evictOldestEntry() {
        String oldestKey = accessTimes.entrySet().stream()
            .min(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse(null);
            
        if (oldestKey != null) {
            imageCache.remove(oldestKey);
            accessTimes.remove(oldestKey);
        }
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return CacheStats.builder()
            .size(imageCache.size())
            .hits(cacheHits.get())
            .misses(cacheMisses.get())
            .hitRatio(calculateHitRatio())
            .build();
    }
    
    private double calculateHitRatio() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * Clear all cached images
     */
    public void clear() {
        imageCache.clear();
        accessTimes.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
    }
    
    @lombok.Builder
    @lombok.Data
    public static class CacheStats {
        private int size;
        private long hits;
        private long misses;
        private double hitRatio;
    }
    
    @FunctionalInterface
    public interface ImageGenerator {
        BufferedImage generateImage();
    }
}
```

## Configuration & Customization

### 1. Configuration Schema

```yaml
# tray-widget-config.yml
tray:
  widget:
    # Display configuration
    display:
      mode: TIME_AND_DURATION  # CURRENT_TIME, SESSION_DURATION, TIME_AND_DURATION, STATUS_INDICATOR, COMPACT_INFO
      theme: AUTO  # LIGHT, DARK, AUTO
      showSeconds: false
      showProgressBar: true
      updateInterval: 1000  # milliseconds
      
    # Text configuration  
    text:
      primaryFont:
        family: "SansSerif"
        size: 9
        style: BOLD
      secondaryFont:
        family: "SansSerif"  
        size: 7
        style: PLAIN
      
    # Color configuration
    colors:
      auto: true
      light:
        primary: "#000000"
        background: "#F0F0F0E6"
        accent: "#0078D4"
      dark:
        primary: "#FFFFFF"
        background: "#202020B4"
        accent: "#64C8FF"
        
    # Platform-specific settings
    platform:
      windows:
        highDPI: true
        themeAware: true
      macos:
        useTemplate: true
        hideInDock: false
      linux:
        desktopOptimized: true
        
    # Animation settings
    animation:
      enabled: true
      blinkDuration: 500
      fadeEnabled: false
      
    # Notification settings  
    notifications:
      enabled: true
      showOnStatusChange: true
      showOnCheckIn: true
      showOnCheckOut: true
      showOnBreak: true
      duration: 5000
      
    # Performance settings
    performance:
      cacheEnabled: true
      cacheSize: 50
      cacheExpiry: 60000
      minUpdateInterval: 1000
      
  # Context menu configuration
  contextMenu:
    items:
      - type: "displayMode"
        label: "Display Mode"
        submenu: true
      - type: "separator"
      - type: "action"
        label: "Check In"
        action: "checkIn"
        condition: "notCheckedIn"
      - type: "action" 
        label: "Check Out"
        action: "checkOut"
        condition: "checkedIn"
      - type: "action"
        label: "Take Break"
        action: "startBreak"
        condition: "canStartBreak"
      - type: "separator"
      - type: "action"
        label: "Show Application"
        action: "showApp"
      - type: "action"
        label: "Settings"
        action: "openSettings"
      - type: "separator"
      - type: "action"
        label: "Exit"
        action: "exit"
```

### 2. Configuration Manager

```java
package com.noteflix.pcm.tray.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "tray.widget")
@Data
public class TrayWidgetConfiguration {
    
    private DisplayConfig display = new DisplayConfig();
    private TextConfig text = new TextConfig();
    private ColorConfig colors = new ColorConfig();
    private PlatformConfig platform = new PlatformConfig();
    private AnimationConfig animation = new AnimationConfig();
    private NotificationConfig notifications = new NotificationConfig();
    private PerformanceConfig performance = new PerformanceConfig();
    
    @Data
    public static class DisplayConfig {
        private TrayDisplayMode mode = TrayDisplayMode.TIME_AND_DURATION;
        private TrayDisplayTheme theme = TrayDisplayTheme.AUTO;
        private boolean showSeconds = false;
        private boolean showProgressBar = true;
        private int updateInterval = 1000;
    }
    
    @Data
    public static class TextConfig {
        private FontConfig primaryFont = new FontConfig("SansSerif", 9, Font.BOLD);
        private FontConfig secondaryFont = new FontConfig("SansSerif", 7, Font.PLAIN);
        
        @Data
        public static class FontConfig {
            private String family;
            private int size;
            private int style;
            
            public FontConfig() {}
            
            public FontConfig(String family, int size, int style) {
                this.family = family;
                this.size = size;
                this.style = style;
            }
            
            public Font toFont() {
                return new Font(family, style, size);
            }
        }
    }
    
    @Data
    public static class ColorConfig {
        private boolean auto = true;
        private ThemeColors light = new ThemeColors("#000000", "#F0F0F0E6", "#0078D4");
        private ThemeColors dark = new ThemeColors("#FFFFFF", "#202020B4", "#64C8FF");
        
        @Data
        public static class ThemeColors {
            private String primary;
            private String background;
            private String accent;
            
            public ThemeColors() {}
            
            public ThemeColors(String primary, String background, String accent) {
                this.primary = primary;
                this.background = background;
                this.accent = accent;
            }
            
            public Color getPrimaryColor() {
                return Color.decode(primary);
            }
            
            public Color getBackgroundColor() {
                return parseColorWithAlpha(background);
            }
            
            public Color getAccentColor() {
                return Color.decode(accent);
            }
            
            private Color parseColorWithAlpha(String colorStr) {
                if (colorStr.length() == 9 && colorStr.startsWith("#")) {
                    // #RRGGBBAA format
                    int rgba = Long.valueOf(colorStr.substring(1), 16).intValue();
                    return new Color(rgba, true);
                } else {
                    return Color.decode(colorStr);
                }
            }
        }
    }
    
    @Data
    public static class PlatformConfig {
        private WindowsConfig windows = new WindowsConfig();
        private MacOSConfig macos = new MacOSConfig();
        private LinuxConfig linux = new LinuxConfig();
        
        @Data
        public static class WindowsConfig {
            private boolean highDPI = true;
            private boolean themeAware = true;
        }
        
        @Data
        public static class MacOSConfig {
            private boolean useTemplate = true;
            private boolean hideInDock = false;
        }
        
        @Data
        public static class LinuxConfig {
            private boolean desktopOptimized = true;
        }
    }
    
    @Data
    public static class AnimationConfig {
        private boolean enabled = true;
        private int blinkDuration = 500;
        private boolean fadeEnabled = false;
    }
    
    @Data  
    public static class NotificationConfig {
        private boolean enabled = true;
        private boolean showOnStatusChange = true;
        private boolean showOnCheckIn = true;
        private boolean showOnCheckOut = true;
        private boolean showOnBreak = true;
        private int duration = 5000;
    }
    
    @Data
    public static class PerformanceConfig {
        private boolean cacheEnabled = true;
        private int cacheSize = 50;
        private int cacheExpiry = 60000;
        private int minUpdateInterval = 1000;
    }
}
```

## Testing & Validation

### 1. Visual Testing Framework

```java
package com.noteflix.pcm.tray.test;

import com.noteflix.pcm.tray.DynamicTrayIconManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TrayIconVisualTest {
    
    private DynamicTrayIconManager iconManager;
    private final String TEST_OUTPUT_DIR = "target/tray-test-images";
    
    @BeforeEach
    void setUp() {
        iconManager = DynamicTrayIconManager.getInstance();
        
        // Create output directory
        new File(TEST_OUTPUT_DIR).mkdirs();
    }
    
    @Test
    void testAllDisplayModes() throws IOException {
        Dimension testSize = new Dimension(32, 32);
        
        for (DynamicTrayIconManager.TrayDisplayMode mode : DynamicTrayIconManager.TrayDisplayMode.values()) {
            iconManager.setDisplayMode(mode);
            
            BufferedImage image = iconManager.generateTrayImage(testSize);
            
            // Save for visual inspection
            String fileName = String.format("%s/display_mode_%s.png", TEST_OUTPUT_DIR, mode.name().toLowerCase());
            ImageIO.write(image, "PNG", new File(fileName));
        }
    }
    
    @Test
    void testThemeVariations() throws IOException {
        Dimension testSize = new Dimension(32, 32);
        
        for (DynamicTrayIconManager.TrayDisplayTheme theme : DynamicTrayIconManager.TrayDisplayTheme.values()) {
            iconManager.setTheme(theme);
            
            BufferedImage image = iconManager.generateTrayImage(testSize);
            
            String fileName = String.format("%s/theme_%s.png", TEST_OUTPUT_DIR, theme.name().toLowerCase());
            ImageIO.write(image, "PNG", new File(fileName));
        }
    }
    
    @Test
    void testDifferentSizes() throws IOException {
        int[] sizes = {16, 20, 22, 24, 32, 48, 64};
        
        for (int size : sizes) {
            Dimension testSize = new Dimension(size, size);
            BufferedImage image = iconManager.generateTrayImage(testSize);
            
            String fileName = String.format("%s/size_%dx%d.png", TEST_OUTPUT_DIR, size, size);
            ImageIO.write(image, "PNG", new File(fileName));
        }
    }
    
    @Test
    void testAnimationFrames() throws IOException {
        Dimension testSize = new Dimension(32, 32);
        iconManager.setDisplayMode(DynamicTrayIconManager.TrayDisplayMode.STATUS_INDICATOR);
        
        // Simulate animation frames
        for (int frame = 0; frame < 10; frame++) {
            // Set animation state
            iconManager.setAnimationFrame(frame);
            
            BufferedImage image = iconManager.generateTrayImage(testSize);
            
            String fileName = String.format("%s/animation_frame_%02d.png", TEST_OUTPUT_DIR, frame);
            ImageIO.write(image, "PNG", new File(fileName));
        }
    }
}
```

### 2. Performance Testing

```java
package com.noteflix.pcm.tray.test;

import com.noteflix.pcm.tray.DynamicTrayIconManager;
import com.noteflix.pcm.tray.performance.TrayUpdateOptimizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TrayPerformanceTest {
    
    private DynamicTrayIconManager iconManager;
    private TrayUpdateOptimizer updateOptimizer;
    
    @BeforeEach
    void setUp() {
        iconManager = DynamicTrayIconManager.getInstance();
        updateOptimizer = new TrayUpdateOptimizer();
    }
    
    @Test
    void testImageGenerationPerformance() {
        Dimension testSize = new Dimension(32, 32);
        int iterations = 1000;
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            BufferedImage image = iconManager.generateTrayImage(testSize);
            assertNotNull(image);
            assertEquals(testSize.width, image.getWidth());
            assertEquals(testSize.height, image.getHeight());
        }
        
        long endTime = System.currentTimeMillis();
        long avgTime = (endTime - startTime) / iterations;
        
        // Assert reasonable performance (should be < 10ms per image)
        assertTrue(avgTime < 10, "Image generation too slow: " + avgTime + "ms average");
        
        System.out.println("Average image generation time: " + avgTime + "ms");
    }
    
    @Test
    void testConcurrentUpdate() throws InterruptedException {
        int threadCount = 10;
        int updatesPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < updatesPerThread; j++) {
                        String displayData = "thread-" + threadId + "-update-" + j;
                        
                        BufferedImage image = updateOptimizer.getCachedOrUpdate(
                            displayData,
                            () -> iconManager.generateTrayImage(new Dimension(32, 32))
                        );
                        
                        assertNotNull(image);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Concurrent updates timed out");
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long totalUpdates = threadCount * updatesPerThread;
        
        System.out.println("Concurrent updates completed in " + totalTime + "ms");
        System.out.println("Total updates: " + totalUpdates);
        System.out.println("Average time per update: " + (totalTime / totalUpdates) + "ms");
        
        executor.shutdown();
    }
    
    @Test
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Generate many images
        for (int i = 0; i < 1000; i++) {
            BufferedImage image = iconManager.generateTrayImage(new Dimension(32, 32));
            // Simulate normal usage - don't hold references
            image = null;
            
            if (i % 100 == 0) {
                System.gc(); // Suggest garbage collection
                Thread.yield();
            }
        }
        
        System.gc();
        Thread.yield();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (< 10MB for 1000 images)
        long maxMemoryIncrease = 10 * 1024 * 1024; // 10MB
        assertTrue(memoryIncrease < maxMemoryIncrease, 
            "Memory usage too high: " + (memoryIncrease / 1024 / 1024) + "MB increase");
        
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + "MB");
    }
}
```

---

**Tổng kết Implementation:**

✅ **Dynamic Tray Widget với:**
- Real-time hiển thị check-in/session time
- Multiple display modes (time, duration, combined, status)
- Cross-platform optimizations (Windows/macOS/Linux)
- Custom graphics rendering với text, icons, animations
- Performance optimization với caching và update throttling
- Comprehensive configuration system
- Full testing framework

✅ **Key Features:**
- Time tracking integration
- Theme-aware colors
- Platform-specific icon styles  
- Animation support
- Context menu integration
- Performance monitoring
- Visual testing tools

Đây là complete solution cho custom tray widget thay thế simple icon, hiển thị thông tin time tracking real-time với professional quality và cross-platform compatibility.