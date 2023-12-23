
package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import ar.edu.um.programacion2.santiago.service.dto.OrdenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProcesarService {

    private final Logger log = LoggerFactory.getLogger(ProcesarService.class);

    @Autowired
    OrdenService ordenService;

    @Autowired
    ColaAhoraService colaAhoraService;

    @Autowired
    ColaPrincipioDiaService colaPrincipioDiaService;

    @Autowired
    ColaFinDiaService colaFinDiaService;

    public boolean comprar(Orden orden) {
        orden.setProcesamiento(true);
        orden.setDescripcion(orden.getDescripcion() + " Compra Exitosa.");
        log.info("Processing Purchase");
        return true;
    }

    public boolean vender(Orden orden) {
        orden.setProcesamiento(true);
        orden.setDescripcion(orden.getDescripcion() + " Venta Exitosa.");
        log.info("Processing Sale");
        return true;
    }

    private List<Orden> procesarOrdenesEnCola(List<Orden> ordenes) {
        List<Orden> procesadas = new ArrayList<>();
        List<Orden> noProcesadas = new ArrayList<>();

        for (Orden orden : ordenes) {
            if (orden.getAnalisis()) {
                if (orden.getOperacion().equals("COMPRA")) {
                    if (comprar(orden)) {
                        procesadas.add(orden);
                    } else {
                        noProcesadas.add(orden);
                    }
                } else {
                    if (vender(orden)) {
                        procesadas.add(orden);
                    } else {
                        noProcesadas.add(orden);
                    }
                }
                OrdenDTO ordenDTO = ordenService.toDTO(orden);
                ordenService.update(ordenDTO);
            }
        }

        return noProcesadas;
    }

    public Map<String, List<Orden>> procesarOrdenes(ModoProcesamiento modo) {
        log.info("Processing orders with mode " + modo);
        Map<String, List<Orden>> mapaListas = new HashMap<>();

        List<Orden> procesadas = new ArrayList<>();
        List<Orden> noProcesadas;

        switch (modo) {
            case PRINCIPIODIA:
                noProcesadas = procesarOrdenesEnCola(colaPrincipioDiaService.getElementsFromQueue());
                break;
            case FINDIA:
                noProcesadas = procesarOrdenesEnCola(colaFinDiaService.getElementsFromQueue());
                break;
            default:
                noProcesadas = procesarOrdenesEnCola(colaAhoraService.getElementsFromQueue());
        }

        procesadas.addAll(noProcesadas); // Todas las ordenes no procesadas también se consideran procesadas

        mapaListas.put("Ordenes Procesadas", procesadas);
        mapaListas.put("Ordenes No Procesadas", noProcesadas);

        log.info(procesadas.size() + " ordenes con modo " + modo + " procesadas");
        log.info(noProcesadas.size() + " ordenes con modo " + modo + " no procesadas");

        return mapaListas;
    }

    public enum ModoProcesamiento {
        PRINCIPIODIA,
        FINDIA,
        AHORA
    }
}
