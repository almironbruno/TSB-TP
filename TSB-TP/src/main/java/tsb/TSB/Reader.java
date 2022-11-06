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
        arch.useDelimiter("[\\n\\r\\,;]+");
        String [] listaGeneros = new String[3];
        arch.nextLine();
        int c=0;
        while (arch.hasNextLine()) {
            String linea = arch.nextLine();
            String[] datos = linea.split("\\,");
            String titulo = datos[0];System.out.println(titulo);
            String anio = datos[1];System.out.println(anio);
            //arch.useDelimiter("[\\n\\r\\,\\s,;]+");
            String certificacion = datos[2];System.out.println(certificacion);
            //arch.useDelimiter("[\\n\\r\\,;]+");
            String duracion = datos[3];System.out.println(duracion);
            String generos = datos[4];System.out.println(generos);
            listaGeneros = generos.split("[\\|;]+");
            float IMDB_Rating =Float.parseFloat(datos[5]);System.out.println(IMDB_Rating);
            String argumento = datos[6];System.out.println(argumento);
            String Star1 = datos[7];System.out.println(Star1);
            String Star2 = datos[8];System.out.println(Star1);
            String Star3 = datos[9];System.out.println(Star3);
            String Star4 = datos[10];System.out.println(Star4);
            String botos = datos[11];System.out.println(botos);
            for(int i = 0; i<listaGeneros.length; i++){
                if(!tabla.containsKey(listaGeneros[i])){
                    Genero genero = new Genero(listaGeneros[i]);
                    genero.agregarNombreSerie(titulo);
                    genero.sumarCantidad();
                    genero.sumarPuntuacion((int)IMDB_Rating);
                    tabla.put(genero.nombre,genero);
                }
                else {
                    Genero genero = tabla.get(listaGeneros[i]);
                    genero.sumarPuntuacion((int)IMDB_Rating);
                    genero.sumarCantidad();
                    genero.agregarNombreSerie(titulo);

                }
            }
        }
        return tabla;
    }
}
