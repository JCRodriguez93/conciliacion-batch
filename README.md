# conciliacion-batch

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 4.1.1](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-batch)
[![H2](https://img.shields.io/badge/H2-embebida-1021FF?style=for-the-badge)](https://www.h2database.com/)
[![Maven 3.9](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

Proceso Spring Batch autónomo que **concilia facturas contra pagos**: lee dos CSV, valida, compara importes y marca el estado final de cada factura en una base H2. Arranca con `java -jar`, ejecuta el job y termina solo. Los datos quedan persistidos en un fichero H2 (`source/data/`) que puedes inspeccionar después. Verificado con un test de integración de punta a punta.

---

## Origen: generado con Scafftrix y adaptado

Este proyecto **empezó siendo el que generó [Scafftrix](https://github.com/JCRodriguez93/Scafftrix)** desde su perfil `SPRING_BATCH` (un job de importación de empleados). El esqueleto de batch —reader, processor, writer, fault tolerance y job repository— se mantuvo intacto; lo que cambió fue el dominio: de empleados a facturas y pagos.

Adaptar el template destapó errores tanto míos como de la propia fábrica. Corregí en [Scafftrix](https://github.com/JCRodriguez93/Scafftrix) dos bugs que se habrían repetido en cada generación: faltaba `spring-boot-starter-batch-jdbc` (sin él, Boot usa un `ResourcelessJobRepository` y no persiste las tablas `BATCH_*`), y el *resource filtering* de Spring Boot horneaba `scafftrix` en los `application.*` de las plantillas. Todo está documentado en el post-mortem del portfolio: [Errores al adaptar un batch de ejemplo](https://jcrrodriguez93.github.io/errores-batch/).

## Qué hace el job

Un job, `conciliacionJob`, con **dos steps** encadenados, chunk-oriented de 3 en 3:

| Step | Componente | Qué hace |
|---|---|---|
| `stepFacturas` | `FlatFileItemReader` | Lee `facturas.csv` (7 facturas) |
| | `FacturaProcessor` | Descarta importes `<= 0` y fechas mal formadas |
| | `JdbcBatchItemWriter` | Inserta en `facturas` como `PENDIENTE` |
| `stepPagos` | `FlatFileItemReader` | Lee `pagos.csv` (6 pagos) |
| | `PagoConciliadorProcessor` | Busca la factura, compara importes con `BigDecimal` y decide el estado |
| | `JdbcBatchItemWriter` | `UPDATE` del estado de la factura |
| ambos | Skip policy | `.skip(FlatFileParseException.class).skipLimit(5)`: una fila rota no mata el lote |
| job | `JobCompletionNotificationListener` | Al finalizar en `COMPLETED`, resume los resultados agrupados por estado |

## Dónde viven los datos (los writers)

No hay una clase `*Writer.java`: los dos writers son **`@Bean` dentro de `config/BatchConfig.java`**, que es donde se ensambla todo el job (readers, processors, writers, steps y job):

- `writerFacturas` → `JdbcBatchItemWriter` con un `INSERT` en la tabla `facturas`.
- `writerPagos` → `JdbcBatchItemWriter` con un `UPDATE facturas SET estado = ...`.

Ambos escriben contra el `DataSource` de la aplicación (H2). El resultado es **persistente**: tras ejecutar el job, la tabla `facturas` contiene las 5 facturas con su estado final, y las tablas `BATCH_*` guardan el historial de ejecuciones del job. La app usa H2 en fichero (`source/data/`, en `.gitignore`), así que puedes abrir la base después y verlo tú mismo.

## Salida real

Con los datos de muestra, una ejecución se ve así:

```text
WARN  e.s.c.processor.FacturaProcessor      : Factura descartada por importe inválido: FAC-2026-0005
WARN  e.s.c.processor.FacturaProcessor      : Factura descartada por fecha de emisión inválida: FAC-2026-0007
INFO  e.s.c.p.PagoConciliadorProcessor      : Factura FAC-2026-0001: pago 1250.00 vs factura 1250.00 -> CONCILIADA
INFO  e.s.c.p.PagoConciliadorProcessor      : Factura FAC-2026-0002: pago 800.50 vs factura 890.50 -> IMPORTE_NO_COINCIDE
INFO  e.s.c.p.PagoConciliadorProcessor      : Factura FAC-2026-0003: pago 450.00 vs factura 450.00 -> CONCILIADA
INFO  e.s.c.p.PagoConciliadorProcessor      : Factura FAC-2026-0004: pago 2100.75 vs factura 2100.75 -> CONCILIADA
WARN  e.s.c.p.PagoConciliadorProcessor      : Pago ignorado: no existe la factura FAC-2026-0009
INFO  .s.c.l.JobCompletionNotificationListener :   CONCILIADA: 3 factura(s)
INFO  .s.c.l.JobCompletionNotificationListener :   IMPORTE_NO_COINCIDE: 1 factura(s)
INFO  .s.c.l.JobCompletionNotificationListener :   PENDIENTE: 1 factura(s)
INFO  .s.c.l.JobCompletionNotificationListener :   TOTAL: 5 factura(s) en la base
INFO  o.s.b.c.l.s.TaskExecutorJobLauncher   : ... status: [COMPLETED] in 266ms
```

De las 7 facturas del CSV: **5 escritas**, 2 descartadas por el processor (importe negativo y fecha inválida). De los 6 pagos: 4 concilian, 1 no coincide y 1 apunta a una factura inexistente.

## Cómo ejecutarlo

Requisitos: **Java 17+** y Maven 3.9+.

```bash
cd source
mvn -DskipTests package
java -jar target/conciliacion-batch-1.0.0-SNAPSHOT.jar
```

Tras la ejecución, los datos quedan en `source/data/conciliacion.mv.db`. Puedes inspeccionarlos con el shell de H2 (o cualquier cliente JDBC) contra `jdbc:h2:file:./data/conciliacion`:

```text
== Tabla facturas ==
FAC-2026-0001 | 1250.00    | CONCILIADA
FAC-2026-0002 | 890.50     | IMPORTE_NO_COINCIDE
FAC-2026-0003 | 450.00     | CONCILIADA
FAC-2026-0004 | 2100.75    | CONCILIADA
FAC-2026-0006 | 330.00     | PENDIENTE
```

Cada ejecución arranca de cero (el `schema-all.sql` recrea la tabla), así que el fichero siempre refleja la última corrida.

### Tests

```bash
cd source
mvn test
```

`ConciliacionBatchIntegrationTest` lanza el job real sobre el CSV de muestra y verifica con assertions que quedan **5 facturas** repartidas en **3 `CONCILIADA` / 1 `IMPORTE_NO_COINCIDE` / 1 `PENDIENTE`**.

## Estructura

| Ruta | Qué es |
|---|---|
| `source/src/main/java/.../config/BatchConfig.java` | Definición de los dos steps y del job |
| `source/src/main/java/.../processor/` | `FacturaProcessor` y `PagoConciliadorProcessor` |
| `source/src/main/java/.../model/` | `Factura`, `Pago` y `ConciliacionResult` |
| `source/src/main/resources/facturas.csv` | CSV de entrada (7 filas, 2 inválidas a propósito) |
| `source/src/main/resources/pagos.csv` | CSV de pagos (6 filas, 1 huérfana y 1 inválida) |
| `source/src/main/resources/schema-all.sql` | Tabla `facturas` |
| `source/src/test/java/.../ConciliacionBatchIntegrationTest.java` | Test de integración de punta a punta |

## Limitaciones y siguientes pasos

- Las rutas de los CSV están fijadas en el classpath; lo siguiente es hacerlas configurables por parámetro.
- H2 en fichero es perfecto para demo, pero para producción convendría un motor real (PostgreSQL con Docker) y un volumen de datos que hoy no hay.
- `stepPagos` consulta la factura por cada pago; con volumen real conviene un *batch read* en lugar de N consultas.
- Sin programación de ejecuciones: un conciliador real no se lanza a mano.
