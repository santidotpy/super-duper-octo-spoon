package ar.edu.um.programacion2.santiago.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MainService {

    @Autowired
    ObtenerService obtenerService;

    @Autowired
    AnalizarService analizarService;

    @Autowired
    ProcesarService procesarService;

    @Autowired
    ReportarService reportarService;

    // schedule methods
    public void exeRightNow() {
        obtenerService.obtenerOrdenes();
        analizarService.analizarOrdenes(AnalizarService.Modo.AHORA);
        procesarService.procesarOrdenes(ProcesarService.ModoProcesamiento.AHORA);
        reportarService.reportOrders("AHORA");
    }

    // schedule methods diaria a la 9
    // @Scheduled(cron = "0 0 9 * * *")
    public void exeEarlyBird() {
        obtenerService.obtenerOrdenes();
        analizarService.analizarOrdenes(AnalizarService.Modo.PRINCIPIODIA);
        procesarService.procesarOrdenes(ProcesarService.ModoProcesamiento.PRINCIPIODIA);
        reportarService.reportOrders("PRINCIPIODIA");
    }

    // schedule methods diaria a la 18
    // @Scheduled(cron = "0 0 18 * * *")
    public void exeNightOwl() {
        obtenerService.obtenerOrdenes();
        analizarService.analizarOrdenes(AnalizarService.Modo.FINDIA);
        procesarService.procesarOrdenes(ProcesarService.ModoProcesamiento.FINDIA);
        reportarService.reportOrders("FINDIA");
    }
}
