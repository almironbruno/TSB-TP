package tsb.TSB;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;
public class Reader {
    public Reader(){
    }

    public Hashtable<String,Genero> LlenarHash(String nombre) throws FileNotFoundException {
        Scanner arch = new Scanner(new File("series_data_clean.csv"));
        Hashtable<String,Genero> tabla = new Hashtable<String,Genero>();
        arch.useDelimiter("[\\n\\r\\|;]+");
        String [] listaGeneros = new String[3];
        while (arch.hasNext()) {
            String titulo = arch.next();
            String anio = arch.next();
            String certificacion = arch.next();
            String duracion = arch.next();
            listaGeneros[0] = arch.next();
            listaGeneros[1] = arch.next();
            listaGeneros[2] = arch.next();
            int IMDB_Rating = arch.nextInt();
            String argumento = arch.next();
            String Star1 = arch.next();
            String Star2 = arch.next();
            String Star3 = arch.next();
            String Star4 = arch.next();
            int botos = arch.nextInt();
            for(int i = 0; i<listaGeneros.length; i++){
                if(tabla.containsKey(listaGeneros[i])){
                    Genero genero = new Genero(listaGeneros[i]);
                    genero.agregarNombreSerie(titulo);
                    genero.sumarCantidad();
                    genero.sumarPuntuacion(IMDB_Rating);
                    tabla.put(nombre,genero);
                }
                else {
                    Genero genero = tabla.get(listaGeneros[i]);
                    genero.sumarPuntuacion(IMDB_Rating);
                    genero.sumarCantidad();
                    genero.agregarNombreSerie(titulo);
                }
            }
        }
        return tabla;
    }
}
