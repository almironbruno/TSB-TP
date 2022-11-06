package tsb.TSB;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

public class Controller {

    private Hashtable<String,Genero> tabla;
   @FXML
   private ChoiceBox choiceMostrar;
    @FXML
    private ChoiceBox choiceGenero;

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

        //welcomeText.setText("Welcome to JavaFX Application!"
        String opcion = (String) choiceMostrar.getValue();
        if(opcion=="Cantidad de Series")
        {
            System.out.println("op1");

        }
        if(opcion=="Detalle de Series")
        {
            System.out.println("op2");

        }
        if(opcion=="Cantidad de Series X Puntuacion")
        {
            System.out.println("op3");

        }
    }

    private void cargarDatos()
    {

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

}