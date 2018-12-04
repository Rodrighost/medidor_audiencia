package py.edu.columbia.tcc.medidoraudiencia.core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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
import org.opencv.objdetect.Objdetect;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import py.edu.columbia.tcc.medidoraudiencia.objects.Mano;
import py.edu.columbia.tcc.medidoraudiencia.objects.Rostro;
import py.edu.columbia.tcc.medidoraudiencia.utils.Cons;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class MedidorAudiencia extends Thread {

    private final CascadeClassifier detectorRostros;
    private final CascadeClassifier detectorManos;

    private int audiencia;
    private List<Rostro> rostrosAudiencia;
    private List<Rostro> rostrosCandidatosAudiencia;

    private List<Mano> manosAudiencia;
    private List<Mano> manosCandidatosAudiencia;

    private boolean reset;
    private MedidorAudienciaListener listener;
    private boolean encuadrar;
    private double factorEscala;
    private double tamanoRectMin;
    private double tamanoRectMax;
    private int minVecinos;
    private VideoCapture video;
    private Mano.Gesto direccion;
    private Date ultCambio;

    public MedidorAudiencia() {
        System.load(getClass().getResource(Cons.LIBRERIA_OPENCV).getPath());
        factorEscala = Cons.FACTOR_ESCALA;
        tamanoRectMin = Cons.TAMANHO_REC_MIN;
        tamanoRectMax = Cons.TAMANHO_REC_MAX;
        minVecinos = Cons.MIN_VECINOS;
        audiencia = 0;
        reset = false;

        rostrosAudiencia = new ArrayList();
        rostrosCandidatosAudiencia = new ArrayList();

        manosAudiencia = new ArrayList();
        manosCandidatosAudiencia = new ArrayList();

        detectorRostros = new CascadeClassifier(getClass().getResource(Cons.HAARCASCADE_FRONTALFACE_ALT2).getPath());
        detectorManos = new CascadeClassifier(getClass().getResource(Cons.HAARCASCADE_MANO).getPath());
        video = new VideoCapture(0);
        ultCambio = new Date();
    }

    @Override
    public void run() {
        detectarAudiencia();
    }

    private void detectarAudiencia() {

        Mat captura = new Mat();
        MatOfRect matRostrosDetectados = new MatOfRect();
        MatOfRect matManosDetectados = new MatOfRect();

        BackgroundSubtractorMOG2 fgbg = Video.createBackgroundSubtractorMOG2(50, 10, false);
        int i = 0;
        while (!isInterrupted()) {
            if (video.grab()) {
                try {
                    if (reset) {
                        resetearMediciones();
                    }

                    video.retrieve(captura);

                    Mat capturaGris = new Mat();
                    Imgproc.cvtColor(captura, capturaGris, Imgproc.COLOR_RGB2GRAY);
//                    Imgproc.equalizeHist(capturaGris, capturaGris);
//                    Imgproc.blur(capturaGris, capturaGris, new Size(7, 7));

                    Mat fgmask = new Mat();
                    i++;
//                    if(i == 0){
//                        fgbg.apply(capturaGris, fgmask, 1);
//                    }else{
//                        fgbg.apply(capturaGris, fgmask, -1);
//                        i = 0;
//                }
                    fgbg.apply(capturaGris, fgmask);

                    Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
                    Imgproc.morphologyEx(fgmask, fgmask, Imgproc.MORPH_OPEN, kernel);

                    detectorRostros.detectMultiScale(captura, matRostrosDetectados, factorEscala, minVecinos, Objdetect.CASCADE_SCALE_IMAGE, new Size(tamanoRectMin, tamanoRectMin), new Size(tamanoRectMax, tamanoRectMax));
                    detectorManos.detectMultiScale(fgmask, matManosDetectados, factorEscala, minVecinos * 2, Objdetect.CASCADE_DO_ROUGH_SEARCH, new Size(tamanoRectMin, tamanoRectMin), new Size(tamanoRectMax, tamanoRectMax));

                    procesarRostros(matRostrosDetectados, rostrosAudiencia, rostrosCandidatosAudiencia);

                    procesarManos(matManosDetectados, manosAudiencia, manosCandidatosAudiencia);

                    Mat img = captura.clone();
                    if (encuadrar) {
                        encuadrarRostros(img, rostrosAudiencia);
                        encuadrarManos(img, manosAudiencia);
                    }
                    Imgproc.cvtColor(fgmask, fgmask, Imgproc.COLOR_GRAY2RGB);
                    List<Mat> srcResult = Arrays.asList(img, fgmask);
                    Core.hconcat(srcResult, img);

                    if (listener != null) {
                        listener.onNuevaImagen(convertToByteArrayInputStream(img));
                        if (direccion == null && !manosAudiencia.isEmpty()) {
                            listener.onGestoAgarrar();
                            direccion = manosAudiencia.get(0).getDireccion();
                        } else if (direccion != null && !manosAudiencia.isEmpty()) {
                            direccion = manosAudiencia.get(0).getDireccion();
                        } else if (direccion != null && manosAudiencia.isEmpty()) {
                            listener.onGestoSoltar();
                            direccion = null;
                        }

                        Date d = new Date();

                        if (direccion != null) {
                            switch (direccion) {
                                case ARRIBA:
                                    listener.onGestoArriba();
                                    break;
                                case ABAJO:
                                    listener.onGestoAbajo();
                                    break;
                            }
                            if (d.getTime() - ultCambio.getTime() > Cons.TIEMPO_ENTRE_CAMBIOS) {
                                switch (direccion) {
                                    case IZQUIERDA:
                                        listener.onGestoIzquierda();
                                        break;
                                    case DERECHA:
                                        listener.onGestoDerecha();
                                        break;
                                }
                                ultCambio = d;
                            }
                        }
                    }

                } catch (Exception ex) {
                    System.out.println("Error");
                    ex.printStackTrace();
                }
            }
        }

        video.release();
    }

    private void procesarRostros(MatOfRect matRostrosDetectados, List<Rostro> rostrosAudiencia, List<Rostro> rostrosCandidatosAudiencia) {
        List<Rostro> rostrosDetectados = new ArrayList();
        for (Rect rect : matRostrosDetectados.toArray()) {
            Rostro rostro = new Rostro();
            rostro.setCentroX(rect.x + rect.width / 2);
            rostro.setCentroY(rect.y + rect.height / 2);
            rostro.setAlto(rect.height);
            rostro.setAncho(rect.width);
            rostrosDetectados.add(rostro);
        }

        for (Rostro rostroAudiencia : rostrosAudiencia) {
            rostroAudiencia.setMatcheado(false);
        }
        for (Rostro rostroCandidato : rostrosCandidatosAudiencia) {
            rostroCandidato.setMatcheado(false);
        }

        Iterator<Rostro> iter = rostrosDetectados.iterator();
        while (iter.hasNext()) {
            Rostro rostroDetectado = iter.next();
            for (Rostro rostroAudiencia : rostrosAudiencia) {
                if (!rostroAudiencia.isMatcheado()) {
                    if (rostroAudiencia.isRostroAproximado(rostroDetectado)) {
                        rostroAudiencia.setAncho(rostroDetectado.getAncho());
                        rostroAudiencia.setAlto(rostroDetectado.getAlto());
                        rostroAudiencia.setCentroX(rostroDetectado.getCentroX());
                        rostroAudiencia.setCentroY(rostroDetectado.getCentroY());
                        rostroAudiencia.setMatcheado(true);
                        rostroAudiencia.setFechaHasta(new Date());

                        rostroDetectado.setMatcheado(true);
                        break;
                    }
                }
            }

            if (!rostroDetectado.isMatcheado()) {
                for (Rostro rostroCandidato : rostrosCandidatosAudiencia) {
                    if (!rostroCandidato.isMatcheado()) {
                        if (rostroCandidato.isRostroAproximado(rostroDetectado)) {
                            rostroCandidato.setAncho(rostroDetectado.getAncho());
                            rostroCandidato.setAlto(rostroDetectado.getAlto());
                            rostroCandidato.setCentroX(rostroDetectado.getCentroX());
                            rostroCandidato.setCentroY(rostroDetectado.getCentroY());
                            rostroCandidato.setMatcheado(true);
                            rostroCandidato.setFechaHasta(new Date());

                            rostroDetectado.setMatcheado(true);
                            break;
                        }
                    }
                }
            }
            if (rostroDetectado.isMatcheado()) {
                iter.remove();
            }
        }

        for (Rostro rostroDetectado : rostrosDetectados) {
            rostroDetectado.setFechaDesde(new Date());
            rostroDetectado.setFechaHasta(new Date());
            rostrosCandidatosAudiencia.add(rostroDetectado);
        }

        Date ahora = new Date();

        iter = rostrosCandidatosAudiencia.iterator();
        while (iter.hasNext()) {
            Rostro rostroCandidato = iter.next();
            if (rostroCandidato.getDuracionMilis() > Cons.TOLERANCIA_ROSTRO_NUEVO) {
                audiencia++;
                rostroCandidato.setFechaDesde(new Date());
                rostroCandidato.setFechaHasta(new Date());
                rostroCandidato.setId(audiencia);
                rostrosAudiencia.add(rostroCandidato);
                iter.remove();
                if (listener != null) {
                    listener.onNuevoAudiente(rostroCandidato);
                }
            } else if (ahora.getTime() - rostroCandidato.getFechaHasta().getTime() >= Cons.TOLERANCIA_ROSTRO_PERDIDO) {
                iter.remove();
            }
        }

        iter = rostrosAudiencia.iterator();
        while (iter.hasNext()) {
            Rostro rostroAudiencia = iter.next();
            if (ahora.getTime() - rostroAudiencia.getFechaHasta().getTime() >= Cons.TOLERANCIA_ROSTRO_PERDIDO) {
                iter.remove();
            }
        }
    }

    private void procesarManos(MatOfRect matManosDetectados, List<Mano> manosAudiencia, List<Mano> manosCandidatosAudiencia) {
        List<Mano> manosDetectados = new ArrayList();

        for (Rect rect : matManosDetectados.toArray()) {
            Mano mano = new Mano();
            mano.setCentroX(rect.x + rect.width / 2);
            mano.setCentroY(rect.y + rect.height / 2);
            mano.setAlto(rect.height);
            mano.setAncho(rect.width);
            manosDetectados.add(mano);
        }

        for (Mano manoAudiencia : manosAudiencia) {
            manoAudiencia.setMatcheado(false);
        }
        for (Mano manoCandidato : manosCandidatosAudiencia) {
            manoCandidato.setMatcheado(false);
        }

        Iterator<Mano> iter = manosDetectados.iterator();
        while (iter.hasNext()) {
            Mano manoDetectado = iter.next();
            for (Mano manoAudiencia : manosAudiencia) {
                if (!manoAudiencia.isMatcheado()) {
                    if (manoAudiencia.isManoAproximado(manoDetectado)) {
                        manoAudiencia.setAncho(manoDetectado.getAncho());
                        manoAudiencia.setAlto(manoDetectado.getAlto());
                        manoAudiencia.setCentroX(manoDetectado.getCentroX());
                        manoAudiencia.setCentroY(manoDetectado.getCentroY());
                        manoAudiencia.setMatcheado(true);
                        manoAudiencia.setFechaHasta(new Date());

                        manoAudiencia.actualizarDireccion();

                        manoDetectado.setMatcheado(true);
                        break;
                    }
                }
            }

            if (!manoDetectado.isMatcheado()) {
                for (Mano manoCandidato : manosCandidatosAudiencia) {
                    if (!manoCandidato.isMatcheado()) {
                        if (manoCandidato.isManoAproximado(manoDetectado)) {
                            manoCandidato.setAncho(manoDetectado.getAncho());
                            manoCandidato.setAlto(manoDetectado.getAlto());
                            manoCandidato.setCentroX(manoDetectado.getCentroX());
                            manoCandidato.setCentroY(manoDetectado.getCentroY());
                            manoCandidato.setMatcheado(true);
                            manoCandidato.setFechaHasta(new Date());

                            manoCandidato.actualizarDireccion();

                            manoDetectado.setMatcheado(true);
                            break;
                        }
                    }
                }
            }
            if (manoDetectado.isMatcheado()) {
                iter.remove();
            }
        }

        for (Mano manoDetectado : manosDetectados) {
            manoDetectado.setFechaDesde(new Date());
            manoDetectado.setFechaHasta(new Date());
            manosCandidatosAudiencia.add(manoDetectado);
        }

        Date ahora = new Date();

        iter = manosCandidatosAudiencia.iterator();
        while (iter.hasNext()) {
            Mano manoCandidato = iter.next();
            if (manoCandidato.getDuracionMilis() > Cons.TOLERANCIA_MANO_NUEVO) {
                manoCandidato.setFechaDesde(new Date());
                manoCandidato.setFechaHasta(new Date());
                manosAudiencia.add(manoCandidato);
                iter.remove();
            } else if (ahora.getTime() - manoCandidato.getFechaHasta().getTime() >= Cons.TOLERANCIA_MANO_PERDIDO) {
                iter.remove();
            }
        }

        iter = manosAudiencia.iterator();
        while (iter.hasNext()) {
            Mano rostroAudiencia = iter.next();
            if (ahora.getTime() - rostroAudiencia.getFechaHasta().getTime() >= Cons.TOLERANCIA_MANO_PERDIDO) {
                iter.remove();
            }
        }
    }

    private void encuadrarRostros(Mat img, List<Rostro> listaRostros) {
        for (Rostro rostro : listaRostros) {
            Imgproc.circle(img, new Point(rostro.getCentroX(), rostro.getCentroY()), rostro.getAlto() / 2, new Scalar(0, rostro.isMatcheado() ? 255 : 0, rostro.isMatcheado() ? 0 : 255));
            Imgproc.putText(img, String.valueOf(rostro.getId()), new Point(rostro.getCentroX(), rostro.getCentroY()), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0), 2);
        }
    }

    private void encuadrarManos(Mat img, List<Mano> listaManos) {
        for (Mano mano : listaManos) {
            Imgproc.circle(img, new Point(mano.getCentroX(), mano.getCentroY()), mano.getAlto() / 2, new Scalar(255, 0, 0));
        }
        if (!listaManos.isEmpty()) {
            Mano mano = listaManos.get(0);
            Imgproc.putText(img, "AGARRAR", new Point(mano.getCentroX(), mano.getCentroY()), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 2);

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

    private void resetearMediciones() {
        reset = false;
        rostrosAudiencia.clear();
        audiencia = 0;
    }

    public int getAudienciaTotal() {
        return audiencia;
    }

    public int getAudienciaActual() {
        return rostrosAudiencia.size();
    }

    public List<Rostro> getListaRostros() {
        return rostrosAudiencia;
    }

    public void setListaRostros(List<Rostro> listaRostros) {
        this.rostrosAudiencia = listaRostros;
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

    public double getFactorEscala() {
        return factorEscala;
    }

    public void setFactorEscala(double factorEscala) {
        this.factorEscala = factorEscala;
    }

    public double getTamanoRectMin() {
        return tamanoRectMin;
    }

    public void setTamanoRectMin(double tamanoRectMin) {
        this.tamanoRectMin = tamanoRectMin;
    }

    public double getTamanoRectMax() {
        return tamanoRectMax;
    }

    public void setTamanoRectMax(double tamanoRectMax) {
        this.tamanoRectMax = tamanoRectMax;
    }

    public int getMinVecinos() {
        return minVecinos;
    }

    public void setMinVecinos(int minVecinos) {
        this.minVecinos = minVecinos;
    }

    public void setResolucion(int ancho, int alto) {
        video.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, ancho);
        video.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, alto);
    }
}
