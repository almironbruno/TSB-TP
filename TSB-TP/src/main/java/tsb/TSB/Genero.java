package tsb.TSB;

import java.util.ArrayList;

public class Genero {

    public String nombre;
    public int cantidadSeries=0;
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

    public String mostrarPuntuaciones()
    {
        StringBuilder str = new StringBuilder();
        str.append("Cantidades por cada puntuacion:\n");
        for(int i =0;i<cantidadXPuntuacion.length;i++)
        {
            str.append(" Puntuacion "+i+": ");
            str.append(cantidadXPuntuacion[i]);
            str.append(" series\n");
        }
        return str.toString();

    }
    public ArrayList<String> mostrarSeries()
    {
        return this.series;

    }

}
