package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatedraService {

    private final Logger log = LoggerFactory.getLogger(CatedraService.class);

    public String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzZ3JhZmZpZ25hIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTczMDM5ODc5MX0.PY7PVosPTcsiHT4Fg-5oL_R0HIPptxzh2qNmoDn3zbmnNlWa3tPzvRd70t6efMTlAkjwxjInZaKzSXAsDnXPog";
    public String url_report = "http://192.168.194.254:8000/api/reporte-operaciones/reportar";
    public String url_qty = "http://192.168.194.254:8000/api/reporte-operaciones/consulta_cliente_accion?";
    public String url_stocks = "http://192.168.194.254:8000/api/acciones/";
    public String url_clients = "http://192.168.194.254:8000/api/clientes/";
    public String url_orders = "http://192.168.194.254:8000/api/ordenes/ordenes";
    public String url_last_value = "http://192.168.194.254:8000/api/acciones/ultimovalor/";


    // get ordenes de la catedra
    public HttpResponse<String> getOrdenes() {
        String uri = url_orders;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).header("Authorization", "Bearer " + token)
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("GET " + uri + " : " + response.statusCode());

            return response;
        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
            return null;
        }
    }

    // get clientes de la catedra
    public JsonNode getClientes() {
        HttpClient client = HttpClient.newHttpClient(); // Crear un cliente HTTP
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url_clients)) // Crear una solicitud HTTP
                .header("Authorization", "Bearer " + token).build();
        JsonNode clientesJsonNode;

        try { // Enviar la solicitud HTTP y obtener una respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("GET " + url_clients + " : " + response.statusCode());
            String jsonResponse = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            clientesJsonNode = jsonNode.get("clientes");
        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
            clientesJsonNode = null;
        }
        return clientesJsonNode;
    }

    // get acciones de la catedra
    public JsonNode getAcciones() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url_stocks))
                .header("Authorization", "Bearer " + token).build();
        JsonNode stocksJsonNode;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("GET " + url_stocks + " : " + response.statusCode());
            String jsonResponse = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            stocksJsonNode = jsonNode.get("acciones");
        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
            stocksJsonNode = null;
        }
        return stocksJsonNode;
    }

    public JsonNode getCantidad(Orden orden) {
        JsonNode qtyJsonNode;
        String url = url_qty + "clienteId=" + orden.getCliente() + "&accionId=" + orden.getAccionId();
        System.out.println("getCantidad" + orden.getCliente());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + token)
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("GET " + url + " : " + response.statusCode());
            String jsonResponse = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            qtyJsonNode = jsonNode.get("cantidadActual");
        } catch (IOException | InterruptedException e) {
            qtyJsonNode = null;
        }
        return qtyJsonNode;
    }

    public JsonNode getPrecio(Orden orden) {
        JsonNode lastValueJsonNode;
        String simbolo = orden.getAccion();
        String uri = url_last_value + simbolo;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).header("Authorization", "Bearer " + token)
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("GET " + url_last_value + simbolo + " : " + response.statusCode());
            String jsonResponse = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            lastValueJsonNode = jsonNode.get("ultimoValor");
        } catch (IOException | InterruptedException e) {
            lastValueJsonNode = null;
            // e.printStackTrace();
        }

        return lastValueJsonNode;
    }

    public HttpResponse<String> postOrdenes(Map<String, ArrayNode> data) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] jsonBytes = new byte[0];

        try {
            // Serializar el mapa de datos a una cadena JSON
            jsonBytes = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Crear un cuerpo de solicitud a partir de los bytes JSON
        BodyPublisher body = BodyPublishers.ofByteArray(jsonBytes);

        // Configurar y enviar el POST
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(url_report))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(body)
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
