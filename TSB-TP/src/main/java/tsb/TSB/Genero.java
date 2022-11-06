package tsb.TSB;

import java.util.ArrayList;

public class Genero {

    private String nombre;
    private int cantidadSeries;
    private ArrayList<Object> series;
    private int[] cantidadXPuntuacion=new int[10];

    public Genero(String nombre) {
        this.nombre = nombre;
    }
    public void sumarCantidad() {
        this.cantidadSeries += 1;
    }
    public void agregarNombreSerie(String nombre){
        this.series.add(nombre);
    }

    public void sumarPuntuacion(int puntiacion){
        this.cantidadXPuntuacion[puntiacion-1] += 1;
    }
}
