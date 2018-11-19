package py.edu.columbia.tcc.medidoraudiencia.objects;

import java.util.Date;
import py.edu.columbia.tcc.medidoraudiencia.utils.Cons;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class Rostro {

    private int id;

    private int centroX;
    private int centroY;
    private int ancho;
    private int alto;
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean macheado;

    public Rostro() {
        this.macheado = false;
        id = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCentroX() {
        return centroX;
    }

    public void setCentroX(int ejeX) {
        this.centroX = ejeX;
    }

    public int getCentroY() {
        return centroY;
    }

    public void setCentroY(int ejeY) {
        this.centroY = ejeY;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
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

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    /**
     * Retorna el tiempo que el rostro estuvo presente
     *
     * @return duración en segundos
     */
    public int getDuracion() {
        return (int) (fechaHasta.getTime() - fechaDesde.getTime()) / 1000;
    }

    public int getDuracionMilis() {
        return (int)(fechaHasta.getTime() - fechaDesde.getTime());
    }

    public boolean isMatcheado() {
        return macheado;
    }

    public void setMatcheado(boolean macheado) {
        this.macheado = macheado;
    }

    public boolean isRostroAproximado(Rostro rostro) {
        boolean esRostro = false;
        int distanciaX = Math.abs(rostro.getCentroX() - centroX);
        int distanciaY = Math.abs(rostro.getCentroY() - centroY);
        if (distanciaX < ancho * Cons.TOLERANCIA_DESPLAZAMIENTO && distanciaY < alto * Cons.TOLERANCIA_DESPLAZAMIENTO) {
            esRostro = true;
        }
        return esRostro;
    }

    @Override
    public String toString() {
        return "Rostro{" + "id=" + id + '}';
    }
}
