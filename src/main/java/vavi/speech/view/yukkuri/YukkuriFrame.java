/*
 * Copyright (c) 2022-2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;


/**
 * Transparent draggable Yukkuri character window with floating animation,
 * evolved from {@code CharacterFrame} ({@code githash:e32fb51}).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class YukkuriFrame extends JFrame {

    private final YukkuriCharacter character;
    private final JPanel panel;

    /** not null during dragging */
    private Point start;
    /** indicates last location of this frame */
    private Point point;

    private class FloatingSystem {
        long interval = 50;
        float resolution = 20f;
        int magnification = 20;
        float radian;
        private ScheduledExecutorService scheduler;
        private ScheduledFuture<?> task;

        private Runnable newTask() {
            return () -> {
                int d = (int) (Math.sin(radian += 2 / this.resolution) * this.magnification);
                if (start == null && point != null) {
                    SwingUtilities.invokeLater(() -> setLocation(point.x, point.y + d));
                }
            };
        }

        synchronized void start() {
            if (task != null && !task.isDone()) {
                return;
            }
            point = getLocation();
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "YukkuriFloating");
                t.setDaemon(true);
                return t;
            });
            task = scheduler.scheduleAtFixedRate(newTask(), 0, this.interval, TimeUnit.MILLISECONDS);
        }

        synchronized void stop() {
            if (task != null) {
                task.cancel(false);
                task = null;
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
                scheduler = null;
            }
        }
    }

    private final FloatingSystem floatingSystem = new FloatingSystem();

    public YukkuriFrame(Path characterDir) throws IOException {
        this(new YukkuriCharacter(characterDir));
    }

    public YukkuriFrame(YukkuriCharacter character) {
        this.character = Objects.requireNonNull(character, "character");

        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setComposite(AlphaComposite.Src);
                    g2.drawImage(character.getImage(), 0, 0, null);
                } finally {
                    g2.dispose();
                }
            }
        };
        this.panel.setPreferredSize(new Dimension(YukkuriCharacter.W, YukkuriCharacter.H));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Component c = e.getComponent();
                point = c.getLocation();
                c.setLocation(point);
                start = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                start = e.getPoint();
                point = e.getLocationOnScreen();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (start == null) {
                    return;
                }
                Component c = e.getComponent();
                point = c.getLocation(point);
                int x = point.x - start.x + e.getX();
                int y = point.y - start.y + e.getY();
                c.setLocation(x, y);
            }
        });

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JRootPane root = getRootPane();
        root.putClientProperty("Window.shadow", false);

        getContentPane().add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public YukkuriCharacter getCharacter() {
        return character;
    }

    public void refresh() {
        panel.repaint();
    }

    public void setFloating(boolean b) {
        if (b) {
            floatingSystem.start();
        } else {
            floatingSystem.stop();
        }
    }
}
