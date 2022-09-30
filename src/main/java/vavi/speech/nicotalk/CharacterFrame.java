package vavi.speech.nicotalk;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import vavi.util.Debug;


/**
 * CharacterFrame.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-04-25 nsano initial version <br>
 */
public class CharacterFrame extends JFrame {

    /** not null during dragging */
    private Point start;
    /** indicate last location of this frame */
    private Point point;

    private class FloatingSystem {
        long interval = 50;
        float resolution = 20f;
        int magnification = 40;
        float radian;
        private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private Runnable newTask() {
            return () -> {
                int d = (int) (Math.sin(radian += 2 / this.resolution) * this.magnification);
                if (start == null) {
                    setLocation(point.x, point.y + d);
                }
//Debug.println(point.x + ", " + (point.y + d));
            };
        };
        void start() {
            point = getLocation();
            scheduler.scheduleAtFixedRate(newTask(), 0, this.interval, TimeUnit.MILLISECONDS);
Debug.println("start");
        }
        void stop() {
            scheduler.shutdown();
Debug.println("stop");
        }
    }

    private FloatingSystem floatingSystem = new FloatingSystem();

    public CharacterFrame(String name) throws IOException {

        Character character = new Character(name);

        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(character.getImage(), 0, 0, null);
            }
        };
        panel.setPreferredSize(new Dimension(Character.W, Character.H));

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                Component c = e.getComponent();
                point = c.getLocation();
                c.setLocation(point);
                start = null;
            }
            public void mousePressed(MouseEvent e) {
                start = e.getPoint();
                point = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Component c = e.getComponent();
                point = c.getLocation(point);
                int x = point.x - start.x + e.getX();
                int y = point.y - start.y + e.getY();
                c.setLocation(x, y);
            }
        });

        // set window opacity
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // eliminate window's shadow
        JRootPane root = getRootPane();
        root.putClientProperty("Window.shadow", false);

        getContentPane().add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void setFloating(boolean b) {
        if (b) {
            floatingSystem.start();
        } else {
            floatingSystem.stop();
        }
    }
}
