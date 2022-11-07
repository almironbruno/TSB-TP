package tsb.TSB;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import java.io.FileNotFoundException;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Controller {

    private TSBHashtableDA<String,Genero> tabla;
    @FXML
    private ChoiceBox choiceMostrar;
    @FXML
    private ChoiceBox choiceGenero;
    @FXML
    private HBox HboxMostrar;
    public void initialize() {
        choiceMostrar.getItems().add("Cantidad de Series");
        choiceMostrar.getItems().add("Detalle de Series");
        choiceMostrar.getItems().add("Cantidad de Series X Puntuacion");
        cargarDatos();
        Set<String> c =tabla.keySet();
        for (String unaSerie:c) {
            choiceGenero.getItems().add(unaSerie);
        }

    }
    @FXML
    protected void onBuscarButtonClick() {

        //toma la seleccion del usuario
        String generoElegido = (String) choiceGenero.getValue();
        String opcion = (String) choiceMostrar.getValue();

        //validacion
        if (opcion==null || generoElegido == null) return;

        //opciones
        switch (opcion)
        {
            case "Cantidad de Series":

                puntoCantSeries(generoElegido);
                break;

            case "Detalle de Series":

                puntoMostrarSeries(generoElegido);
                break;

            case "Cantidad de Series X Puntuacion":

                puntoCantxPuntuacion(generoElegido);

        }


    }

    private void cargarDatos()
    {
        //carga los datos de la tabla
        HashMap<String,Genero> tabla1 =new HashMap<String,Genero>();
        Reader reader = new Reader();
        try
        {
            this.tabla=reader.LlenarHash("hola");
        }
        catch (FileNotFoundException f)
        {
            System.out.println(f.getMessage());
        }

    }

    private void puntoCantSeries(String generoElegido)
    {
        //busca la cantidad de la tabla
        int cantidad = tabla.get(generoElegido).cantidadSeries;

        //crea las partes visuales
        TextField txtCantidad = new TextField(Integer.toString(cantidad));
        Label lbl_mensaje = new Label("Cantidad de Series:");
        txtCantidad.setEditable(false);
        HboxMostrar.getChildren().clear();
        HboxMostrar.getChildren().add(lbl_mensaje);
        HboxMostrar.getChildren().add(txtCantidad);

    }

    private void puntoMostrarSeries(String generoElegido)
    {
        //busca las series de la tabla
        ArrayList<String> series = this.tabla.get(generoElegido).mostrarSeries();

        //crea la parte visual
        crearTabla(series);
    }
    private void puntoCantxPuntuacion(String generoElegido)
    {
        //busca la puntuaciones de la tabla
        String puntuaciones =tabla.get(generoElegido).mostrarPuntuaciones();

        //crea las partes visuales
        TextArea txtCant = new TextArea(puntuaciones);
        txtCant.setEditable(false);
        HboxMostrar.getChildren().clear();
        HboxMostrar.getChildren().add(txtCant);

    }
    private  void crearTabla(ArrayList<String> series)
    {
        //Crea la tableView

        TableView tablaSeries = new TableView();

        //crea la columna Series
        TableColumn<Serie, String> columnaSeries =
                new TableColumn<Serie,String>("Series");
        columnaSeries.setCellValueFactory(
                new PropertyValueFactory<>("titulo"));

        //añade la coluna a la tabla
        tablaSeries.getColumns().add(columnaSeries);

        //añade las series a la tabla
        ObservableList<Serie> listaSeries = FXCollections.observableArrayList();
        for(String unaSerie:series)
        {
            listaSeries.add( new Serie(unaSerie));
        }

        //añade a la scene
        tablaSeries.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSeries.setItems(listaSeries);
        HboxMostrar.getChildren().clear();
        HboxMostrar.getChildren().add(tablaSeries);
    }

}