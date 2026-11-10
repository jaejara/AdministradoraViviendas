package cl.duoc.viviendas.model;

/**
 * Contrato que cumplen las viviendas que participan en el servicio de
 * estacionamiento de la administradora.
 */
public interface ConEstacionamiento {

    /**
     * Indica si la vivienda tiene un estacionamiento asignado.
     *
     * @return true si tiene estacionamiento asignado
     */
    boolean tieneEstacionamientoAsignado();

    /**
     * Asigna un estacionamiento a la vivienda.
     */
    void asignarEstacionamiento();
}
