package py.edu.columbia.tcc.medidoraudiencia.utils;

/**
 *
 * @author Rodrigo Rodriguez
 */
public class Cons {

    public static final String HAARCASCADE_FRONTALFACE_ALT = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/frontalface_alt.xml";
    public static final String HAARCASCADE_FRONTALFACE_ALT2 = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/frontalface_alt2.xml";
//    public static final String HAARCASCADE_PUNHO = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/punho.xml";
    public static final String HAARCASCADE_PUNHO = "/py/edu/columbia/tcc/medidoraudiencia/haarcascades/palm.xml";
    
    public static final String LIBRERIA_OPENCV = "/py/edu/columbia/tcc/medidoraudiencia/libs/libopencv_java320.so";
    
    public static final float TOLERANCIA_DESPLAZAMIENTO = 0.4f; //40%
    public static final float TOLERANCIA_MOVIMIENTO_HORIZONTAL = 0.8f;
    public static final float TOLERANCIA_MOVIMIENTO_VERTICAL = 0.5f;
    
    public static final int TOLERANCIA_ROSTRO_PERDIDO = 5000; //milisegundos
    public static final int TOLERANCIA_ROSTRO_NUEVO = 2000; //milisegundos

    public static final int TOLERANCIA_MANO_PERDIDO = 100; //milisegundos
    public static final int TOLERANCIA_MANO_NUEVO = 100; //milisegundos

    public static final double FACTOR_ESCALA = 1.1;
    public static final double TAMANHO_REC_MIN = 50;
    public static final double TAMANHO_REC_MAX = 200;
    public static final int MIN_VECINOS = 5;
}
