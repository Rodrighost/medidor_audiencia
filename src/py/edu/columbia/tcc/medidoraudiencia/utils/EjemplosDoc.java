/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.columbia.tcc.medidoraudiencia.utils;

/**
 *
 * @author roderic
 */
//Importanción de la librerías
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import py.edu.columbia.tcc.medidoraudiencia.objects.Mano;
import py.edu.columbia.tcc.medidoraudiencia.objects.Rostro;

class EjemploCascadeClasifier {

    //Creación del Clasificador
    CascadeClassifier detectorRostros
            = new CascadeClassifier("./haarcascades/frontalface_alt2.xml");
    //Objeto del tipo Mat, matriz de píxeles
    Mat captura = new Mat();
    //Objeto para obtención de imágenes mediante una cámara digital
    VideoCapture video = new VideoCapture(0);

    void detectarRostros() {
        //Obtención de la imagen
        video.retrieve(captura);
        //Objeto donde se guardaran las coordenadas de los rostros detectados
        MatOfRect matRostrosDetectados = new MatOfRect();
        //Detección de rostros en la imagen capturada
        detectorRostros.detectMultiScale(captura, matRostrosDetectados);
        //Creación de lista: rostros detectados
        List<Rostro> rostrosDetectados = new ArrayList();
        //Por cada rostro detectado, se instancia un objeto Rostro, se setean
        // sus propiedades y se agregan a la lista de rostros detectados
        for (Rect rect : matRostrosDetectados.toArray()) {
            Rostro rostro = new Rostro();
            rostro.setCentroX(rect.x + rect.width / 2);
            rostro.setCentroY(rect.y + rect.height / 2);

            rostro.setAlto(rect.height);
            rostro.setAncho(rect.width);
            rostrosDetectados.add(rostro);
        }
    }

    //Propiedades ubicación de la clase Rostro
//    int centroX, centroY, ancho, alto;

    //Función que evalúa la proximidad entre el propio Rostro y un Rostro que 
    //recibe como parámetro
    boolean isRostroAproximado(Rostro rostro) {
        boolean esAproximado = false;
        int distanciaX = Math.abs(rostro.getCentroX() - centroX);
        int distanciaY = Math.abs(rostro.getCentroY() - centroY);
        if (distanciaX < ancho * 0.5f && distanciaY < alto * 0.5f) {
            esAproximado = true;
        }
        return esAproximado;
    }

    void ejemploBackgroundSubtractorMOG2() {
        //Instancia del objeto BackgroundSubtractorMOG2
        BackgroundSubtractorMOG2 bgs
                = Video.createBackgroundSubtractorMOG2(200, 5, false);

        Mat umbralizado = new Mat();
        //Obteción de la matriz umbralizada
        bgs.apply(captura, umbralizado);

        //Creación de un Kernel para aplicar transformaciones morfológicas
        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));

        //Transformación morfológica de apertura, para reducción de ruido
        //recibe el Mat umbralizado, y el resultado se guarda en el mismo Mat
        Imgproc.morphologyEx(umbralizado, umbralizado, Imgproc.MORPH_OPEN, kernel);
    }

    //Propiedades ubicación inicial de la clase Mano
    int centroMovX, centroMovY;
    //Propiedades ubicación de la clase Mano
    int centroX, centroY, ancho, alto;
    //Último gesto actualizado
    Mano.Gesto direccion;

    void actualizarDireccion() {
        //Inicialización de la dirección
        direccion = Mano.Gesto.NINGUNO;
        //Cálculo de distancias
        int distanciaX = centroMovX - centroX;
        int distanciaY = centroMovY - centroY;
        
        //Evaluación de la dirección
        if (Math.abs(distanciaX) > ancho * 0.5d) {
            centroMovX = centroX;
            if (distanciaX < 0) {
                direccion = Mano.Gesto.IZQUIERDA;
            } else {
                direccion = Mano.Gesto.DERECHA;
            }
        }

        if (Math.abs(distanciaY) > alto * 0.5d) {
            centroMovY = centroY;
            if (distanciaY < 0) {
                direccion = Mano.Gesto.ABAJO;
            } else {
                direccion = Mano.Gesto.ARRIBA;
            }
        }

    }
}

public class EjemplosDoc {

}
