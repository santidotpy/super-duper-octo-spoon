package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import ar.edu.um.programacion2.santiago.service.dto.OrdenDTO;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalizarService {

    private final Logger log = LoggerFactory.getLogger(AnalizarService.class);

    @Autowired
    OrdenService ordenService;

    @Autowired
    CatedraService catedraService;

    @Autowired
    ColaAhoraService colaAhoraService;

    @Autowired
    ColaPrincipioDiaService colaPrincipioDiaService;

    @Autowired
    ColaFinDiaService colaFinDiaService;

    public void addOrdersToQueue(Modo modo) {
        if (modo == Modo.PRINCIPIODIA) {
            List<Orden> ordenes = ordenService.findOrdenesNullByModo(modo.name());
            for (Orden orden : ordenes) {
                colaPrincipioDiaService.addOrderToQueue(orden);
                log.info(colaPrincipioDiaService.queueSize() + " orders added to PRINCIPIODIA queue");
            }
        } else if (modo == Modo.FINDIA) {
            List<Orden> ordenes = ordenService.findOrdenesNullByModo(modo.name());
            for (Orden orden : ordenes) {
                colaFinDiaService.addOrderToQueue(orden);
            }
            log.info(colaFinDiaService.queueSize() + " orders added to FINDIA queue");
        } else {
            List<Orden> ordenes = ordenService.findOrdenesNullByModo(modo.name());
            for (Orden orden : ordenes) {
                colaAhoraService.addOrderToQueue(orden);
            }
            log.info(colaAhoraService.queueSize() + " orders added to AHORA queue");
        }
    }

    public boolean analizarHorario(Orden orden) {
        boolean ok = true;

        Instant instant = orden.getFechaOperacion();

        ZonedDateTime fecha = instant.atZone(ZoneId.of("UTC"));

        LocalTime horaLocal = fecha.toLocalTime();

        LocalTime horaInicio = LocalTime.of(9, 0);
        LocalTime horaFin = LocalTime.of(18, 0);

        if (horaLocal.isAfter(horaInicio) && horaLocal.isBefore(horaFin)) {
            ok = true;
        } else {
            orden.setDescripcion(orden.getDescripcion()
                    + "An order cannot be executed outside of transaction hours.");
            ok = false;
        }
        return ok;
    }

    public boolean analizarCliente(Orden orden) {
        boolean ok = false;
        JsonNode clientesNode = catedraService.getClientes();
        if (clientesNode != null && clientesNode.isArray()) {
            for (JsonNode cliente : clientesNode) {
                long clienteId = cliente.get("id").asLong();
                if (clienteId == orden.getCliente()) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                orden.setDescripcion(orden.getDescripcion() + "Client does not exist.");
            }
        }
        return ok;
    }

    public boolean analizarAccion(Orden orden) {
        boolean ok = false;
        JsonNode accionesNode = catedraService.getAcciones();
        if (accionesNode != null && accionesNode.isArray()) {
            for (JsonNode accion : accionesNode) {
                long accionId = accion.get("id").asLong();
                if (accionId == orden.getAccionId()) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                orden.setDescripcion(orden.getDescripcion() + "Stock does not exist.");
            }
        }
        return ok;
    }

    public boolean analizarCantidad(Orden orden) {
        Integer cantidadActual = null;
        boolean ok = false;
        if (orden.getCantidad() < 0) {
            orden.setDescripcion(orden.getDescripcion() + "Quantity cannot be negative.");
            return false;
        }
        JsonNode cantidadActualNode = catedraService.getCantidad(orden);
        if (cantidadActualNode != null) {
            cantidadActual = cantidadActualNode.asInt();
            if (cantidadActual >= orden.getCantidad()) {
                ok = true;
            }
        }
        if (!ok) {
            orden.setDescripcion(orden.getDescripcion() + "Quantity is not available.");
        }
        return ok;
    }

    public void cambiarPrecio(Orden orden) {
        JsonNode ultimoValorNode = catedraService.getPrecio(orden);
        if (ultimoValorNode != null) {
            Double valor = ultimoValorNode.get("valor").asDouble();
            orden.setPrecio(valor);
        }
    }

    public enum Modo {
        PRINCIPIODIA,
        FINDIA,
        AHORA
    }

    public Map<String, List<Orden>> analizarOrdenes(Modo modo) {
        log.info("Analizando ordenes con modo " + modo + " ...");

        Map<String, List<Orden>> mapaListas = new HashMap<>();
        List<Orden> aceptadas = new ArrayList<>();
        List<Orden> rechazadas = new ArrayList<>();
        addOrdersToQueue(modo);
        
        List<Orden> ordenes = getElementsInQueue(modo);
        System.out.println(ordenes);

        for (Orden orden : ordenes) {
            procesarOrden(orden, aceptadas, rechazadas, modo);
        }

        mapaListas.put("Ordenes Aceptadas", aceptadas);
        mapaListas.put("Ordenes Rechazadas", rechazadas);

        log.info(aceptadas.size() + " orders type " + modo + " accepted");
        log.info(rechazadas.size() + " orders type " + modo + " denied");

        return mapaListas;
    }

    private List<Orden> getElementsInQueue(Modo modo) {
        switch (modo) {
            case PRINCIPIODIA:
                return colaPrincipioDiaService.getElementsFromQueue();
            case FINDIA:
                return colaFinDiaService.getElementsFromQueue();
            default:
                return colaAhoraService.getElementsFromQueue();
        }
    }

    private void procesarOrden(Orden orden, List<Orden> aceptadas, List<Orden> rechazadas, Modo modo) {
        boolean condicion1 = modo != Modo.AHORA || analizarHorario(orden);
        boolean condicion2 = analizarCliente(orden);
        boolean condicion3 = analizarAccion(orden);
        boolean condicion4 = orden.getOperacion().equals("VENTA") ? analizarCantidad(orden) : true;

        if (condicion1 && condicion2 && condicion3 && condicion4) {
            orden.setAnalisis(true);
            orden.setDescripcion("Análisis Válido.");
            aceptadas.add(orden);
        } else {
            orden.setAnalisis(false);
            orden.setProcesamiento(false);
            orden.setDescripcion("Análisis Inválido. " + orden.getDescripcion());
            rechazadas.add(orden);
        }

        OrdenDTO ordenDTO = ordenService.toDTO(orden);
        ordenService.update(ordenDTO);
    }

}
