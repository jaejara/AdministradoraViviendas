package cl.duoc.viviendas.persistence;

import cl.duoc.viviendas.model.Vivienda;

import java.util.List;

/** Contrato de acceso a datos para un tipo de vivienda. */
public interface ViviendaDAO<T extends Vivienda> {
    /** Lee todas las viviendas almacenadas (lista vacia si no hay archivo). */
    List<T> leerTodos() throws PersistenciaException;

    /** Reemplaza el contenido almacenado por la lista recibida. */
    void guardarTodos(List<T> viviendas) throws PersistenciaException;
}
