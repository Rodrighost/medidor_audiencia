package py.edu.columbia.tcc.medidoraudiencia.core;

import java.io.ByteArrayInputStream;
import py.edu.columbia.tcc.medidoraudiencia.objects.Rostro;

/**
 *
 * @author Rodrigo Rodriguez
 */
public interface MedidorAudienciaListener {
    public void onGestoIzquierda();
    public void onGestoDerecha();
    public void onGestoArriba();
    public void onGestoAbajo();
    public void onGestoAgarrar();
    public void onGestoSoltar();
    public void onNuevaImagen(ByteArrayInputStream bais);
    public void onNuevoAudiente(Rostro rostro);
}
