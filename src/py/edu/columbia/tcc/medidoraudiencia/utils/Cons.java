package py.edu.columbia.tcc.medidoraudiencia.utils;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class Cons {

    public static final String HAARCASCADE_FRONTALFACE_ALT = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/frontalface_alt.xml";
    public static final String HAARCASCADE_FRONTALFACE_ALT2 = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/frontalface_alt2.xml";
    public static final String HAARCASCADE_MANO = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/hand.xml";
    public static final String LIBRERIA_OPENCV = "/py/edu/columbia/tcc/medidoraudiencia/libs/libopencv_java320.so";
    
    public static final float TOLERANCIA_DESPLAZAMIENTO = 0.4f; //40%
    
    public static final float TOLERANCIA_DESPLAZAMIENTO_MANO = 1.5f; //150%
    public static final float TOLERANCIA_MOVIMIENTO_HORIZONTAL = 0.4f;
    public static final float TOLERANCIA_MOVIMIENTO_VERTICAL = 0.4f;
    public static final long TIEMPO_ENTRE_CAMBIOS = 2000; //2 segundos
    
    public static final int TOLERANCIA_ROSTRO_PERDIDO = 3000; //milisegundos
    public static final int TOLERANCIA_ROSTRO_NUEVO = 5000; //milisegundos

    public static final int TOLERANCIA_MANO_PERDIDO = 500; //milisegundos
    public static final int TOLERANCIA_MANO_NUEVO = 50; //milisegundos

    public static final double FACTOR_ESCALA = 1.1;
    public static final double TAMANHO_REC_MIN = 70;
    public static final double TAMANHO_REC_MAX = 400;
    public static final int MIN_VECINOS = 10;
}
