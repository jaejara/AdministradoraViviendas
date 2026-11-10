package cl.duoc.viviendas.controller;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.persistence.JsonViviendaDAO;
import cl.duoc.viviendas.persistence.PersistenciaException;
import cl.duoc.viviendas.persistence.ViviendaRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Controlador de la ventana principal. Maneja los eventos de la vista y delega
 * la logica de negocio al modelo y el acceso a datos al repositorio.
 */
public class PrincipalController {
    private static final String DEPARTAMENTO = "Departamento";
    private static final String CASA = "Casa";

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtSuperficie;
    @FXML private TextField txtHabitaciones;
    @FXML private TextField txtPiso;
    @FXML private CheckBox chkGastoComun;
    @FXML private CheckBox chkEstacionamiento;
    @FXML private CheckBox chkPatio;
    @FXML private TitledPane panelDepartamento;
    @FXML private TitledPane panelCasa;
    @FXML private TextField txtBusqueda;
    @FXML private TableView<Vivienda> tablaViviendas;
    @FXML private TableColumn<Vivienda, String> colTipo;
    @FXML private TableColumn<Vivienda, String> colCodigo;
    @FXML private TableColumn<Vivienda, Double> colSuperficie;
    @FXML private TableColumn<Vivienda, Integer> colHabitaciones;
    @FXML private TableColumn<Vivienda, String> colCosto;
    @FXML private Label lblMensaje;

    private ViviendaRepository repositorio;
    private FilteredList<Vivienda> viviendasFiltradas;

    /** Inicializa los componentes visuales y registra los manejadores de eventos. */
    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(DEPARTAMENTO, CASA);
        cmbTipo.valueProperty().addListener((obs, anterior, nuevo) -> actualizarPanelesPorTipo());
        actualizarPanelesPorTipo();

        repositorio = new ViviendaRepository(
                new JsonViviendaDAO<>(Path.of("departamentos.json"), Departamento.class),
                new JsonViviendaDAO<>(Path.of("casas.json"), Casa.class));

        configurarTabla();
        configurarBusqueda();
        cargarDatosIniciales();
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTipo()));
        colCodigo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCodigo()));
        colSuperficie.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSuperficie()));
        colHabitaciones.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getHabitaciones()));
        colCosto.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                formatearCosto(c.getValue().calcularCostoArriendo())));
        colSuperficie.setStyle("-fx-alignment: CENTER-RIGHT;");
        colHabitaciones.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCosto.setStyle("-fx-alignment: CENTER-RIGHT;");
        // La tabla de costos es texto: se ordena por el valor numerico real.
        colCosto.setComparator((a, b) -> Long.compare(parsearCosto(a), parsearCosto(b)));
    }

    /** Filtrado dinamico: la coleccion original no se modifica, solo se filtra una vista de ella. */
    private void configurarBusqueda() {
        viviendasFiltradas = new FilteredList<>(repositorio.getViviendas(), v -> true);
        txtBusqueda.textProperty().addListener((obs, anterior, texto) -> {
            String criterio = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
            viviendasFiltradas.setPredicate(v -> criterio.isEmpty()
                    || v.getCodigo().toLowerCase(Locale.ROOT).contains(criterio)
                    || v.getTipo().toLowerCase(Locale.ROOT).contains(criterio));
        });
        SortedList<Vivienda> ordenadas = new SortedList<>(viviendasFiltradas);
        ordenadas.comparatorProperty().bind(tablaViviendas.comparatorProperty());
        tablaViviendas.setItems(ordenadas);
    }

    private void cargarDatosIniciales() {
        try {
            repositorio.cargar();
        } catch (PersistenciaException e) {
            mostrarError("Error al leer los archivos: " + e.getMessage());
        }
    }

    /** Solo quedan habilitados los datos especificos del tipo elegido. */
    private void actualizarPanelesPorTipo() {
        String tipo = cmbTipo.getValue();
        panelDepartamento.setDisable(!DEPARTAMENTO.equals(tipo));
        panelCasa.setDisable(!CASA.equals(tipo));
    }

    @FXML
    private void registrar() {
        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            mostrarError("Tipo: debe seleccionar Departamento o Casa.");
            return;
        }

        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            mostrarError("Código: no puede estar vacío.");
            return;
        }

        Double superficie = leerSuperficie();
        if (superficie == null) {
            return;
        }

        Integer habitaciones = leerEnteroPositivo(txtHabitaciones, "Habitaciones");
        if (habitaciones == null) {
            return;
        }

        try {
            Vivienda vivienda;
            if (DEPARTAMENTO.equals(tipo)) {
                Integer piso = leerEnteroPositivo(txtPiso, "Piso");
                if (piso == null) {
                    return;
                }
                Departamento departamento = new Departamento(codigo, superficie, habitaciones,
                        piso, chkGastoComun.isSelected());
                if (chkEstacionamiento.isSelected()) {
                    departamento.asignarEstacionamiento();
                }
                vivienda = departamento;
            } else {
                vivienda = new Casa(codigo, superficie, habitaciones, chkPatio.isSelected());
            }

            repositorio.agregar(vivienda);
            limpiarFormulario();
            mostrarExito(codigo + " (" + tipo + ") registrada correctamente.");
        } catch (IllegalArgumentException e) {
            // Respaldo: el modelo rechazo un dato que el formulario no detecto.
            mostrarError(e.getMessage());
        } catch (PersistenciaException e) {
            mostrarError("Error al guardar los archivos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Vivienda seleccionada = tablaViviendas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una vivienda de la tabla para eliminarla.");
            return;
        }
        try {
            repositorio.eliminar(seleccionada);
            mostrarExito(seleccionada.getCodigo() + " (" + seleccionada.getTipo() + ") eliminada correctamente.");
        } catch (PersistenciaException e) {
            mostrarError("Error al guardar los archivos: " + e.getMessage());
        }
    }

    // ---------- Lectura y validacion de campos ----------

    private Double leerSuperficie() {
        String texto = txtSuperficie.getText() == null ? "" : txtSuperficie.getText().trim().replace(',', '.');
        double valor;
        try {
            valor = Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            mostrarError("Superficie: debe ser un número (se aceptan decimales, ej. 65.5).");
            return null;
        }
        if (!Double.isFinite(valor) || valor < 20 || valor > 500) {
            mostrarError("Superficie: debe estar entre 20 y 500 m².");
            return null;
        }
        return valor;
    }

    private Integer leerEnteroPositivo(TextField campo, String nombre) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        int valor;
        try {
            valor = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            mostrarError(nombre + ": debe ser un número entero mayor que cero.");
            return null;
        }
        if (valor <= 0) {
            mostrarError(nombre + ": debe ser mayor que cero.");
            return null;
        }
        return valor;
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtSuperficie.clear();
        txtHabitaciones.clear();
        txtPiso.clear();
        chkGastoComun.setSelected(false);
        chkEstacionamiento.setSelected(false);
        chkPatio.setSelected(false);
    }

    // ---------- Mensajes en la ventana ----------

    private void mostrarExito(String mensaje) {
        mostrarMensaje(mensaje, "mensaje-ok");
    }

    private void mostrarError(String mensaje) {
        mostrarMensaje(mensaje, "mensaje-error");
    }

    private void mostrarMensaje(String mensaje, String estilo) {
        lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error", "mensaje-info");
        lblMensaje.getStyleClass().add(estilo);
        lblMensaje.setText(mensaje);
    }

    // ---------- Formato de costos ($207.000) ----------

    private static String formatearCosto(double costo) {
        return "$" + String.format(Locale.GERMANY, "%,d", Math.round(costo));
    }

    private static long parsearCosto(String texto) {
        return Long.parseLong(texto.replace("$", "").replace(".", ""));
    }
}
