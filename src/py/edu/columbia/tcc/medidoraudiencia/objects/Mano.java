package py.edu.columbia.tcc.medidoraudiencia.objects;

import java.util.Date;
import py.edu.columbia.tcc.medidoraudiencia.utils.Cons;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class Mano {

    private int centroX;
    private int centroY;
    private int ancho;
    private int alto;
    private int centroMovX;
    private int centroMovY;
    private boolean macheado;
    private Date fechaDesde;
    private Date fechaHasta;
    private Gesto direccion;

    public static enum Gesto {
        IZQUIERDA, DERECHA, ARRIBA, ABAJO, NINGUNO, AGARRAR, SOLTAR
    }

    public Mano() {
        this.macheado = false;
        direccion = Gesto.NINGUNO;
        centroMovX = -1;
    }

    public int getCentroX() {
        return centroX;
    }

    public void setCentroX(int centroX) {
        this.centroX = centroX;
    }

    public int getCentroY() {
        return centroY;
    }

    public void setCentroY(int centroY) {
        this.centroY = centroY;
    }

    public int getAncho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public int getAlto() {
        return alto;
    }

    public void setAlto(int alto) {
        this.alto = alto;
    }

    public boolean isMatcheado() {
        return macheado;
    }

    public void setMatcheado(boolean macheado) {
        this.macheado = macheado;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public int getDuracion() {
        return (int) (fechaHasta.getTime() - fechaDesde.getTime()) / 1000;
    }

    public int getDuracionMilis() {
        return (int) (fechaHasta.getTime() - fechaDesde.getTime());
    }

    public Gesto getDireccion() {
        return direccion;
    }

    public void setDireccion(Gesto direccion) {
        this.direccion = direccion;
    }

    public void actualizarDireccion() {
        if (centroMovX == -1) {
            centroMovX = centroX;
            centroMovY = centroY;
        }

        direccion = Gesto.NINGUNO;

        int distanciaX = centroMovX - centroX;
        int distanciaY = centroMovY - centroY;

        if (Math.abs(distanciaX) > ancho * Cons.TOLERANCIA_MOVIMIENTO_HORIZONTAL) {
            centroMovX = centroX;
            if (distanciaX < 0) {
                direccion = Gesto.IZQUIERDA;
            } else {
                direccion = Gesto.DERECHA;
            }
        }

        if (Math.abs(distanciaY) > alto * Cons.TOLERANCIA_MOVIMIENTO_VERTICAL) {
            centroMovY = centroY;
            if (distanciaY < 0) {
                direccion = Gesto.ABAJO;
            } else {
                direccion = Gesto.ARRIBA;
            }
        }

    }

    public boolean isManoAproximado(Mano mano) {
        boolean isMano = false;
        int distanciaX = Math.abs(mano.getCentroX() - centroX);
        int distanciaY = Math.abs(mano.getCentroY() - centroY);
        if (distanciaX < ancho * Cons.TOLERANCIA_DESPLAZAMIENTO_MANO && distanciaY < alto * Cons.TOLERANCIA_DESPLAZAMIENTO_MANO) {
            isMano = true;
        }
        return isMano;
    }
}
