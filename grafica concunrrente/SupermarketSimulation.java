import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class SupermarketSimulation {

    //---- contiene la lógica del comportamiento del cliente en el supermercado.
    static class ClienteRunnable implements Runnable {
        private String nombre;
        private int numCajas; // Número de cajas en el supermercado
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
            int cantidadProductos = new Random().nextInt(10) + 1;
            double montoTotal = cantidadProductos * 5;

            List<Producto> productosComprados = new ArrayList<>();
            for (int i = 0; i < cantidadProductos; i++) {
                productosComprados.add(new Producto("Producto " + (i + 1), 5.0));
            }

            int indiceCaja = new Random().nextInt(numCajas);

            //String fechaHoraStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //System.out.println(fechaHoraStr + " - " + nombre + " ha pagado $ " + montoTotal + " por sus " + cantidadProductos + " productos en Caja " + (indiceCaja + 1));

            CajaRegistradora cajaRegistradora = cajasRegistradoras.get(indiceCaja);
            while (true) {
                if (cajaRegistradora.estaDisponible()) {
                    atenderEnCaja(cajaRegistradora, nombre, productosComprados, montoTotal);
                    break;
                } else {
                    indiceCaja = new Random().nextInt(numCajas);
                    cajaRegistradora = cajasRegistradoras.get(indiceCaja);
                }
            }
        }

        private void atenderEnCaja(CajaRegistradora cajaRegistradora, String nombreCliente, List<Producto> productosComprados, double montoTotal) {
            cajaRegistradora.atenderCliente(nombreCliente, productosComprados, montoTotal);
            agregarEnvioPendiente(nombreCliente, cajaRegistradora, productosComprados);
            //App.myFrame.setInfo("");
        }

        private void agregarEnvioPendiente(String nombreCliente, CajaRegistradora cajaRegistradora, List<Producto> productosComprados) {
            enviosPendientes.add(new Envio(nombreCliente, cajaRegistradora, productosComprados, nombreCadeteResponsable));
        }
    }

    static class Cadete implements Runnable {
        private String nombre;
        private List<Envio> enviosPendientes;

        public Cadete(String nombre, List<Envio> enviosPendientes) {
            this.nombre = nombre;
            this.enviosPendientes = enviosPendientes;
        }

        @Override
        public void run() {
            while (true) {
                Envio envio = null;
                synchronized (enviosPendientes) {
                    if (!enviosPendientes.isEmpty()) {
                        envio = enviosPendientes.remove(0);
                    }
                }
                if (envio != null) {
                    entregarEnvio(envio);
                } else {
                    break; // Si no hay envíos pendientes, el cadete termina de trabajar
                }
            }
        }

        private void entregarEnvio(Envio envio) {
            String fechaHoraStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
            App.myFrame.setInfo(fechaHoraStr + " - " + nombre + " ha INICIADO envío del " + envio.getNombreCliente() + " a domicilio desde " + envio.getCajaRegistradora().getNombre());
            try {
                TimeUnit.SECONDS.sleep(6);; // Simulamos el tiempo que tarda en entregar el envío (1 segundo)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            App.myFrame.setInfo(fechaHoraStr + " - " + nombre + " ha ENTREGADO el envío de " + envio.getNombreCliente() + " a domicilio desde " + envio.getCajaRegistradora().getNombre() + "- Cadete responsable: " + envio.getCadeteResponsable());
        }
    }

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
            String fechaHoraStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String mensaje = fechaHoraStr + " - Envio del " + nombreCliente + " a domicilio desde " + cajaRegistradora.getNombre() + "- Cadete: " + cadeteResponsable;
            App.myFrame.setInfo(mensaje);
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
        }

        public void atenderCliente(String nombreCliente, List<Producto> productosComprados, double montoTotal) {
            synchronized (lock) {
                ocupada = true;
                totalRecaudado += montoTotal;
                totalProductos += productosComprados.size();
                cantidadClientes++;
                String fechaHoraStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
                App.myFrame.setInfo(fechaHoraStr + " - " + nombreCliente + " atendido en " + nombre + " - Monto pagado: $ " + montoTotal + ", Cantidad comprados: " + productosComprados.size());
                ocupada = false;
            }
        }

        public void mostrarResultados() {
            App.myFrame.setInfo(nombre + " - Total recaudado: $ " + totalRecaudado + ", Cantidad vendidos: " + totalProductos + ", Clientes atendidos: " + cantidadClientes);
        }

        public String getNombre() {
            return nombre;
        }

        public int getCantidadClientes() {
            return cantidadClientes;
        }
    }
}