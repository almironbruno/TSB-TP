package tsb.TSB;

import java.io.Serializable;
import java.util.*;

/**
 * Clase para emular la funcionalidad de la clase java.util.Hashtable, pero implementada
 * en base al modelo de ResoluciÃ³n de Colisiones por Direccionamiento Abierto. Modelo para
 * aplicar de base para el desarrollo del TPU.
 *
 * @author Ing. Valerio Frittelli.
 * @version Octubre de 2019.
 * @param <K> el tipo de los objetos que serÃ¡n usados como clave en la tabla.
 * @param <V> el tipo de los objetos que serÃ¡n los valores de la tabla.
 */
public class TSBHashtableDA<K,V> implements Map<K,V>, Cloneable, Serializable
{
    //************************ Constantes (privadas o pÃºblicas).

    // estados en los que puede estar una casilla o slot de la tabla...
    public static final int OPEN = 0;
    public static final int CLOSED = 1;
    public static final int TOMBSTONE = 2;

    //************************ Atributos privados (estructurales).

    // la tabla hash: el arreglo que contiene todos los objetos...
    private Object table[];

    // el tamaÃ±o inicial de la tabla (tamaÃ±o con el que fue creada)...
    private int initial_capacity;
    
    // la cantidad de objetos que contiene la tabla...
    private int count;
    
    // el factor de carga para calcular si hace falta un rehashing...
    private float load_factor;
      
    
    //************************ Atributos privados (para gestionar las vistas).

    /*
     * (Tal cual estÃ¡n definidos en la clase java.util.Hashtable)
     * Cada uno de estos campos se inicializa para contener una instancia de la
     * vista que sea mÃ¡s apropiada, la primera vez que esa vista es requerida. 
     * Las vistas son objetos stateless (no se requiere que almacenen datos, sino
     * que sÃ³lo soportan operaciones), y por lo tanto no es necesario crear mÃ¡s 
     * de una de cada una.
     */
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;

    
    //************************ Atributos protegidos (control de iteraciÃ³n).
    
    // conteo de operaciones de cambio de tamaÃ±o (fail-fast iterator).
    protected transient int modCount;
    
    
    //************************ Constructores.

    /**
     * Crea una tabla vacÃ­Â­a, con la capacidad inicial igual a 11 y con factor 
     * de carga igual a 0.5f (que equivale a un nivel de carga del 50%).
     */    
    public TSBHashtableDA()
    {
        this(11, 0.5f);
    }
    
    /**
     * Crea una tabla vacÃ­Â­a, con la capacidad inicial indicada y con factor 
     * de carga igual a 0.5f (que equivale a un nivel de carga del 50%).
     * @param initial_capacity la capacidad inicial de la tabla.
     */    
    public TSBHashtableDA(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    /**
     * Crea una tabla vacÃ­Â­a, con la capacidad inicial indicada y con el factor 
     * de carga indicado. Si la capacidad inicial indicada por initial_capacity 
     * es menor o igual a 0, la tabla serÃ¡ creada de tamaÃ±o 11. Si el factor de
     * carga indicado es negativo, cero o mayor a 0.5, se ajustarÃ¡ a 0.5f. Si el
     * valor de initial_capacity no es primo, el tamaÃ±o se ajustarÃ¡ al primer
     * primo que sea mayor a initial_capacity.
     * @param initial_capacity la capacidad inicial de la tabla.
     * @param load_factor el factor de carga de la tabla.
     */
    public TSBHashtableDA(int initial_capacity, float load_factor)
    {
        if(load_factor <= 0 || load_factor > 0.5) { load_factor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else
        {
            if(!isPrime(initial_capacity))
            {
                initial_capacity = nextPrime(initial_capacity);
            }
        }
        
        this.table = new Object[initial_capacity];
        for(int i=0; i<table.length; i++)
        {
            table[i] = new Entry<K, V>(null, null);
        }
        
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }
    
    /**
     * Crea una tabla a partir del contenido del Map especificado.
     * @param t el Map a partir del cual se crearÃ¡Â¡ la tabla.
     */     
    public TSBHashtableDA(Map<? extends K,? extends V> t)
    {
        this(11, 0.5f);
        this.putAll(t);
    }
    
    
    //************************ ImplementaciÃ³n de mÃ©todos especificados por Map.
    
    /**
     * Retorna la cantidad de elementos contenidos en la tabla.
     * @return la cantidad de elementos de la tabla.
     */
    @Override
    public int size() 
    {
        return this.count;
    }

    /**
     * Determina si la tabla esta vaci­a (no contiene ningun elemento).
     * @return true si la tabla esta vaci­a.
     */
    @Override
    public boolean isEmpty() 
    {
        return (this.count == 0);
    }

    /**
     * Determina si la clave key estan en la tabla. 
     * @param key la clave a verificar.
     * @return true si la clave está en la tabla.
     * @throws NullPointerException si la clave es null.
     */
    @Override
    public boolean containsKey(Object key) 
    {
        if (key == null) throw  new NullPointerException("La clave es nula");
        //Busca si la key tiene un Entry asociado
        Entry<K, V> c = (Entry <K,V>)this.search_for_entry((K)key, this.h((K)key));
        //Existe el entry
        if (c!= null )
        {
            // si el entry tiene un valor asociado retorna true
            return (c.state == CLOSED);
        }
        return false;
    }

    /**
     * Determina si alguna clave de la tabla estÃ¡Â¡ asociada al objeto value que
     * entra como parámetro. Equivale a contains().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave estÃ¡Â¡ asociada efectivamente a ese value.
     */    
    @Override
    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }

    /**
     * Retorna el objeto al cual estï¿½ asociada la clave key en la tabla, o null 
     * si la tabla no contiene ningï¿½n objeto asociado a esa clave.
     * @param key la clave que serï¿½ buscada en la tabla.
     * @return el objeto asociado a la clave especificada (si existe la clave) o 
     *         null (si no existe la clave en esta tabla).
     * @throws NullPointerException si key es null.
     * @throws ClassCastException si la clase de key no es compatible con la 
     *         tabla.
     */
    @Override
    public V get(Object key) 
    {
        //Tira error si no se pasa nada
        if(key == null) throw new NullPointerException("get(): parï¿½metro null");
        //Transforma la key a un tipo valido
        K  aKey =  (K)key;
        //Busca el entry
        Entry<K, V> c = (Entry <K,V>)this.search_for_entry(aKey, this.h(aKey));
        //Si el entry existe
        if (c!=null)
        {
            //retorna el valor
            return c.value;
        }
        // El entry no existe
        return null;



    }

    /**
     * Asocia el valor (value) especificado, con la clave (key) especificada en
     * esta tabla. Si la tabla contení­a previamente un valor asociado para la 
     * clave, entonces el valor anterior es reemplazado por el nuevo (y en este 
     * caso el tamaÃ±o de la tabla no cambia). 
     * @param key la clave del objeto que se quiere agregar a la tabla.
     * @param value el objeto que se quiere agregar a la tabla.
     * @return el objeto anteriormente asociado a la clave si la clave ya 
     *         estaba asociada con alguno, o null si la clave no estaba antes 
     *         asociada a ningÃƒÂºn objeto.
     * @throws NullPointerException si key es null o value es null.
     */
    @Override
    public V put(K key, V value) 
    {
       if(key == null || value == null) throw new NullPointerException("put(): parÃ¡metro null");
       
       int ik = this.h(key);

       V old = null;
       Map.Entry<K, V> x = this.search_for_entry((K)key, ik);
       if(x != null) 
       {
           old = x.getValue();
           x.setValue(value);
       }
       else
       {
           if(this.load_level() >= this.load_factor) { this.rehash(); }
           int pos = search_for_OPEN(this.table, this.h(key));
           Map.Entry<K, V> entry = new Entry<>(key, value, CLOSED);
           table[pos] = entry;

           this.count++;
           this.modCount++;
       }
       
       return old;
    }

    /**
     * Elimina de la tabla la clave key (y su correspondiente valor asociado).  
     * El método no hace nada si la clave no está en la tabla. 
     * @param key la clave a eliminar.
     * @return El objeto al cual la clave estaba asociada, o null si la clave no
     *         estaba en la tabla.
     * @throws NullPointerException - if the key is null.
     */
    @Override
    public V remove(Object key) 
    {
       // Verifica que no venga un key nulo
        if(key == null) throw new NullPointerException("remove(): parÃ¡metro null");

        // Busca y guarda el valor hash de key
        int v_hash = this.h((K)key);
        //variable para el objeto a devolver
        V viejo = null;
        //Primero buscamos que exista una entrada con esa key
        //Luego casteamos esa entrada resultante para poder cambiar su edo
        Entry<K, V> unEntry = (Entry <K,V>)this.search_for_entry((K)key, v_hash);

        //Verifica que dicha key corresponda a un elemento en la tabla
        if (unEntry != null) {
            //Verifica que dicha key no referiencie a una tumba o una casilla abierta
            //y asigna el valor correspondiente a la marca TOMBSTONE [2]
            if (unEntry.getState() !=TOMBSTONE && unEntry.getState()!=OPEN) {
                unEntry.setState(TOMBSTONE);
                //obtenemos el valor viejo
                viejo=unEntry.value;

                this.count--;
                this.modCount--;

            }
            
        }
        return viejo;
    }     

    /**
     * Copia en esta tabla, todos los objetos contenidos en el map especificado.
     * Los nuevos objetos reemplazaran a los que ya existan en la tabla 
     * asociados a las mismas claves (si se repitiese alguna).
     * @param m el map cuyos objetos seran copiados en esta tabla. 
     * @throws NullPointerException si m es null.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) 
    {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Elimina el contenido de la tabla, de forma de dejarla vaci­a. En esta
     * implementacion ademas, el arreglo de soporte vuelve a tener el tamaño que
     * inicialmente tuvo al ser creado el objeto.
     */
    @Override
    public void clear() 
    {
        //Recorre cada Entry de la tabla
        //for (var cadaEntry: table) {
        //    Entry<K,V> unEntry = (Entry<K, V>) cadaEntry;
        //    //Cambia sus valores
        //    unEntry.key=null;
        //    unEntry.value=null;
        //    unEntry.state=OPEN;
        //}
        //Existen cero elementos en la tabla
        //count=0;
        if(load_factor <= 0 || load_factor > 0.5) { load_factor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else
        {
            if(!isPrime(initial_capacity))
            {
                initial_capacity = nextPrime(initial_capacity);
            }
        }

        this.table = new Object[initial_capacity];
        for(int i=0; i<table.length; i++)
        {
            table[i] = new Entry<K, V>(null, null);
        }
        this.count = 0;
        this.modCount = 0;

    }

    /**
     * Retorna un Set (conjunto) a modo de vista de todas las claves (key)
     * contenidas en la tabla. El conjunto estÃ¡ respaldado por la tabla, por lo 
     * que los cambios realizados en la tabla serÃ¡n reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador estÃ¡ actuando 
     * sobre el conjunto vista, el resultado de la iteraciÃ³n serÃ¡Â¡ indefinido 
     * (salvo que la modificaciÃƒÂ³n sea realizada por la operaciÃƒÂ³n remove() propia
     * del iterador, o por la operaciÃƒÂ³n setValue() realizada sobre una entrada 
     * de la tabla que haya sido retornada por el iterador). El conjunto vista 
     * provee mÃƒÂ©todos para eliminar elementos, y esos mÃƒÂ©todos a su vez 
     * eliminan el correspondiente par (key, value) de la tabla (a travÃƒÂ©s de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll() 
     * y clear()). El conjunto vista no soporta las operaciones add() y 
     * addAll() (si se las invoca, se lanzarÃƒÂ¡ una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todas las claves
     *         mapeadas en la tabla.
     */
    @Override
    public Set<K> keySet() 
    {
        if(keySet == null) 
        { 
            // keySet = Collections.synchronizedSet(new KeySet()); 
            keySet = new KeySet();
        }
        return keySet;  
    }
        
    /**
     * Retorna una Collection (colecciÃƒÂ³n) a modo de vista de todos los valores
     * (values) contenidos en la tabla. La colecciÃƒÂ³n estÃ¡ respaldada por la 
     * tabla, por lo que los cambios realizados en la tabla serÃ¡n reflejados en 
     * la colecciÃƒÂ³n, y viceversa. Si la tabla es modificada mientras un iterador 
     * estÃ¡ actuando sobre la colecciÃƒÂ³n vista, el resultado de la iteraciÃ³n serÃ¡Â¡ 
     * indefinido (salvo que la modificaciÃƒÂ³n sea realizada por la operaciÃƒÂ³n 
     * remove() propia del iterador, o por la operaciÃƒÂ³n setValue() realizada 
     * sobre una entrada de la tabla que haya sido retornada por el iterador). 
     * La colecciÃƒÂ³n vista provee mÃƒÂ©todos para eliminar elementos, y esos mÃƒÂ©todos 
     * a su vez eliminan el correspondiente par (key, value) de la tabla (a 
     * travÃƒÂ©s de las operaciones Iterator.remove(), Collection.remove(), 
     * removeAll(), removeAll(), retainAll() y clear()). La colecciÃƒÂ³n vista no 
     * soporta las operaciones add() y addAll() (si se las invoca, se lanzarÃƒÂ¡ 
     * una UnsuportedOperationException).
     * @return una colecciÃƒÂ³n (un Collection) a modo de vista de todas los 
     *         valores mapeados en la tabla.
     */
    @Override
    public Collection<V> values() 
    {
        if(values==null)
        {
            // values = Collections.synchronizedCollection(new ValueCollection());
            values = new ValueCollection();
        }
        return values;    
    }

    /**
     * Retorna un Set (conjunto) a modo de vista de todos los pares (key, value)
     * contenidos en la tabla. El conjunto estÃ¡ respaldado por la tabla, por lo 
     * que los cambios realizados en la tabla serÃ¡n reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador estÃ¡ actuando 
     * sobre el conjunto vista, el resultado de la iteraciÃ³n serÃ¡ indefinido 
     * (salvo que la modificaciÃƒÂ³n sea realizada por la operaciÃƒÂ³n remove() propia
     * del iterador, o por la operaciÃƒÂ³n setValue() realizada sobre una entrada 
     * de la tabla que haya sido retornada por el iterador). El conjunto vista 
     * provee mÃƒÂ©todos para eliminar elementos, y esos mÃƒÂ©todos a su vez 
     * eliminan el correspondiente par (key, value) de la tabla (a travÃƒÂ©s de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll() 
     * and clear()). El conjunto vista no soporta las operaciones add() y 
     * addAll() (si se las invoca, se lanzarÃƒÂ¡ una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todos los objetos 
     *         mapeados en la tabla.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() 
    {
        if(entrySet == null) 
        { 
            // entrySet = Collections.synchronizedSet(new EntrySet()); 
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    
    //************************ RedefiniciÃƒÂ³n de mÃƒÂ©todos heredados desde Object.
    
    /**
     * Retorna una copia superficial de la tabla. Las listas de desborde o 
     * buckets que conforman la tabla se clonan ellas mismas, pero no se clonan 
     * los objetos que esas listas contienen: en cada bucket de la tabla se 
     * almacenan las direcciones de los mismos objetos que contiene la original. 
     * @return una copia superficial de la tabla.
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la
     *         interface Cloneable.    
     */ 
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        //Utiliza el constructor que recibe una tabla y copia sus elementos en la nueva tabla
        return new TSBHashtableDA<K,V>(this);
    }

    /**
     * Determina si esta tabla es igual al objeto especificado.
     * @param obj el objeto a comparar con esta tabla.
     * @return true si los objetos son iguales.
     */
    @Override
    public boolean equals(Object obj) 
    {
        if(!(obj instanceof Map)) { return false; }
        
        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try 
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext()) 
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else 
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        } 
        
        catch (ClassCastException | NullPointerException e) 
        {
            return false;
        }

        return true;    
    }

    /**
     * Retorna un hash code para la tabla completa.
     * @return un hash code para la tabla.
     */
    @Override
    public int hashCode() 
    {
        //Inicia el acumulador
        int hc=0;

        //Recorrido
        for(var entry: table)
        {
            //Cast de Object a Entry<K,V>
            Entry<K,V> unEntry=(Entry<K,V>)entry;
            //Solo agrega los que estan cerrados
            if(unEntry.state==CLOSED)
            {
                hc+=unEntry.hashCode();
            }
        };

        return hc;
    }
    
    /**
     * Devuelve el contenido de la tabla en forma de String.
     * @return una cadena con el contenido completo de la tabla.
     */
    @Override
    public String toString() 
    {

        StringBuilder cad = new StringBuilder("[");
        for(int i = 0; i < this.table.length; i++)
        {

            Entry<K, V> entry = (Entry<K, V>) table[i];
            if(entry.getState()==CLOSED && entry.value!=null && entry.key !=null)
            {
                cad.append(entry.toString());
                cad.append(",");
            }
        }
        cad.append("]");
        return cad.toString();
    }
    
    
    //************************ Metodos especi­ficos de la clase.

    /**
     * Determina si alguna clave de la tabla esta asociada al objeto value que
     * entra como parametro. Equivale a containsValue().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave esta asociada efectivamente a ese value.
     */
    public boolean contains(Object value)
    {
        //Verifica de que no le pasen nulls
        if(value == null) return false;
        //Recorrido
        for (var i: table)
        {
            Entry<K,V> unEntry = (Entry<K, V>) i;
            if (unEntry.state == CLOSED && unEntry.value == (V)value)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Incrementa el tamaÃ±o de la tabla y reorganiza su contenido. Se invoca 
     * automaticamente cuando se detecta que la cantidad promedio de nodos por 
     * lista supera a cierto el valor critico dado por (10 * load_factor). Si el
     * valor de load_factor es 0.8, esto implica que el lÃƒÂ­mite antes de invocar 
     * rehash es de 8 nodos por lista en promedio, aunque seria aceptable hasta 
     * unos 10 nodos por lista.
     */
    protected void rehash()
    {
        int old_length = this.table.length;
        
        // nuevo tamaÃ±o: primer primo mayor o igual al 50% del anterior...
        int new_length = nextPrime((int)(old_length * 1.5f));
        
        // crear el nuevo arreglo de tamaÃ±o new_length...
        Object temp[] = new Object[new_length];
        for(int j=0; j<temp.length; j++) { temp[j] = new Entry<>(null, null); }
        
        // notificaciÃƒÂ³n fail-fast iterator... la tabla cambiÃƒÂ³ su estructura...
        this.modCount++;  
       
        // recorrer el viejo arreglo y redistribuir los objetos que tenia...
        for(int i=0; i<this.table.length; i++)
        {
           // obtener un objeto de la vieja lista...
           Entry<K, V> x = (Entry<K, V>) table[i];

           // si la casilla estÃ¡ cerrada...
           if(x.getState() == CLOSED)
           {
               // ...obtener el valor de dispersiÃƒÂ³n en el nuevo arreglo...
               K key = x.getKey();
               int ik = this.h(key, temp.length);
               int y = search_for_OPEN(temp, ik);

               // ...insertar en el nuevo arreglo
               temp[y] = x;
           }
        }
       
        // cambiar la referencia table para que apunte a temp...
        this.table = temp;
    }
    

    //************************ Metodos privados.
    
    /*
     * Funcion hash. Toma una clave entera k y calcula y retorna un i­ndice 
     * valido para esa clave para entrar en la tabla.     
     */
    private int h(int k)
    {
        return h(k, this.table.length);
    }
    
    /*
     * Funcion hash. Toma un objeto key que representa una clave y calcula y 
     * retorna un i­ndice valido para esa clave para entrar en la tabla.     
     */
    private int h(K key)
    {
        return h(key.hashCode(), this.table.length);
    }
    
    /*
     * Funcion hash. Toma un objeto key que representa una clave y un tamaÃ±o de 
     * tabla t, y calcula y retorna un i­ndice valido para esa clave dedo ese
     * tamaño.     
     */
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }
    
    /*
     * Funcion hash. Toma una clave entera k y un tamaño de tabla t, y calcula y 
     * retorna un i­ndice valido para esa clave dado ese tamaño.     
     */
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;        
    }

    private boolean isPrime(int n)
    {
        // negativos no admitidos en este contexto...
        if(n < 0) return false;

        if(n == 1) return false;
        if(n == 2) return true;
        if(n % 2 == 0) return false;

        int raiz = (int) Math.pow(n, 0.5);
        for(int div = 3;  div <= raiz; div += 2)
        {
            if(n % div == 0) return false;
        }

        return true;
    }

    private int nextPrime (int n)
    {
        if(n % 2 == 0) n++;
        for(; !isPrime(n); n+=2);
        return n;
    }

    /**
     * Calcula el nivel de carga de la tabla, como un numero en coma flotante entre 0 y 1.
     * Si este valor se multiplica por 100, el resultado es el porcentaje de ocupacion de la
     * tabla.
     * @return el nivel de ocupacion de la tabla.
     */
    private float load_level()
    {
        return (float) this.count / this.table.length;
    } 
    
    /*
     * Busca en la tabla un objeto Entry cuya clave coincida con key, a partir
     * de la posicion ik. Si lo encuentra, retorna ese objeto Entry. Si no lo
     * encuentra, retorna null. Aplica exploracion cuadratica.
     */
    private Map.Entry<K, V> search_for_entry(K key, int ik)
    {
        int pos = search_for_index(key, ik);
        return pos != -1 ? (Map.Entry<K, V>) table[pos] : null;
    }
    
    /*
     * Busca en la tabla un objeto Entry cuya clave coincida con key, a partir
     * de la posiciÃ³n ik. Si lo encuentra, retorna su posiciÃ³n. Si no lo encuentra,
     * retorna -1. Aplica exploraciÃ³n cuadrÃ¡tica.
     */
    private int search_for_index(K key, int ik)
    {
        for(int j=0; ;j++)
        {
            int y = ik + (int)Math.pow(j, 2);
            y %= table.length;

            Entry<K, V> entry = (Entry<K, V>) table[y];
            if(entry.getState() == OPEN) { return -1; }
            if(key.equals(entry.getKey())) { return y; }
        }
    }

    /*
     * Retorna el Ã­Â­ndice de la primera casilla abierta, a partir de la posiciÃ³n ik,
     * en la tabla t. Aplica exploraciÃ³n cuadrÃ¡tica.
     */
    private int search_for_OPEN(Object t[], int ik)
    {
        for(int j=0; ;j++)
        {
            int y = ik + (int)Math.pow(j, 2);
            y %= t.length;

            Entry<K, V> entry = (Entry<K, V>) t[y];
            if(entry.getState() == OPEN) { return y; }
        }
    }

    //************************ Clases Internas.

    /*
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en 
     * en cada una de las listas del arreglo table que se usa como soporte de 
     * la tabla. LanzarÃ¡Â¡ una IllegalArgumentException si alguno de los dos 
     * parÃ¡metros es null.
     */
    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;
        private int state;
        
        public Entry(K key, V value) 
        {
            this(key, value, OPEN);
        }

        public Entry(K key, V value, int state)
        {
            this.key = key;
            this.value = value;
            this.state = state;
        }

        @Override
        public K getKey() 
        {
            return key;
        }

        @Override
        public V getValue() 
        {
            return value;
        }

        public int getState() { return state; }

        @Override
        public V setValue(V value) 
        {
            if(value == null) 
            {
                throw new IllegalArgumentException("setValue(): parÃƒÂ¡metro null...");
            }
                
            V old = this.value;
            this.value = value;
            return old;
        }

        public void setState(int ns)
        {
            if(ns >= 0 && ns < 3)
            {
                state = ns;
            }
        }
       
        @Override
        public int hashCode() 
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);            
            return hash;
        }

        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }
            
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }            
            return true;
        }       
        
        @Override
        public String toString()
        {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }
    
    /*
     * Clase interna que representa una vista de todas los Claves mapeadas en la
     * tabla: si la vista cambia, cambia tambiÃƒÂ©n la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no 
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus mÃƒÂ©todos gestionan en
     * forma directa el contenido de la tabla. EstÃ¡n soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la  
     * creaciÃƒÂ³n de un Iterator (que incluye el mÃƒÂ©todo Iterator.remove()).
     */    
    private class KeySet extends AbstractSet<K> 
    {
        @Override
        public Iterator<K> iterator() 
        {
            return new KeySetIterator();
        }
        
        @Override
        public int size() 
        {
            return TSBHashtableDA.this.count;
        }
        
        @Override
        public boolean contains(Object o) 
        {
            return TSBHashtableDA.this.containsKey(o);
        }
        
        @Override
        public boolean remove(Object o) 
        {
            return (TSBHashtableDA.this.remove(o) != null);
        }
        
        @Override
        public void clear() 
        {
            TSBHashtableDA.this.clear();
        }
        
        private class KeySetIterator implements Iterator<K>
        {
           
            // indice de la entrada actual
            private int entry_current;
            // indice de la la ultima entrada recorrida
            private int entry_last;

            // flag para controlar si remove() esta¡ bien invocado...
            private boolean next_ok;
            
            // el valor que deberi­a tener el modCount de la tabla completa...
            private int expected_modCount;
            
            //Agregados 
            private K siguiente;
            private int posActual;
            /*
             * Crea un iterador comenzando en la primera lista. Activa el 
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                //Se inicializan
                entry_current = -1;
                entry_last = 0;
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
                posActual=0;
                siguiente=null;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya 
             * sido retornado por next(). 
             */
            @Override
            public boolean hasNext() 
            {
                //Verifica que la tabla no este vacia
                if(table.length == 0 ) { return false; }
                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //En caso de ser null el siguiente, busca el prox no null
                while(next < table.length && ((Entry<K,V>)table[next]).key==null )
                {
                    next++;
                }
                //Si se llego al final y no se encontro otro elemento
                if(next==table.length) {return false;}
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next() 
            {

                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {    
                    throw new ConcurrentModificationException("next(): modificaciÃƒÂ³n inesperada de tabla...");
                }
                
                if(!hasNext()) 
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                
                // avisar que next() fue invocado con ÃƒÂ©xito...
                next_ok = true;
                
                // y retornar la clave del elemento alcanzado...
                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //busca el prox no null (igual que el hasNext())
                while(next < table.length && ((Entry<K,V>)table[next]).key==null )
                {
                    next++;
                }
                //busca y convierte la key a retornar
                siguiente = ((Entry <K,V>)table[next]).key;
                //Setea la posicion actual a la key encontrada
                posActual=next;
                // y retornar el elemento alcanzado...
                return siguiente;
            }
            
            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posiciÃ³n anterior al que fue removido. El elemento removido es el
             * que fue retornado la Ãºltima vez que se invocÃ³ a next(). El mÃ©todo
             * sÃ³lo puede ser invocado una vez por cada invocaciÃ³n a next().
             */
            @Override
            public void remove() 
            {


                if(!next_ok) 
                { 
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()..."); 
                }
                
                // avisar que el remove() vÃ¡lido para next() ya se activÃ³...
                next_ok = false;
                                
                // la tabla tiene un elemento menos...
                TSBHashtableDA.this.count--;

                // fail_fast iterator...
                TSBHashtableDA.this.modCount++;
                expected_modCount++;
                //remueve la key, poniendola en tumba
                Entry<K,V> theEntry = (Entry<K,V>)table[posActual];
                theEntry.state=TOMBSTONE;
                theEntry.value=null;
                theEntry.key=null;
                posActual--;
            }     
        }
    }

    /*
     * Clase interna que representa una vista de todos los PARES mapeados en la
     * tabla: si la vista cambia, cambia tambien la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no 
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus metodos gestionan en
     * forma directa el contenido de la tabla. Estan soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la  
     * creacion de un Iterator (que incluye el metodo Iterator.remove()).
     */    
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> 
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() 
        {
            return new EntrySetIterator();
        }

        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par 
         * que entra como parametro (que debe ser de la clase Entry).

         */
        @Override
        public boolean contains(Object o) 
        {
            //Verifica que el objeto que entre no sea null Y sea una entrada
            if(o == null) { return false; } 
            if(!(o instanceof Map.Entry<?,?>)) { return false; }
            
            //Convierte a una entrada el par ingresado
            Map.Entry<K,V> entry = (Map.Entry<K, V>)o;
            
            //Guarda el valor de la key de la entrada 
            //K key = entry.getKey();
            //Guarda el hascode
            //int idex = TSBHashtableDA.this.h(key);            
            
            //Verifica que la entrada ingresada esta contenida en al tabla
            if (this.contains(entry)) {return true;}
                        
            return false;
        }

        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parametro (y que debe ser de tipo Entry).

         */
        @Override
        public boolean remove(Object o) 
        {
            //Verifica que lo que entre sea una entrada
            if(o == null) { throw new NullPointerException("remove(): parametro null");}


            if(!(o instanceof Map.Entry<?,?>)) { return false; }
            
            //Convierte a una entrada el par ingresado
            Map.Entry<K,V> entry = (Map.Entry<K, V>)o;
            
            //Elimina la entrada
            if (this.remove(entry)) {
                TSBHashtableDA.this.count--;
                TSBHashtableDA.this.modCount++;
                return true;
            }
            return false;
        }

        @Override
        public int size() 
        {
            return TSBHashtableDA.this.count;
        }

        @Override
        public void clear() 
        {
            TSBHashtableDA.this.clear();
        }
        
        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {
            private boolean next_ok;
            
            // el valor que deberÃƒÂ­a tener el modCount de la tabla completa...
            private int expected_modCount;
            // agregados
            private int posActual;
            private Map.Entry<K,V> siguiente;
            /*
             * Crea un iterador comenzando en la primera lista. Activa el 
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
                posActual=-1;
                siguiente =null;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya 
             * sido retornado por next(). 
             */
            @Override
            public boolean hasNext()
            {

                //Verifica que la tabla no este vacia
                if(table.length == 0 ) { return false; }
                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //En caso de ser null el siguiente, busca el prox no null
                while(next < table.length && ((Entry<K,V>)table[next]).key==null )
                {
                    next++;
                }
                //Si se llego al final y no se encontro otro elemento
                if(next==table.length) {return false;}
                return true;


            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Map.Entry<K, V> next()
            {
                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {    
                    throw new ConcurrentModificationException("next(): modificaciÃƒÂ³n inesperada de tabla...");
                }
                
                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // avisar que next() fue invocado con ÃƒÂ©xito...
                next_ok = true;

                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //busca el prox no null (igual que el hasNext())
                while(next < table.length && ((Entry<K,V>)table[next]).key==null )
                {
                    next++;
                }
                //busca y convierte el entry a retornar
                siguiente = ((Entry <K,V>)table[next]);
                //Setea la posicion actual al entry encontrado
                posActual=next;
                // y retornar el elemento alcanzado...
                return siguiente;

            }
            
            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posiciÃƒÂ³n anterior al que fue removido. El elemento removido es el
             * que fue retornado la ÃƒÂºltima vez que se invocÃƒÂ³ a next(). El mÃƒÂ©todo
             * sÃƒÂ³lo puede ser invocado una vez por cada invocaciÃƒÂ³n a next().
             */
            @Override
            public void remove() 
            {
                if(!next_ok) 
                { 
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()..."); 
                }
                

                // avisar que el remove() vÃƒÂ¡lido para next() ya se activÃƒÂ³...
                next_ok = false;
                                
                // la tabla tiene un elementon menos...
                TSBHashtableDA.this.count--;

                // fail_fast iterator...
                TSBHashtableDA.this.modCount++;
                expected_modCount++;
                //remueve el objeto, poniendolo en tumba
                ((Entry<K,V>)table[posActual]).state=TOMBSTONE;
                ((Entry<K,V>)table[posActual]).value=null;
                ((Entry<K,V>)table[posActual]).key=null;

            }     
        }
    }    
    
    /*
     * Clase interna que representa una vista de todos los VALORES mapeados en 
     * la tabla: si la vista cambia, cambia tambiÃƒÂ©n la tabla que le da respaldo, 
     * y viceversa. La vista es stateless: no mantiene estado alguno (es decir, 
     * no contiene datos ella misma, sino que accede y gestiona directamente los
     * de otra fuente), por lo que no tiene atributos y sus mÃƒÂ©todos gestionan en
     * forma directa el contenido de la tabla. EstÃ¡n soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la  
     * creaciÃƒÂ³n de un Iterator (que incluye el mÃƒÂ©todo Iterator.remove()).
     */ 
    private class ValueCollection extends AbstractCollection<V> 
    {
        @Override
        public Iterator<V> iterator() 
        {
            return new ValueCollectionIterator();
        }
        
        @Override
        public int size() 
        {
            return TSBHashtableDA.this.count;
        }
        
        @Override
        public boolean contains(Object o) 
        {
            return TSBHashtableDA.this.containsValue(o);
        }
        
        @Override
        public void clear() 
        {
            TSBHashtableDA.this.clear();
        }
        
        private class ValueCollectionIterator implements Iterator<V>
        {
            // flag para controlar si remove() estÃ¡ bien invocado...
            private boolean next_ok;
            
            // el valor que deberÃƒÂ­a tener el modCount de la tabla completa...
            private int expected_modCount;
            //agregado 
            private int posActual;
            private V siguiente;
            
            /*
             * Crea un iterador comenzando en la primera lista. Activa el 
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
                posActual=0;
                siguiente =null;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya 
             * sido retornado por next(). 
             */
            @Override
            public boolean hasNext() 
            {
                //Verifica que la tabla no este vacia
                if(table.length == 0 ) { return false; }
                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //En caso de ser null el siguiente, busca el prox no null
                while(next < table.length && ((Entry<K,V>)table[next]).key==null )
                {
                    next++;
                }
                //Si se llego al final y no se encontro otro elemento
                if(next==table.length) {return false;}
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next() 
            {
                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {    
                    throw new ConcurrentModificationException("next(): modificaciÃƒÂ³n inesperada de tabla...");
                }
                
                if(!hasNext()) 
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                

                // avisar que next() fue invocado con ÃƒÂ©xito...
                next_ok = true;


                //Crea el indice para el siguiente elemento
                int next = posActual+1;
                //busca el prox no null (igual que el hasNext())
                while(next < table.length && ((Entry<K,V>)table[next]).value==null )
                {
                    next++;
                }
                //busca y convierte la key a retornar
                siguiente = ((Entry <K,V>)table[next]).value;
                //Setea la posicion actual a la key encontrada
                posActual=next;
                // y retornar el elemento alcanzado...
                return siguiente;
            }
            
            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posiciÃƒÂ³n anterior al que fue removido. El elemento removido es el
             * que fue retornado la ÃƒÂºltima vez que se invocÃƒÂ³ a next(). El mÃƒÂ©todo
             * sÃƒÂ³lo puede ser invocado una vez por cada invocaciÃƒÂ³n a next().
             */
            @Override
            public void remove() 
            {
                if(!next_ok) 
                { 
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()..."); 
                }
                

                // avisar que el remove() vÃƒÂ¡lido para next() ya se activÃƒÂ³...
                next_ok = false;
                                
                // la tabla tiene un elementon menos...
                TSBHashtableDA.this.count--;

                // fail_fast iterator...
                TSBHashtableDA.this.modCount++;
                expected_modCount++;

                //remueve el value, poniendola en null
                ((Entry<K,V>)table[posActual]).value=null;
            }     
        }
    }
}
