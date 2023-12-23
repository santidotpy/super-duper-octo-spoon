package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import ar.edu.um.programacion2.santiago.service.dto.OrdenDTO;
import ar.edu.um.programacion2.santiago.service.dto.OrdenesDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
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
public class ObtenerService {

    private final Logger log = LoggerFactory.getLogger(ObtenerService.class);

    @Autowired
    OrdenService ordenService;

    @Autowired
    CatedraService catedraService;

    public List<Orden> formatResponse(HttpResponse<String> jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            OrdenesDTO response = objectMapper.readValue(jsonResponse.body(), OrdenesDTO.class); // convierte lo de la respuesta en un objeto de tipo OrdenesDTO
            List<Orden> orders = response.getOrdenes();
            return orders;
        } catch (IOException e) {
            // e.printStackTrace();
            return Collections.emptyList(); // return empty list if error
        }
    }

    public void saveToDB(List<Orden> orders) {
        for (Orden orden : orders) {
            orden.setDescripcion("");
            OrdenDTO ordenDTO = ordenService.toDTO(orden);
            ordenService.save(ordenDTO);
        }
    }

    public Map<String, List<Orden>> obtenerOrdenes() {
        log.info("Getting orders...");
        HttpResponse<String> jsonResponse = catedraService.getOrdenes();
        List<Orden> orders = formatResponse(jsonResponse);
        saveToDB(orders);
        log.info(orders.size() + " orders recovered");
        Map<String, List<Orden>> mapaLista = new HashMap<>();
        mapaLista.put("Orders Recovered", orders);
        return mapaLista;
    }
}
