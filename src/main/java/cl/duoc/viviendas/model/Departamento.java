package cl.duoc.viviendas.model;

/**
 * Departamento gestionado por la administradora. Participa en el servicio de estacionamiento.
 * Costo de arriendo: base $180.000; si el gasto común no está al día, recargo del 15%.
 */
public class Departamento extends Vivienda implements ConEstacionamiento {

    private static final double COSTO_BASE = 180000;
    private static final double RECARGO_GASTO_COMUN_PENDIENTE = 0.15;

    private int piso;
    private boolean gastoComunAlDia;
    private boolean estacionamientoAsignado;

    /**
     * Constructor sin parámetros, requerido para reconstruir objetos.
     */
    public Departamento() {
    }

    public Departamento(String codigo, double superficie, int habitaciones, int piso, boolean gastoComunAlDia) {
        super(codigo, superficie, habitaciones);
        setPiso(piso);
        setGastoComunAlDia(gastoComunAlDia);
        this.estacionamientoAsignado = false;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

    public boolean isGastoComunAlDia() {
        return gastoComunAlDia;
    }

    public void setGastoComunAlDia(boolean gastoComunAlDia) {
        this.gastoComunAlDia = gastoComunAlDia;
    }

    @Override
    public boolean tieneEstacionamientoAsignado() {
        return estacionamientoAsignado;
    }

    @Override
    public void asignarEstacionamiento() {
        this.estacionamientoAsignado = true;
    }

    @Override
    public double calcularCostoArriendo() {
        if (gastoComunAlDia) {
            return COSTO_BASE;
        }
        return COSTO_BASE + COSTO_BASE * RECARGO_GASTO_COMUN_PENDIENTE;
    }

    @Override
    public String getTipo() {
        return "Departamento";
    }
}
