package cl.duoc.viviendas.persistence;

/** Error al leer o guardar los archivos de datos. */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
