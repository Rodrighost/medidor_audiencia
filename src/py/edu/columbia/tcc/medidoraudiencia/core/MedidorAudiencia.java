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
    private Mano.Direccion direccion;

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
        detectorManos = new CascadeClassifier(getClass().getResource(Cons.HAARCASCADE_PUNHO).getPath());
        video = new VideoCapture(0);
    }

    @Override
    public void run() {
        detectarAudiencia();
    }

    private void detectarAudiencia() {

        Mat captura = new Mat();
        MatOfRect matRostrosDetectados = new MatOfRect();
        MatOfRect matManosDetectados = new MatOfRect();

        BackgroundSubtractorMOG2 fgbg = Video.createBackgroundSubtractorMOG2(200, 15, true);

        while (!isInterrupted()) {
            if (video.grab()) {
                try {
                    if (reset) {
                        resetearMediciones();
                    }

                    video.retrieve(captura);

                    Mat fgmask = new Mat();
                    fgbg.apply(captura, fgmask);
                    
                    Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
                    Imgproc.morphologyEx(fgmask, fgmask, Imgproc.MORPH_OPEN, kernel);
                    Imgproc.morphologyEx(fgmask, fgmask, Imgproc.MORPH_CLOSE, kernel);

                    Mat capturaGris = new Mat();
                    Imgproc.cvtColor(captura, capturaGris, Imgproc.COLOR_RGB2GRAY);

                    detectorRostros.detectMultiScale(captura, matRostrosDetectados, factorEscala, minVecinos, Objdetect.CASCADE_SCALE_IMAGE, new Size(tamanoRectMin, tamanoRectMin), new Size(tamanoRectMax, tamanoRectMax));
//                    detectorManos.detectMultiScale(capturaGris, matManosDetectados, factorEscala, minVecinos * 2, Objdetect.CASCADE_DO_ROUGH_SEARCH, new Size(tamanoRectMin / 2, tamanoRectMin / 2), new Size(tamanoRectMax / 2, tamanoRectMax / 2));
                    detectorManos.detectMultiScale(fgmask, matManosDetectados, factorEscala, minVecinos, Objdetect.CASCADE_SCALE_IMAGE, new Size(tamanoRectMin, tamanoRectMin), new Size(tamanoRectMax, tamanoRectMax));

                    procesarRostros(matRostrosDetectados, rostrosAudiencia, rostrosCandidatosAudiencia);

                    Mat umbralizado = procesarManos(captura, matManosDetectados, manosAudiencia, manosCandidatosAudiencia);

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

                        if (direccion != null) {
                            switch (direccion) {
                                case ARRIBA:
                                    listener.onGestoArriba();
                                    break;
                                case ABAJO:
                                    listener.onGestoAbajo();
                                    break;
                                case IZQUIERDA:
                                    listener.onGestoIzquierda();
                                    break;
                                case DERECHA:
                                    listener.onGestoDerecha();
                                    break;
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

    private Mat procesarManos(Mat captura, MatOfRect matManosDetectados, List<Mano> manosAudiencia, List<Mano> manosCandidatosAudiencia) {
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

//        Mat capturaHSV = new Mat();
//        captura = captura.clone();
//        Imgproc.blur(captura, captura, new Size(7, 7));
//
//        Imgproc.cvtColor(captura, capturaHSV, Imgproc.COLOR_RGB2HSV);
//
//        Mat umbralizado = new Mat(capturaHSV.height(), capturaHSV.width(), CvType.CV_8UC3, new Scalar(0, 0, 0));
//
//        if (!manosAudiencia.isEmpty()) {
//            Mano mano = manosAudiencia.get(0);
//            int xDesde = mano.getCentroX() - mano.getAncho() / 3;
//            int xHasta = mano.getCentroX() + mano.getAncho() / 3;
//            int yDesde = mano.getCentroY() - mano.getAlto() / 3;
//            int yHasta = mano.getCentroY() + mano.getAlto() / 3;
//            int hMax = -1, hMin = -1, sMax = -1, sMin = -1, vMax = -1, vMin = -1, promH = 0, promS = 0, promV = 0, pixeles = 0;
//            for (int x = xDesde; x < xHasta; x++) {
//                for (int y = yDesde; y < yHasta; y++) {
//                    double data[] = capturaHSV.get(y, x);
//                    if (hMax == -1 || hMax < data[0]) {
//                        hMax = (int) data[0];
//                    }
//                    if (hMin == -1 || hMin > data[0]) {
//                        hMin = (int) data[0];
//                    }
//                    if (sMax == -1 || sMax < data[1]) {
//                        sMax = (int) data[1];
//                    }
//                    if (sMin == -1 || sMin > data[1]) {
//                        sMin = (int) data[1];
//                    }
//                    if (vMax == -1 || vMax < data[2]) {
//                        vMax = (int) data[2];
//                    }
//                    if (vMin == -1 || vMin > data[2]) {
//                        vMin = (int) data[2];
//                    }
//                    promH += (int) data[0];
//                    promS += (int) data[1];
//                    promV += (int) data[2];
//                    pixeles++;
//                }
//            }
//            promH = promH / pixeles;
//            promS = promS / pixeles;
//            promV = promV / pixeles;
//
//            double scaleY = mano.isMatcheado() ? 2 : 0.5;
//            double scaleX = mano.isMatcheado() ? 2 : 0.9;
//
//            xDesde = mano.getCentroX() - (int) (mano.getAncho() / scaleX);
//            xHasta = mano.getCentroX() + (int) (mano.getAncho() / scaleX);
//            yDesde = mano.getCentroY() - (int) (mano.getAlto() / scaleY);
//            yHasta = mano.getCentroY() + (int) (mano.getAlto() / 2);
//
//            if (xDesde < 0) {
//                xDesde = 0;
//            }
//            if (xHasta > capturaHSV.width()) {
//                xHasta = capturaHSV.width();
//            }
//            if (yDesde < 0) {
//                yDesde = 0;
//            }
//            if (yHasta > capturaHSV.height()) {
//                yHasta = capturaHSV.height();
//            }
//
//            capturaHSV.submat(yDesde, yHasta, xDesde, xHasta).copyTo(umbralizado.submat(yDesde, yHasta, xDesde, xHasta));
//
////            if (mano.isMatcheado()) {
////                Core.inRange(umbralizado, new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax), umbralizado);
////            } else {
////                Core.inRange(umbralizado, new Scalar(promH - 15, sMin,vMin), new Scalar(promH + 15, sMax, vMax), umbralizado);
////                Core.inRange(umbralizado, new Scalar(promH - 20, promS - 20, promV - 20), new Scalar(promH + 20, promS + 20, promV + 20), umbralizado);
//            Core.inRange(umbralizado, new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax), umbralizado);
//            int kernelSize = (xHasta - xDesde) / 100 + 1;
//            Mat kernel = new Mat(new Size(kernelSize, kernelSize), CvType.CV_8UC1, new Scalar(255));
//            Imgproc.morphologyEx(umbralizado, umbralizado, Imgproc.MORPH_OPEN, kernel);
////                Imgproc.morphologyEx(umbralizado, umbralizado, Imgproc.MORPH_CLOSE, kernel);
////                Imgproc.dilate(umbralizado, umbralizado, kernel);
////                Imgproc.dilate(umbralizado, umbralizado, kernel);
////                Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize *  4, kernelSize * 4));
////                Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize *  2, kernelSize * 2));
////                Imgproc.erode(umbralizado, umbralizado, erodeElement);
////                Imgproc.dilate(umbralizado, umbralizado, dilateElement);
//
//            List<MatOfPoint> contornos = new ArrayList();
//            Mat hierarchy = new Mat();
//
//            Imgproc.findContours(umbralizado, contornos, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
////                Imgproc.cvtColor(umbralizado, umbralizado, Imgproc.COLOR_GRAY2RGB);
//            umbralizado = new Mat(capturaHSV.height(), capturaHSV.width(), CvType.CV_8UC3, new Scalar(0, 0, 0));
//
//            if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
//                for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//                    Imgproc.drawContours(umbralizado, contornos, idx, new Scalar(255, 255, 255), 2);
//                }
//            }
//
////            }
//        }
//        return umbralizado;
        return captura;
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
