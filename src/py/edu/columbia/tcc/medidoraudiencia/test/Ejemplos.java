/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.columbia.tcc.medidoraudiencia.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 *
 * @author Rodrigo
 */
public class Ejemplos {

    private String personas1 = "resources/personas1.jpg";
    private String personas2 = "resources/personas2.jpg";
    private String personasTmp = "resources/personasTmp.jpg";

    /**
     * Cargar de imgs del hdd y la muestrar en ventanas En el ejemplo se carga y
     * muestra una imagen, en colores en escala de grises
     */
    public void ejCargarMostrar() {
        Imgcodecs imageCodecs = new Imgcodecs();

        Mat matPers1 = imageCodecs.imread(personas1);
        Mat matPers1Gris = imageCodecs.imread(personas1, Imgcodecs.IMREAD_GRAYSCALE);
        mostrarEnPantalla(matPers1);
        mostrarEnPantalla(matPers1Gris);
    }

    /**
     * Guardar Mat en un archivo de imagen En el ejemplo se lee un archivo, y
     * luego se guarda el mismo con otro nombre.
     */
    public void ejGuardarMat() {
        Imgcodecs imageCodecs = new Imgcodecs();

        Mat matPersonas1 = imageCodecs.imread(personas1);

        imageCodecs.imwrite(personasTmp, matPersonas1);
    }

    /**
     * Detecta los rostros en una imagen y los muestra en pantalla
     */
    public void ejDeteccionDeRostros() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas1);
        String xmlFile = "resources/haarcascade_frontalface_alt2.xml";

        CascadeClassifier classifier = new CascadeClassifier(xmlFile);

        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(matPers1, faceDetections);//Deteccion de Rostros

        System.out.println(String.format("Rostros detectados: %s", faceDetections.toArray().length));

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    matPers1, // Donde se va dibujar 
                    new Point(rect.x, rect.y), //Punto1
                    new Point(rect.x + rect.width, rect.y + rect.height), //Punto2 
                    new Scalar(0, 0, 255), // Color de línea 
                    3 //Grosor de línea
            );
        }
        mostrarEnPantalla(matPers1);
    }

    /**
     * Convertir una imagen color a escala de gris
     */
    public void ejConvertirColor() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas1);
        Mat matConvertido = new Mat();
        Imgproc.cvtColor(matPers1, matConvertido, Imgproc.COLOR_RGB2GRAY);
        mostrarEnPantalla(matConvertido);
    }

    /**
     * Umbralizar una imagen
     */
    public void ejUmbralizacion() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas2);
        Mat matUmbralizado = new Mat();

        Imgproc.cvtColor(matPers1, matUmbralizado, Imgproc.COLOR_RGB2GRAY);

        Imgproc.threshold(matUmbralizado, matUmbralizado, 200, 500, Imgproc.THRESH_BINARY);

        mostrarEnPantalla(matUmbralizado);
    }

    /**
     * Dibujar figuras básicas: circulo, linea, rectangulo y elipse
     */
    public void ejDibujarFigurasBasicas() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas1);
        Imgproc.circle(
                matPers1, //Imagen
                new Point(230, 160), //Centro del circulo
                100, //Radio en px
                new Scalar(0, 0, 255), //Color del circulo
                10 //Grosor de la linea
        );
        Imgproc.line(
                matPers1,
                new Point(10, 50), //punto inicio
                new Point(100, 100), //punto fin
                new Scalar(0, 255, 0),
                10
        );
        Imgproc.rectangle(
                matPers1,
                new Point(10, 10), //punto inicio
                new Point(150, 200), //punto fin
                new Scalar(255, 0, 0),
                10
        );
        Imgproc.ellipse(
                matPers1,
                new RotatedRect( // RotatedRect(Point c, Size s, double a)
                        new Point(200, 150),
                        new Size(260, 180),
                        180
                ),
                new Scalar(255, 0, 255),
                10
        );
        mostrarEnPantalla(matPers1);
    }

    /**
     * Dibujar figuras avanzadas: Multilineas, multilineas rellenas, y lineas de
     * flecha
     */
    public void ejDibujarFigurasAvanzadas() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas1);

        List<MatOfPoint> list = new ArrayList();
        list.add(
                new MatOfPoint(
                        new Point(75, 100), new Point(350, 100),
                        new Point(75, 150), new Point(350, 150),
                        new Point(75, 200), new Point(350, 200),
                        new Point(75, 250), new Point(350, 250)
                )
        );
        Imgproc.polylines(
                matPers1,
                list,
                true, // isClosed. En true: traza una línea desde el primer punto hasta el último
                new Scalar(0, 0, 0),
                10
        );

        MatOfPoint matOfPoint = new MatOfPoint(
                new Point(75, 100), new Point(350, 100),
                new Point(75, 150), new Point(350, 150),
                new Point(75, 200), new Point(350, 200),
                new Point(75, 250), new Point(350, 250)
        );
        Imgproc.fillConvexPoly(
                matPers1,
                matOfPoint,
                new Scalar(0, 0, 255)
        );
        mostrarEnPantalla(matPers1);

        Imgproc.arrowedLine(
                matPers1,
                new Point(200, 400),
                new Point(200, 500),
                new Scalar(0, 0, 0)
        );
    }

    /**
     * Insertar texto en la imagen
     */
    public void ejInsertarTexto() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas2);

        Imgproc.putText(
                matPers1,
                "Insertando texto",
                new Point(100, 50), //Punto de inserción 
                Core.FONT_HERSHEY_SIMPLEX, //Fuente
                0.5, // Escala de la fuente
                new Scalar(0, 0, 0), // Color
                2 // Grosor
        );
        mostrarEnPantalla(matPers1);
    }

    /**
     * Blurs
     */
    public void ejBlur() {
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat matPers1 = imageCodecs.imread(personas2);

        Mat average = new Mat();
        Imgproc.blur(matPers1, average, new Size(45, 45), new Point(20, 30), Core.BORDER_DEFAULT);
        mostrarEnPantalla(average);

        Mat gaussian = new Mat();
        Imgproc.GaussianBlur(matPers1, gaussian, new Size(3, 3), 0);
        mostrarEnPantalla(gaussian);

        Mat median = new Mat();
        Imgproc.medianBlur(matPers1, median, 15);
        mostrarEnPantalla(median);
    }

    /**
     * Muestra en pantalla un objeto Mat
     *
     * @param mat Objeto Mat a mostrar
     */
    public void mostrarEnPantalla(Mat mat) {
        try {
            Imgcodecs imageCodecs = new Imgcodecs();
            MatOfByte mobPers1 = new MatOfByte();
            imageCodecs.imencode(".jpg", mat, mobPers1);
            byte[] baPers1 = mobPers1.toArray();
            InputStream isPers1 = new ByteArrayInputStream(baPers1);
            BufferedImage biPers1 = ImageIO.read(isPers1);
            JFrame frmPers1 = new JFrame();
            frmPers1.getContentPane().add(new JLabel(new ImageIcon(biPers1)));
            frmPers1.pack();
            frmPers1.setVisible(true);
        } catch (IOException ex) {
            System.out.println("Error al mostrar en pantalla");
        }
    }
}
