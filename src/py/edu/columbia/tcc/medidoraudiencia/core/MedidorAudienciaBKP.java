package py.edu.columbia.tcc.medidoraudiencia.core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import py.edu.columbia.tcc.medidoraudiencia.objects.Rostro;
import py.edu.columbia.tcc.medidoraudiencia.utils.Cons;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class MedidorAudienciaBKP extends Thread {

    private final CascadeClassifier detector;
    private int contador;
    private List<Rostro> listaRostros;
    private List<Rostro> listaRostrosCandidatos;
    private boolean reset;
    private MedidorAudienciaListener listener;
    private boolean encuadrar;
    private double factorEscala;
    private double tamanoRect;
    private int minVecinos;

    public MedidorAudienciaBKP() {
        System.load(getClass().getResource(Cons.LIBRERIA_OPENCV).getPath());
        factorEscala = 1.05;
        tamanoRect = 50;
        contador = 0;
        minVecinos = 1;
        reset = false;
        listaRostros = new ArrayList();
        detector = new CascadeClassifier(getClass().getResource(Cons.HAARCASCADE_FRONTALFACE_ALT).getPath());
    }

    @Override
    public void run() {
        detectarAudiencia();
    }

    private void detectarAudiencia() {
        VideoCapture video = new VideoCapture(0);

        Mat captura = new Mat();
        MatOfRect matParaRostros = new MatOfRect();

        while (!isInterrupted()) {
            if (video.grab()) {
                try {
                    if (reset) {
                        resetearContadores();
                    }

                    List<Rostro> rostrosDetectados = new ArrayList();

                    video.retrieve(captura);

                    Mat capturaGris = new Mat();
                    Imgproc.cvtColor(captura, capturaGris, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.equalizeHist(capturaGris, capturaGris);

                    detector.detectMultiScale(capturaGris, matParaRostros, factorEscala, minVecinos, 0, new Size(tamanoRect, tamanoRect), new Size(200, 200));

                    for (Rect rect : matParaRostros.toArray()) {
                        Rostro rostro = new Rostro();
                        rostro.setCentroX(rect.x + rect.width / 2);
                        rostro.setCentroY(rect.y + rect.height / 2);
                        rostro.setAlto(rect.height);
                        rostro.setAncho(rect.width);
                        rostrosDetectados.add(rostro);
                    }
                    cruzarRostros(listaRostros, rostrosDetectados);

                    if (listener != null) {
                        Mat img = captura;
                        if (encuadrar) {
                            encuadrarRostros(img, listaRostros);
                        }
                        listener.onNuevaImagen(convertToByteArrayInputStream(img));
                    }

                } catch (Exception ex) {
                    System.out.println("Error");
                    ex.printStackTrace();
                }
            }
        }
        video.release();
    }

    private void cruzarRostros(List<Rostro> rostrosRastreados, List<Rostro> rostrosDetectados) {
        for (Rostro rostroRastreado : rostrosRastreados) {
            rostroRastreado.setMatcheado(false);
        }

        for (Rostro rostroDetectado : rostrosDetectados) {
            for (Rostro rostroRastreado : rostrosRastreados) {
                if (!rostroRastreado.isMatcheado()) {
                    if (rostroRastreado.isRostroAproximado(rostroDetectado)) {
                        rostroRastreado.setAncho(rostroDetectado.getAncho());
                        rostroRastreado.setCentroX(rostroDetectado.getCentroX());
                        rostroRastreado.setCentroY(rostroDetectado.getCentroY());
                        rostroRastreado.setMatcheado(true);
                        rostroRastreado.setFechaHasta(new Date());

                        rostroDetectado.setMatcheado(true);
                        break;
                    }
                }
            }
        }

        for (Rostro rostroDetectado : rostrosDetectados) {
            if (!rostroDetectado.isMatcheado()) {
                contador++;
                rostroDetectado.setFechaDesde(new Date());
                rostroDetectado.setFechaHasta(new Date());
                rostroDetectado.setId(contador);
                rostroDetectado.setMatcheado(true);
                rostrosRastreados.add(rostroDetectado);
            }
        }

        Date ahora = new Date();
        for (int i = 0; i < rostrosRastreados.size(); i++) {
            if (ahora.getTime() - rostrosRastreados.get(i).getFechaHasta().getTime() >= Cons.TOLERANCIA_ROSTRO_PERDIDO * 1000) {
                rostrosRastreados.remove(i);
                i--;
            }
        }
    }

    private void encuadrarRostros(Mat img, List<Rostro> listaRostros) {
        for (Rostro rostro : listaRostros) {
            Imgproc.circle(img, new Point(rostro.getCentroX(), rostro.getCentroY()), rostro.getAlto() / 2, new Scalar(0, rostro.isMatcheado() ? 255 : 0, rostro.isMatcheado() ? 0 : 255));
//            Imgproc.circle(captura, new Point(rect.x + rect.width / 2, rect.y + rect.height / 2), 5 + rect.width / 2, new Scalar(0, 255, 0));
        }
    }

    private ByteArrayInputStream convertToByteArrayInputStream(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, mob);
        return new ByteArrayInputStream(mob.toArray());
    }

    public void reset() {
        reset = true;
    }

    private void resetearContadores() {
        reset = false;
        listaRostros.clear();
        contador = 0;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

    public List<Rostro> getListaRostros() {
        return listaRostros;
    }

    public void setListaRostros(List<Rostro> listaRostros) {
        this.listaRostros = listaRostros;
    }

    public MedidorAudienciaListener getListener() {
        return listener;
    }

    public void setListener(MedidorAudienciaListener listener) {
        this.listener = listener;
    }

    public boolean isEncuadrar() {
        return encuadrar;
    }

    public void setEncuadrar(boolean encuadrar) {
        this.encuadrar = encuadrar;
    }
}
