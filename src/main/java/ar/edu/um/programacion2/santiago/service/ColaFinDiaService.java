package ar.edu.um.programacion2.santiago.service;

import ar.edu.um.programacion2.santiago.domain.Orden;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ColaFinDiaService {

    private Queue<Orden> cola = new LinkedList<>();

    // add elements to the queue
    public void addOrderToQueue(Orden orden) {
        cola.add(orden);
    }

    // remove element from the queue, it returns the removed element
    public Orden removeOrderFromQueue() {
        return cola.poll();
    }

    // check if the queue is empty
    public boolean isQueueEmpty() {
        return cola.isEmpty();
    }

    // get the size of the queue
    public int queueSize() {
        return cola.size();
    }

    // get all elements from the queue
    public List<Orden> getElementsFromQueue() {
        List<Orden> listaOrdenes = new ArrayList<>(cola);
        return listaOrdenes;
    }
}
