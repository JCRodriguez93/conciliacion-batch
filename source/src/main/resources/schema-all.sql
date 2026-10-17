DROP TABLE IF EXISTS facturas;

CREATE TABLE facturas (
    numero_factura VARCHAR(20) NOT NULL PRIMARY KEY,
    fecha_emision DATE NOT NULL,
    importe DECIMAL(10,2) NOT NULL,
    nif_cliente VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL
);
