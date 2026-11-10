package cl.duoc.viviendas.persistence;

import cl.duoc.viviendas.model.Vivienda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Implementacion del DAO que guarda las viviendas en un archivo JSON usando Gson. */
public class JsonViviendaDAO<T extends Vivienda> implements ViviendaDAO<T> {
    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonViviendaDAO(Path archivo, Class<T> clase) {
        this.archivo = archivo;
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
    }

    @Override
    public List<T> leerTodos() throws PersistenciaException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(archivo, StandardCharsets.UTF_8);
            if (json.isBlank()) {
                return new ArrayList<>();
            }
            List<T> leidas = gson.fromJson(json, tipoLista);
            return leidas == null ? new ArrayList<>() : new ArrayList<>(leidas);
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + archivo.getFileName() + ".", e);
        }
    }

    @Override
    public void guardarTodos(List<T> viviendas) throws PersistenciaException {
        try {
            Files.writeString(archivo, gson.toJson(viviendas, tipoLista), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el archivo " + archivo.getFileName() + ".", e);
        }
    }
}
