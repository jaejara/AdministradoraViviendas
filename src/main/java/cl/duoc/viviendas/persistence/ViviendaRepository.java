package cl.duoc.viviendas.persistence;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Repository: mantiene la coleccion observable en memoria y delega
 * la lectura/escritura a los DAO, manteniendo ambos sincronizados.
 */
public class ViviendaRepository {
    private final ViviendaDAO<Departamento> daoDepartamentos;
    private final ViviendaDAO<Casa> daoCasas;
    private final ObservableList<Vivienda> viviendas = FXCollections.observableArrayList();

    public ViviendaRepository(ViviendaDAO<Departamento> daoDepartamentos, ViviendaDAO<Casa> daoCasas) {
        this.daoDepartamentos = daoDepartamentos;
        this.daoCasas = daoCasas;
    }

    public ObservableList<Vivienda> getViviendas() {
        return viviendas;
    }

    /** Carga la informacion de ambos archivos en la coleccion en memoria. */
    public void cargar() throws PersistenciaException {
        List<Departamento> departamentos = daoDepartamentos.leerTodos();
        List<Casa> casas = daoCasas.leerTodos();
        viviendas.setAll(departamentos);
        viviendas.addAll(casas);
    }

    /** Agrega una vivienda y guarda; si falla el guardado, revierte el cambio en memoria. */
    public void agregar(Vivienda vivienda) throws PersistenciaException {
        viviendas.add(vivienda);
        try {
            guardar();
        } catch (PersistenciaException e) {
            viviendas.remove(vivienda);
            throw e;
        }
    }

    /** Elimina una vivienda y guarda; si falla el guardado, revierte el cambio en memoria. */
    public void eliminar(Vivienda vivienda) throws PersistenciaException {
        int indice = viviendas.indexOf(vivienda);
        if (indice < 0) {
            return;
        }
        viviendas.remove(indice);
        try {
            guardar();
        } catch (PersistenciaException e) {
            viviendas.add(indice, vivienda);
            throw e;
        }
    }

    private void guardar() throws PersistenciaException {
        daoDepartamentos.guardarTodos(viviendas.stream()
                .filter(v -> v instanceof Departamento).map(v -> (Departamento) v).toList());
        daoCasas.guardarTodos(viviendas.stream()
                .filter(v -> v instanceof Casa).map(v -> (Casa) v).toList());
    }
}
