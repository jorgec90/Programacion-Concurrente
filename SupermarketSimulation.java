import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SupermarketSimulation {

    private static final String[] NOMBRES_REALES = { //nombres de clientes aleatorios para los clientes 
        "Luis", "Ana", "Carlos", "María", "Juan", "Florencia", "Miguel", "Valentina", "José", "Sofía", "Jorge",
        "Camila", "Mario", "Lucía", "Raúl","Martina", "Fernando", "Juana", "Diego", "Elena", "Gustavo",
        "Julieta", "Pablo", "Lara", "Ignacio", "Agustina", "Daniel", "Bianca", "Roberto", "Antonella", 
        "Alejandro", "Celeste", "Héctor", "Abril", "Nicolás", "Victoria", "Eduardo", "Romina", "Hugo","Luciana",
        "Francisco", "Mariana", "Emilio", "Catalina", "Hernán", "Jimena", "Ricardo", "Gabriela","Néstor", "Milagros",
        "Julio", "Rocío", "Marcelo", "Carolina", "Oscar", "Delfina", "Sergio", "Luciana", "Andrés", "Florencia", 
        "Marcos", "Valeria", "Rubén", "Natalia", "Adrián", "Isabella",  "Ángel", "Candela", "Maximiliano", 
        "Abril", "Ezequiel", "Ariadna", "Walter", "Emilia", "Gonzalo", "Carla", "Lucas", "Dolores", "Bautista", "Iara",
        "Carmen", "Nelson", "Renata", "Rafael", "Martina", "Enrique", "Luciana", "Darío", "Renata", "Facundo",
        "Samantha", "Leonardo", "Aitana", "Guido", "Clarisa", "Rodrigo", "Pilar", "Bruno", "Lucila", "Joaquín",
             
        // ... (lista de nombres) 6 60 8
    };
//METODO MAIN 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); //objeto escaner
        System.out.println();
        System.out.print("Ingrese la cantidad de cajas en el supermercado: ");
        int numCajas = scanner.nextInt();
        System.out.println();
        System.out.print("Ingrese la cantidad de clientes que ingresan al supermercado: ");
        int numClientes = scanner.nextInt();
        System.out.println();
        System.out.print("Ingrese la cantidad de cadetes para hacer los envíos: ");
        int numCadetes = scanner.nextInt();

        List<CajaRegistradora> cajasRegistradoras = new ArrayList<>(); // lista de objetos CAJAS (representaran las cajas libres)
        for (int i = 1; i <= numCajas; i++) {  // creacion de cajas , cantidad igual a ingresada por teclado 
            cajasRegistradoras.add(new CajaRegistradora("Caja " + i));  //las agregamos a la lista 
        }
        System.out.println("\n   MOVIMIENTO DE CAJAS: \n");
        //----lista de hilos clientes
        List<Thread> clientesThreads = new ArrayList<>(); // Lista clientesThreads, que contendrá los hilos para los clientes del supermercado,
        List<Envio> enviosPendientes = new ArrayList<>(); // Lista que almacenará los envíos que deben entregarse a domicilio

        for (int i = 1; i <= numClientes; i++) { //Ciclo que simula la llegada de clientes al supermercado.
            String nombreCliente = NOMBRES_REALES[new Random().nextInt(NOMBRES_REALES.length)]; // Se le asigna nombre
            int indiceCadete = (i - 1) % numCadetes; // Calculamos el índice del cadete responsable para este cliente
            String nombreCadeteResponsable = "Cadete " + (indiceCadete + 1); //le asignamos el cadete responsable a cada cliente
            ClienteRunnable clienteRunnable = new ClienteRunnable("Cliente " + i + " (" + nombreCliente + ")", numCajas, cajasRegistradoras, enviosPendientes, nombreCadeteResponsable);
            Thread clienteThread = new Thread(clienteRunnable); // creo un hilo con ese objeto ClienteRunnable, implementación de Runnable que contiene la lógica del comportamiento del cliente en el supermercado.
            clientesThreads.add(clienteThread); // ClienteRunnable, se agrega a la lista clientesThreads y se inicia la ejecución del hilo.
            clienteThread.start();
        }

        // Crear un pool de hilos para los cadetes,utilizando la clase ExecutorService 
        // Esto crea un conjunto de hilos reutilizables, en este caso, la cantidad de hilos es igual a numCadetes
        ExecutorService cadetesThreadPool = Executors.newFixedThreadPool(numCadetes);
        List<Cadete> cadetes = new ArrayList<>(); //lista de cadetes 
        for (int i = 1; i <= numCadetes; i++) {
            Cadete cadete = new Cadete("Cadete " + i, enviosPendientes); //le pasamos el nro. cadete y los envios 
            cadetes.add(cadete);
            cadetesThreadPool.execute(cadete); //enviar a cada objeto cadete al pool de hilos para su ejecucion
        }

        // Esperar a que todos los clientes terminen de hacer su compra en esa caja, 
        // Esto asegura que todos los clientes completen su compra antes de continuar con el siguiente paso.
        for (Thread clienteThread : clientesThreads) {
            try {
                clienteThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Detener el pool de hilos de los cadetes para que no acepten más trabajos, 
        cadetesThreadPool.shutdown();
       //   Luego, se espera hasta que todos los envíos se completen utilizando cadetesThreadPool.awaitTermination.
       //   Esta llamada bloqueará el hilo actual hasta que todos los envíos pendientes sean entregados.
        try {
            // (los cadetes terminen de entregar las compras)
            cadetesThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//  Después de que todos los clientes han sido atendidos y los envíos se han entregado, 
//  se imprime un resumen de cada caja registradora utilizando el método mostrarResultados()
//  en cada objeto CajaRegistradora.
        System.out.println();
        System.out.println("RESULTADOS DE LAS VENTAS :");
        System.out.println();
        for (CajaRegistradora caja : cajasRegistradoras) {
            caja.mostrarResultados();
        }
// Se utiliza el método sort() para ordenar la lista cajasRegistradoras
        System.out.println();
        System.out.println("RANKING DE CAJAS POR CANTIDAD DE CLIENTES ATENDIDOS:");
        System.out.println();
        cajasRegistradoras.sort(Comparator.comparingInt(CajaRegistradora::getCantidadClientes).reversed());
        for (int i = 0; i < cajasRegistradoras.size(); i++) {
            CajaRegistradora caja = cajasRegistradoras.get(i);
            System.out.println((i + 1) + ". " + caja.getNombre() + " - Clientes atendidos: " + caja.getCantidadClientes());
        }

        // Mostramos los envíos pendientes con el metodo mostrarEnvio(); los que aun no fueron entregados 
        System.out.println();
        System.out.println("ENVÍOS PENDIENTES:");
        System.out.println();
        for (Envio envio : enviosPendientes) {
            envio.mostrarEnvio();
        }

        scanner.close(); // para liberar los recursos utilizados para leer la entrada del usuario
    }

    //---- LOGICA del comportamiento del cliente en el supermercado. (COMPRAS QUE REALIZA)
    static class ClienteRunnable implements Runnable {
        private String nombre;
        private int numCajas; 
        private List<CajaRegistradora> cajasRegistradoras;
        private List<Envio> enviosPendientes; // Lista para almacenar envíos pendientes
        private String nombreCadeteResponsable;

        public ClienteRunnable(String nombre, int numCajas, List<CajaRegistradora> cajasRegistradoras, List<Envio> enviosPendientes, String nombreCadeteResponsable) {
            this.nombre = nombre;
            this.numCajas = numCajas;
            this.cajasRegistradoras = cajasRegistradoras;
            this.enviosPendientes = enviosPendientes;
            this.nombreCadeteResponsable = nombreCadeteResponsable;
        }

        @Override
        public void run() {
            int cantidadProductos = new Random().nextInt(10) + 1; //Se genera un número aleatorio de productos que el cliente comprará (1-10)
            double montoTotal = cantidadProductos * 5;
//lista con la compra de cada cliente 
            List<Producto> productosComprados = new ArrayList<>(); //lista de los producos que comprara 
            for (int i = 0; i < cantidadProductos; i++) {  //nombrmos los productos (producto 1, producto 2, producto 3,,etc)
                productosComprados.add(new Producto("Producto " + (i + 1), 5.0)); //agregamos 
            }
            
            int indiceCaja = new Random().nextInt(numCajas);//selecciona una caja random para pagar 

            //************************************************************************************* */

            CajaRegistradora cajaRegistradora = cajasRegistradoras.get(indiceCaja);
            while (true) { // Se verifica si la caja seleccionada está disponible utilizando el método estaDisponible() de la clase CajaRegistradora, sino sera otra 
                if (cajaRegistradora.estaDisponible()) {
                    atenderEnCaja(cajaRegistradora, nombre, productosComprados, montoTotal); //esta disponible , metodo atenderEnCaja(recibe los datos)
                    break; //
                } else {
                    indiceCaja = new Random().nextInt(numCajas);
                    cajaRegistradora = cajasRegistradoras.get(indiceCaja);
                }
            }
        }
            // metodo atencion caja 
        private void atenderEnCaja(CajaRegistradora cajaRegistradora, String nombreCliente, List<Producto> productosComprados, double montoTotal) {
            cajaRegistradora.atenderCliente(nombreCliente, productosComprados, montoTotal);
            agregarEnvioPendiente(nombreCliente, cajaRegistradora, productosComprados);
            System.out.println();
        }
        //metodo agregar envio pendiente. agregamos la compra a ENVIOS PENDIENTES 
        private void agregarEnvioPendiente(String nombreCliente, CajaRegistradora cajaRegistradora, List<Producto> productosComprados) {
            enviosPendientes.add(new Envio(nombreCliente, cajaRegistradora, productosComprados, nombreCadeteResponsable));
        }
    }
// ENVIOS A DOMICILIO  
    static class Cadete implements Runnable {
        private String nombre;
        private List<Envio> enviosPendientes;

        public Cadete(String nombre, List<Envio> enviosPendientes) {
            this.nombre = nombre;
            this.enviosPendientes = enviosPendientes;
        }

        @Override
        public void run() {
            while (true) { //todo el tiempo el cadete esta fijandose si hay envios pendientes 
                Envio envio = null;   
                synchronized (enviosPendientes) { //toma el envio , sincronizado para evitar choques 
                    if (!enviosPendientes.isEmpty()) {
                        envio = enviosPendientes.remove(0);
                    }
                }
                if (envio != null) { // Si hay un envío pendiente (envio != null), el cadete procede a entregar el envío llamando al método entregarEnvio()
                    entregarEnvio(envio);
                } else {
                    break; // Si no hay envíos pendientes, el cadete termina de trabajar
                }
            }
        }
        // La lógica específica de la entrega del envío se encuentra dentro del método entregarEnvio()
        private void entregarEnvio(Envio envio) {
            String fechaHoraStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            System.out.println(fechaHoraStr + " - " + nombre + " ha INICIADO la entrega del envío del " + envio.getNombreCliente() + " a su domicilio desde " + envio.getCajaRegistradora().getNombre());
            try {
                TimeUnit.SECONDS.sleep(5);; // Simulamos el tiempo que tarda en entregar el envío (segundos)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(fechaHoraStr + " - " + nombre + " ha ENTREGADO el envío del " + envio.getNombreCliente() + " a su domicilio desde " + envio.getCajaRegistradora().getNombre());
        }
    }
// clase interna representa un objeto de envío y contiene información sobre el cliente, 
// la caja registradora, los productos comprados y el cadete responsable de la entrega.
    static class Envio {
        private String nombreCliente;
        private CajaRegistradora cajaRegistradora;
        private List<Producto> productosComprados;
        private String cadeteResponsable;

        public Envio(String nombreCliente, CajaRegistradora cajaRegistradora, List<Producto> productosComprados, String cadeteResponsable) {
            this.nombreCliente = nombreCliente;
            this.cajaRegistradora = cajaRegistradora;
            this.productosComprados = productosComprados;
            this.cadeteResponsable = cadeteResponsable;
        }

        public String getNombreCliente() {
            return nombreCliente;
        }

        public CajaRegistradora getCajaRegistradora() {
            return cajaRegistradora;
        }

        public String getCadeteResponsable() {
            return cadeteResponsable;
        }

        public void mostrarEnvio() {
            String fechaHoraStr = new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date());
            String mensaje = fechaHoraStr + " - Enviando Productos del " + nombreCliente + " a su domicilio desde " + cajaRegistradora.getNombre() + "- Cadete responsable: " + cadeteResponsable;
            System.out.println(mensaje);
        }

        private String obtenerListaProductos() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < productosComprados.size(); i++) {
                Producto producto = productosComprados.get(i);
                sb.append(producto.getNombre());
                sb.append("($").append(producto.getPrecio()).append(")");
                if (i < productosComprados.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    static class Producto {
        private String nombre;
        private double precio;

        public Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getNombre() {
            return nombre;
        }

        public double getPrecio() {
            return precio;
        }
    }

    static class CajaRegistradora {
        private String nombre;
        private double totalRecaudado;
        private int totalProductos;
        private int cantidadClientes;
        private boolean ocupada;
        private final Object lock = new Object();

        public CajaRegistradora(String nombre) {
            this.nombre = nombre;
            this.totalRecaudado = 0;
            this.totalProductos = 0;
            this.cantidadClientes = 0;
            this.ocupada = false;
        }

        public boolean estaDisponible() {
            return !ocupada;
        }  //************************************************************************ */
//Para garantizar la sincronización y evitar problemas de concurrencia, 
//la clase CajaRegistradora utiliza un objeto de bloqueo (lock)
// que se sincroniza para proteger las secciones críticas de código donde se actualizan los datos compartidos por múltiples hilos.
        public void atenderCliente(String nombreCliente, List<Producto> productosComprados, double montoTotal) {
            synchronized (lock) { //OBJETO LOCK garantiza que solo un hilo (CLIENTE) pueda ejecutar el bloque de código sincronizado a la vez. 
                ocupada = true;  //ocupada= true / ocupada=false
                totalRecaudado += montoTotal;
                totalProductos += productosComprados.size();
                cantidadClientes++;
                String fechaHoraStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                System.out.println(fechaHoraStr + " - " + nombreCliente + " ha sido atendido en " + nombre + " - Monto pagado: $ " + montoTotal + ", Cantidad de productos comprados: " + productosComprados.size());
                ocupada = false;
            }
        }

        public void mostrarResultados() {
            System.out.println(nombre + " - Total recaudado: $ " + totalRecaudado + ", Cantidad de productos vendidos: " + totalProductos + ", Clientes atendidos: " + cantidadClientes);
        }

        public String getNombre() {
            return nombre;
        }

        public int getCantidadClientes() {
            return cantidadClientes;
        }
    }
}
