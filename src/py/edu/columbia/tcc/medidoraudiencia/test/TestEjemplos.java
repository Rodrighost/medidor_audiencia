/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.columbia.tcc.medidoraudiencia.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class TestEjemplos {

    public static void main(String[] args) throws IOException {
        System.out.println("Buscando librerías en... " + System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Ejemplos ejemplo = new Ejemplos();
        ejemplo.ejUmbralizacion();
//        Testing test = new Testing();
//        test.tstHaarCascadeRostros();
        //test.tstMedidorAudiencia();
//        iniciarMedicionAudiencia();
    }

    private static void iniciarMedicionAudiencia() {
        JFrame ventana = new JFrame();
        JLabel etiqueta = new JLabel();
        ventana.getContentPane().add(etiqueta);
        ventana.pack();
        ventana.setVisible(true);

        VideoCapture capturador = new VideoCapture();
        capturador.open(0);
        Mat frame = new Mat();

        if (!capturador.isOpened()) {
            System.out.println("Error al abrir cámara");
        } else {
            while (true) {
                if (capturador.read(frame)) {
                    try {
                        setLabelIcon(frame, etiqueta);
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }

    private static void setLabelIcon(Mat frame, JLabel label) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, mob);
        InputStream is = new ByteArrayInputStream(mob.toArray());
        label.setIcon(new ImageIcon(ImageIO.read(is)));
    }
}
