package test3d;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.meteoinfo.chart.plot3d.JSurfacePanel;
import org.meteoinfo.chart.plot3d.surface.ArraySurfaceModel;

public class SimpleRun {

    public void testSomething() {
        JSurfacePanel jsp = new JSurfacePanel();
        jsp.setTitleText("Hello");

        JFrame jf = new JFrame("test");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(jsp, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);

        Random rand = new Random();
        int yn = 20;
        int xn = 20;
        float[][] z1 = new float[yn][xn];
        float[][] z2 = new float[yn][xn];
        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                //z1[i][j] = rand.nextFloat() * 20 - 10f;
                z2[i][j] = rand.nextFloat() * 20 - 10f;
                z1[i][j] = (float)(Math.sin(i * 0.5) + Math.cos(j));
                //z1[i][j] = (float)(i * Math.sin(i * j));
            }
        }
        ArraySurfaceModel sm = new ArraySurfaceModel();
        sm.setValues(0f, 20f, 0f, 20f, z1, z2);
        jsp.setModel(sm);
        // sm.doRotate();

        // canvas.doPrint();
        // sm.doCompute();
    }

    public static float f1(float x, float y) {
        // System.out.print('.');
        return (float) (Math.sin(x * x + y * y) / (x * x + y * y));
        // return (float)(10*x*x+5*y*y+8*x*y -5*x+3*y);
    }

    public static float f2(float x, float y) {
        return (float) (Math.sin(x * x - y * y) / (x * x + y * y));
        // return (float)(10*x*x+5*y*y+15*x*y-2*x-y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new SimpleRun().testSomething();
            }
        });

    }

}
