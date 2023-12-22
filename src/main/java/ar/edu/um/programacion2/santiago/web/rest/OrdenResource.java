package ar.edu.um.programacion2.santiago.web.rest;

import ar.edu.um.programacion2.santiago.domain.Orden;
import ar.edu.um.programacion2.santiago.repository.OrdenRepository;
import ar.edu.um.programacion2.santiago.service.AnalizarService;
import ar.edu.um.programacion2.santiago.service.ColaAhoraService;
import ar.edu.um.programacion2.santiago.service.ColaFinDiaService;
import ar.edu.um.programacion2.santiago.service.ColaPrincipioDiaService;
import ar.edu.um.programacion2.santiago.service.ObtenerService;
import ar.edu.um.programacion2.santiago.service.OrdenService;
import ar.edu.um.programacion2.santiago.service.ProcesarService;
import ar.edu.um.programacion2.santiago.service.ReportarService;
import ar.edu.um.programacion2.santiago.service.dto.OrdenDTO;
import ar.edu.um.programacion2.santiago.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing
 * {@link ar.edu.um.programacion2.santiago.domain.Orden}.
 */
@RestController
@RequestMapping("/api")
public class OrdenResource {

    private final Logger log = LoggerFactory.getLogger(OrdenResource.class);

    private static final String ENTITY_NAME = "orden";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OrdenService ordenService;

    private final OrdenRepository ordenRepository;

    public OrdenResource(OrdenService ordenService, OrdenRepository ordenRepository) {
        this.ordenService = ordenService;
        this.ordenRepository = ordenRepository;
    }

    // = = = = = = = = = = =
    // || #### ORDERS #### ||
    // = = = = = = = = = = =

    @Autowired
    ObtenerService obtenerService;

    @GetMapping("/orders/all")
    @Secured("ROLE_USER")
    public Map<String, List<Orden>> obtener() {
        log.info("GET all orders");
        Map<String, List<Orden>> obtenidas = obtenerService.obtenerOrdenes();
        return obtenidas;
    }

    // = = = = = = = = = = =
    // || #### ANALYSIS #### ||
    // = = = = = = = = = = =

    @Autowired
    AnalizarService analizarService;

    @GetMapping("/orders/analyze/{modo}")
    @Secured("ROLE_USER")
    public Map<String, List<Orden>> analyze(@PathVariable(value = "modo", required = false) final String modo) {
        if (modo != null) {
            AnalizarService.Modo modoEnum = AnalizarService.Modo.valueOf(modo.toUpperCase());

            switch (modoEnum) {
                case AHORA:
                    log.info("Analyzing orders for AHORA");
                    return analizarService.analizarOrdenes(modoEnum);

                case PRINCIPIODIA:
                    log.info("Analyzing orders for PRINCIPIODIA");
                    return analizarService.analizarOrdenes(modoEnum);

                case FINDIA:
                    log.info("Analyzing orders for FINDIA");
                    return analizarService.analizarOrdenes(modoEnum);
                default:
                    // Handle unknown mode
                    log.error("Unknown mode received: {}", modo);
                    return null;
            }
        } else {
            // Handle null mode
            log.error("Null mode received");
            return null;
        }
    }

    // = = = = = = = = = = = = =
    // || #### PROCESSING #### ||
    // = = = = = = = = = = = = =

    @Autowired
    ProcesarService procesarService;

    @GetMapping("/orders/process/{modo}")
    @Secured("ROLE_USER")
    public Map<String, List<Orden>> process(@PathVariable(value = "modo", required = false) final String modo) {
        if (modo != null) {
            ProcesarService.ModoProcesamiento modoEnum = ProcesarService.ModoProcesamiento.valueOf(modo.toUpperCase());

            switch (modoEnum) {
                case AHORA:
                    log.info("Processing orders for AHORA");
                    return procesarService.procesarOrdenes(modoEnum);

                case PRINCIPIODIA:
                    log.info("Processing orders for PRINCIPIODIA");
                    return procesarService.procesarOrdenes(modoEnum);

                case FINDIA:
                    log.info("Processing orders for FINDIA");
                    return procesarService.procesarOrdenes(modoEnum);
                default:
                    // Handle unknown mode
                    log.error("Unknown mode received: {}", modo);
                    return null;
            }
        } else {
            // Handle null mode
            log.error("Null mode received");
            return null;
        }
    }

    // = = = = = = = = = = = = = = = =
    // || #### DELETE ALL ORDERS #### ||
    // = = = = = = = = = = = = = = = =
    @DeleteMapping("/orders/delete/all")
    @Secured("ROLE_USER")
    public ResponseEntity<Void> borrarOrdenes() {
        log.info("DELETE all orders");
        ordenService.deleteAll();
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, "borrar")).build();
    }

    // = = = = = = = = = = =
    // || #### QUEUE #### ||
    // = = = = = = = = = = =

    @Autowired
    ColaAhoraService colaAhoraService;

    @Autowired
    ColaPrincipioDiaService colaPrincipioDiaService;

    @Autowired
    ColaFinDiaService colaFinDiaService;

    @GetMapping("/orders/queue/{modo}")
    @Secured("ROLE_USER")
    public List<Orden> cola(@PathVariable(value = "modo", required = false) final String modo) {
        if (modo.toUpperCase().equals("AHORA")) {
            log.info("Queue for AHORA");
            List<Orden> cola = colaAhoraService.getElementsFromQueue();
            return cola;
        } else if (modo.toUpperCase().equals("PRINCIPIODIA")) {
            log.info("Queue for PRINCIPIODIA");
            List<Orden> cola = colaPrincipioDiaService.getElementsFromQueue();
            return cola;
        } else if (modo.toUpperCase().equals("FINDIA")) {
            log.info("Queue for FINDIA");
            List<Orden> cola = colaFinDiaService.getElementsFromQueue();
            return cola;
        }
        log.error("Invalid or missing mode received: {}", modo);
        return null;
    }

    // = = = = = = = = = = =
    // || #### REPORT #### ||
    // = = = = = = = = = = =

    @Autowired
    ReportarService reportarService;

    @GetMapping("/orders/report/{modo}")
    @Secured("ROLE_USER")
    public Map<String, Object> reportar(@PathVariable(value = "modo", required = false) final String modo) {
        if (modo.toUpperCase().equals("AHORA")) {
            log.info("Reporting orders for AHORA");
            Map<String, Object> reportadas = reportarService.reportOrders(modo.toUpperCase());
            return reportadas;
        } else if (modo.toUpperCase().equals("PRINCIPIODIA")) {
            log.info("Reporting orders for PRINCIPIODIA");
            Map<String, Object> reportadas = reportarService.reportOrders(modo.toUpperCase());
            return reportadas;
        } else if (modo.toUpperCase().equals("FINDIA")) {
            log.info("Reporting orders for FINDIA");
            Map<String, Object> reportadas = reportarService.reportOrders(modo.toUpperCase());
            return reportadas;
        }
        log.error("Invalid or missing mode received: {}", modo);
        return null;
    }

    // = = = = = = = = = = = = =
    // || #### FILTERING #### ||
    // = = = = = = = = = = = = =

    @GetMapping("/orders/{processed}/filter")
    @Secured("ROLE_USER")
    public ResponseEntity<List<Orden>> findOrdenesByFilters(
            @PathVariable("processed") Integer isProcessed,
            @RequestParam(required = false) Long cliente,
            @RequestParam(required = false) Long accionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaFin) {

        boolean processed = isProcessed != 2;
        String mensajeLog = processed ? "Processed orders" : "Unprocessed orders";
        log.info(mensajeLog);

        List<Orden> ordenes = ordenService.findOrdenesByFilters(processed, cliente, accionId, fechaInicio, fechaFin);
        return ResponseEntity.ok(ordenes);
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    /**
     * {@code POST  /ordens} : Create a new orden.
     *
     * @param ordenDTO the ordenDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new ordenDTO, or with status {@code 400 (Bad Request)} if
     *         the orden has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ordens")
    public ResponseEntity<OrdenDTO> createOrden(@RequestBody OrdenDTO ordenDTO) throws URISyntaxException {
        log.debug("REST request to save Orden : {}", ordenDTO);
        if (ordenDTO.getId() != null) {
            throw new BadRequestAlertException("A new orden cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OrdenDTO result = ordenService.save(ordenDTO);
        return ResponseEntity
                .created(new URI("/api/ordens/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /ordens/:id} : Updates an existing orden.
     *
     * @param id       the id of the ordenDTO to save.
     * @param ordenDTO the ordenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated ordenDTO,
     *         or with status {@code 400 (Bad Request)} if the ordenDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the ordenDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ordens/{id}")
    public ResponseEntity<OrdenDTO> updateOrden(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody OrdenDTO ordenDTO) throws URISyntaxException {
        log.debug("REST request to update Orden : {}, {}", id, ordenDTO);
        if (ordenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ordenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ordenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        OrdenDTO result = ordenService.update(ordenDTO);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        ordenDTO.getId().toString()))
                .body(result);
    }

    /**
     * {@code PATCH  /ordens/:id} : Partial updates given fields of an existing
     * orden, field will ignore if it is null
     *
     * @param id       the id of the ordenDTO to save.
     * @param ordenDTO the ordenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated ordenDTO,
     *         or with status {@code 400 (Bad Request)} if the ordenDTO is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the ordenDTO is not found,
     *         or with status {@code 500 (Internal Server Error)} if the ordenDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/ordens/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<OrdenDTO> partialUpdateOrden(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody OrdenDTO ordenDTO) throws URISyntaxException {
        log.debug("REST request to partial update Orden partially : {}, {}", id, ordenDTO);
        if (ordenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ordenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ordenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OrdenDTO> result = ordenService.partialUpdate(ordenDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ordenDTO.getId().toString()));
    }

    /**
     * {@code GET  /ordens} : get all the ordens.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of ordens in body.
     */
    @GetMapping("/ordens")
    public List<OrdenDTO> getAllOrdens() {
        log.debug("REST request to get all Ordens");
        return ordenService.findAll();
    }

    /**
     * {@code GET  /ordens/:id} : get the "id" orden.
     *
     * @param id the id of the ordenDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the ordenDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ordens/{id}")
    public ResponseEntity<OrdenDTO> getOrden(@PathVariable Long id) {
        log.debug("REST request to get Orden : {}", id);
        Optional<OrdenDTO> ordenDTO = ordenService.findOne(id);
        return ResponseUtil.wrapOrNotFound(ordenDTO);
    }

    /**
     * {@code DELETE  /ordens/:id} : delete the "id" orden.
     *
     * @param id the id of the ordenDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ordens/{id}")
    public ResponseEntity<Void> deleteOrden(@PathVariable Long id) {
        log.debug("REST request to delete Orden : {}", id);
        ordenService.delete(id);
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
