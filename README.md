# Proyecto Final Programacion 2

Desarrollo de un sistema de gestion de acciones.

![My Skills](https://skills.thijs.gg/icons?i=java,maven,react,docker,vscode)

## Correr el proyecto

```bash
./mvnw
```

## Endpoints
> [!NOTE]
> Modo puede ser: `ahora`, `principiodia` o `findia`

### Obtener todas las ordenes

```bash
http://localhost:8080/api/orders/all
```

### Analizar una orden de tipo ahora, principiodia o findia

```bash
http://localhost:8080/api/orders/analyze/{modo}
```

### Procesar una orden de tipo ahora, principiodia o findia

```bash
http://localhost:8080/api/orders/process/{modo}
```

### Obtener una cola de tipo ahora, principiodia o findia

```bash
http://localhost:8080/api/orders/queue/{modo}
```

### Reportar una orden de tipo ahora, principiodia o findia

```bash
http://localhost:8080/api/orders/report/{modo}
```

### Buscar una orden procesada o no procesada de tipo ahora, principiodia o findia

> [!NOTE]
> El parametro processed puede ser 1 para `true` o distinto de 1 para `false`

> [!IMPORTANT]
> Los parametros para el filtro son: `cliente`, `accionId`, `fechaInicio`, `fechaFin`

```bash
http://localhost:8080/api/orders/{processed}/filter?{parametro}={valor}
```