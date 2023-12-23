package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportarService {

    private final Logger log = LoggerFactory.getLogger(ReportarService.class);

    @Autowired
    ColaPrincipioDiaService colaPrincipioDiaService;

    @Autowired
    ColaFinDiaService colaFinDiaService;

    @Autowired
    ColaAhoraService colaAhoraService;

    @Autowired
    CatedraService catedraService;

    @Autowired
    OrdenService ordenService;

    public String url_report = "http://192.168.194.254:8000/api/reporte-operaciones/reportar";

    public Map<String, Object> reportOrders(String modo) {
        log.info("Reporting orders of type: " + modo + " ...");
        Map<String, ArrayNode> json = new HashMap<>();

        ArrayNode orders = JsonNodeFactory.instance.arrayNode();

        if (modo.equals("AHORA")) {
            while (!colaAhoraService.isQueueEmpty()) { // while queue is not empty
                Orden orden = colaAhoraService.removeOrderFromQueue(); // removeOrderFromQueue() returns null if queue
                                                                       // is empty
                JsonNode ordenJson = ordenService.toJson(orden); // toJson() returns null if orden is null
                orders.add(ordenJson); // add() does nothing if ordenJson is null
            }
        }

        else if (modo.equals("PRINCIPIODIA")) {
            while (!colaPrincipioDiaService.isQueueEmpty()) {
                Orden orden = colaPrincipioDiaService.removeOrderFromQueue();
                JsonNode ordenJson = ordenService.toJson(orden);
                orders.add(ordenJson);
            }
        } else if (modo.equals("FINDIA")) {
            while (!colaFinDiaService.isQueueEmpty()) {
                Orden orden = colaFinDiaService.removeOrderFromQueue();
                JsonNode ordenJson = ordenService.toJson(orden);
                orders.add(ordenJson);
            }
        } else {
            log.info("Unknown mode, please try again.");
            return null;
        }

        json.put("ordenes", orders);

        HttpResponse<String> response = catedraService.postOrdenes(json);

        log.info("POST " + url_report + " : " + response.body());

        log.info(orders.size() + " orders of type " + modo + " reported");

        Map<String, Object> report = new HashMap<>();

        report.put("ordenes", orders);

        report.put("respuesta", response.body());

        return report;
    }
}
