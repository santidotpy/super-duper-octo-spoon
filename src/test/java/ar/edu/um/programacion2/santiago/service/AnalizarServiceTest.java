package ar.edu.um.programacion2.santiago.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ar.edu.um.programacion2.santiago.domain.Orden;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AnalizarServiceTest {

    @InjectMocks
    @Spy
    private AnalizarService analizarService;

    @Mock
    private CatedraService catedraService;

    private JsonNode clients;
    private JsonNode stocks;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setClients();
        setStocks();
    }

    private void setClients() {
        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
        ObjectNode client = JsonNodeFactory.instance.objectNode();
        client.put("id", 1117);
        client.put("nombreApellido", "Víctor Romero");
        client.put("empresa", "Sky Jewelry");
        jsonArray.add(client);
        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        ObjectNode client_2 = JsonNodeFactory.instance.objectNode();
        client_2.put("id", 1121);
        client_2.put("nombreApellido", "Ana Medina");
        client_2.put("empresa", "Tienda vintage");
        jsonArray.add(client_2);

        clients = jsonArray;
    }

    private void setStocks() {
        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
        ObjectNode stock = JsonNodeFactory.instance.objectNode();
        stock.put("id", 11);
        stock.put("codigo", "MELI");
        stock.put("empresa", "Mercadolibre Inc.");
        jsonArray.add(stock);
        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        ObjectNode stock_2 = JsonNodeFactory.instance.objectNode();
        stock_2.put("id", 5);
        stock_2.put("codigo", "MSFT");
        stock_2.put("empresa", "Microsoft Corp.");
        jsonArray.add(stock_2);

        stocks = jsonArray;
    }

    @Test
    public void testOrderWithValidQuantity() {
        Orden order = new Orden();
        order.setCliente(1117L);
        order.setAccionId(1L);
        order.setCantidad(1);
        when(catedraService.getCantidad(order)).thenReturn(JsonNodeFactory.instance.numberNode(100));
        boolean result = analizarService.analizarCantidad(order);
        assertTrue(result);
    }

    @Test
    public void testOrderWithInvalidQuantity() {
        Orden order = new Orden();
        order.setCliente(1117L);
        order.setAccionId(1L);
        order.setCantidad(1000);
        when(catedraService.getCantidad(order)).thenReturn(JsonNodeFactory.instance.nullNode());
        boolean result = analizarService.analizarCantidad(order);
        assertFalse(result);
    }

    @Test
    public void testOrderWithNegativeQuantity() {
        Orden order = new Orden();
        order.setCliente(1117L);
        order.setAccionId(1L);
        order.setCantidad(-50);
        when(catedraService.getCantidad(order)).thenReturn(JsonNodeFactory.instance.nullNode());
        boolean result = analizarService.analizarCantidad(order);
        assertFalse(result);
    }

    @Test
    public void testOrderWithValidTime() {
        Orden order = new Orden();
        order.setModo("AHORA");
        Instant date = Instant.parse("2023-01-01T10:30:30Z");
        order.setFechaOperacion(date);
        boolean result = analizarService.analizarHorario(order);
        assertTrue(result);
    }

    @Test
    public void testOrderWithInvalidTime() {
        Orden order = new Orden();
        order.setModo("AHORA");
        Instant date = Instant.parse("2030-12-12T20:20:20Z");
        order.setFechaOperacion(date);
        boolean result = analizarService.analizarHorario(order);
        assertFalse(result);
    }

    @Test
    public void testOrderWithInvalidClient() {
        Orden order = new Orden();
        order.setCliente(118498L);
        when(catedraService.getClientes()).thenReturn(clients);
        boolean result = analizarService.analizarCliente(order);
        assertFalse(result);
    }

    @Test
    public void testOrderWithValidClient() {
        Orden order = new Orden();
        order.setCliente(1117L);
        when(catedraService.getClientes()).thenReturn(clients);
        boolean result = analizarService.analizarCliente(order);
        assertTrue(result);
    }

    @Test
    public void testOrderWithInvalidStock() {
        Orden order = new Orden();
        order.setAccionId(15L);
        when(catedraService.getAcciones()).thenReturn(stocks);
        boolean result = analizarService.analizarAccion(order);
        assertFalse(result);
    }

    @Test
    public void testOrderWithValidStock() {
        Orden order = new Orden();
        order.setAccionId(5L);
        when(catedraService.getAcciones()).thenReturn(stocks);
        boolean result = analizarService.analizarAccion(order);
        assertTrue(result);
    }
}
