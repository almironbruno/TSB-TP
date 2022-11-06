package tsb.TSB;

import java.util.ArrayList;

public class Genero {

    public String nombre;
    private int cantidadSeries=0;
    private ArrayList<String> series=new ArrayList<String>();
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
