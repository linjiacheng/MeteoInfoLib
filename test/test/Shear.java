/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Surface extends JPanel {

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        AffineTransform tx1 = new AffineTransform();
        tx1.translate(50, 90);

        g2d.setTransform(tx1);
        g2d.setPaint(Color.green);
        g2d.drawRect(0, 0, 160, 50);

//        for (double i = 0; i <= 90; i+=10){
//            AffineTransform tx = new AffineTransform();
//            tx.translate(130, 10);
//            tx.rotate(Math.toRadians(i));
//            g2d.setTransform(tx);
//            g2d.setPaint(Color.red);
//            //g2d.draw(new Rectangle(0, 0, 160, 100));
//            g2d.drawLine(0, 0, 160, 0);
//        }
//        for (double i = 10; i <= 90; i+=10){
//            AffineTransform tx = new AffineTransform();
//            tx.translate(130, 10);
//            tx.shear(0, Math.cos(Math.toRadians(i)));
//            g2d.setTransform(tx);
//            g2d.setPaint(Color.blue);
//            //g2d.draw(new Rectangle(0, 0, 160, 100));
//            g2d.drawLine(0, 0, 160, 0);
//        }
        
        AffineTransform tx2 = new AffineTransform();
        tx2.translate(140, 200);
        double angle = -20;
        double angle_y = -30;
        tx2.rotate(Math.toRadians(angle));

        g2d.setTransform(tx2);
        g2d.setPaint(Color.blue);

        g2d.draw(new Rectangle(0, 0, 160, 100));

        AffineTransform tx3 = new AffineTransform();
        angle = Math.toRadians(angle);
        angle_y = Math.toRadians(angle_y);
        tx3.setTransform(Math.cos(angle), Math.sin(angle), -Math.sin(angle_y), Math.cos(angle_y), 140, 200);
        //tx3.translate(130, 10);
        //tx3.shear(20, 0);
        //tx3.scale(2, 2);

        g2d.setTransform(tx3);
        g2d.setPaint(Color.red);
        g2d.drawRect(0, 0, 160, 100);
        
        g2d.dispose();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
}

public class Shear extends JFrame {

    public Shear() {

        initUI();
    }

    private void initUI() {

        add(new Surface());

        setTitle("Shearing");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                Shear ex = new Shear();
                ex.setVisible(true);
            }
        });
    }
}
