import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    private static final String[] NOMBRES_REALES = {
        "Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "Miguel", "Sofía",
        "Ricardo", "Isabel", "José", "Gabriela", "Manuel", "Valentina", 
        // ... (lista de nombres)
    };

    public static MainFrame myFrame;

    public static void Simulacion(int numCajas, int numClientes, int numCadetes) {
        List<SupermarketSimulation.CajaRegistradora> cajasRegistradoras = new ArrayList<>();
        for (int i = 1; i <= numCajas; i++) {
            cajasRegistradoras.add(new SupermarketSimulation.CajaRegistradora("Caja " + i));
        }
        myFrame.setInfo("");
        myFrame.setInfo("   MOVIMIENTO DE CAJAS:");
        
        //----lista de hilos clientes
        List<Thread> clientesThreads = new ArrayList<>();
        List<SupermarketSimulation.Envio> enviosPendientes = new ArrayList<>(); // Lista para almacenar envíos pendientes

        for (int i = 1; i <= numClientes; i++) {
            String nombreCliente = NOMBRES_REALES[new Random().nextInt(NOMBRES_REALES.length)];
            int indiceCadete = (i - 1) % numCadetes; // Calculamos el índice del cadete responsable para este cliente
            String nombreCadeteResponsable = "Cadete " + (indiceCadete + 1);
            SupermarketSimulation.ClienteRunnable clienteRunnable = new SupermarketSimulation.ClienteRunnable("Cliente " + i + " (" + nombreCliente + ")", numCajas, cajasRegistradoras, enviosPendientes, nombreCadeteResponsable);
            Thread clienteThread = new Thread(clienteRunnable);
            clientesThreads.add(clienteThread);
            clienteThread.start();
        }

        // Crear un pool de hilos para los cadetes
        ExecutorService cadetesThreadPool = Executors.newFixedThreadPool(numCadetes);
        List<SupermarketSimulation.Cadete> cadetes = new ArrayList<>();
        for (int i = 1; i <= numCadetes; i++) {
            SupermarketSimulation.Cadete cadete = new SupermarketSimulation.Cadete("Cadete " + i, enviosPendientes);
            cadetes.add(cadete);
            cadetesThreadPool.execute(cadete);
        }

        // Esperar a que todos los clientes terminen de hacer sus compras
        for (Thread clienteThread : clientesThreads) {
            try {
                clienteThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Detener el pool de hilos de los cadetes para que no acepten más trabajos
        cadetesThreadPool.shutdown();

        try {
            // Esperar hasta que todos los envíos se completen (los cadetes terminen de entregar las compras)
            cadetesThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        myFrame.setInfo("");
        myFrame.setInfo("RESULTADOS DE LAS VENTAS:");
        myFrame.setInfo("");
        for (SupermarketSimulation.CajaRegistradora caja : cajasRegistradoras) {
            caja.mostrarResultados();
        }

        myFrame.setInfo("");
        myFrame.setInfo("RANKING DE CAJAS POR CANTIDAD DE CLIENTES ATENDIDOS:");
        myFrame.setInfo("");
        cajasRegistradoras.sort(Comparator.comparingInt(SupermarketSimulation.CajaRegistradora::getCantidadClientes).reversed());
        for (int i = 0; i < cajasRegistradoras.size(); i++) {
            SupermarketSimulation.CajaRegistradora caja = cajasRegistradoras.get(i);
            myFrame.setInfo((i + 1) + ". " + caja.getNombre() + " - Clientes atendidos: " + caja.getCantidadClientes());
        }

        // Mostramos los envíos pendientes
        myFrame.setInfo("");
        myFrame.setInfo("ENVÍOS PENDIENTES:");
        myFrame.setInfo("");
        for (SupermarketSimulation.Envio envio : enviosPendientes) {
            envio.mostrarEnvio();
        }
    }

    public static void main(String[] args) {
        /* -- Inico interfaz visual -- */
        myFrame = new MainFrame();
        myFrame.initializa();
    }
}