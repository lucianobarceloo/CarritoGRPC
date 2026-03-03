package com.tienda.grpc;

import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.table.DefaultTableModel;

public class CarritoServiceImpl extends CarritoServiceGrpc.CarritoServiceImplBase {
    private static final Map<String, Integer> inventario = new ConcurrentHashMap<>();
    private static final Map<String, Double> precios = new HashMap<>();
    private static final Map<String, String> nombres = new HashMap<>();
    private DefaultTableModel tableModel;

    public CarritoServiceImpl(DefaultTableModel model) {
        this.tableModel = model;
        // Inventario inicial
        inicializarProducto("1", "Monitor Gamer", 4500.0, 10);
        inicializarProducto("2", "Teclado Mecánico", 1200.0, 20);
        inicializarProducto("3", "Mouse Pro", 800.0, 15);
    }

    private void inicializarProducto(String id, String nombre, double precio, int stock) {
        inventario.put(id, stock);
        precios.put(id, precio);
        nombres.put(id, nombre);
        tableModel.addRow(new Object[]{id, nombre, stock});
    }

    @Override
    public void obtenerCatalogo(EmptyRequest request, StreamObserver<CatalogoResponse> responseObserver) {
        CatalogoResponse.Builder response = CatalogoResponse.newBuilder();
        nombres.forEach((id, nombre) -> {
            response.addProductos(Producto.newBuilder()
                .setId(id).setNombre(nombre).setPrecio(precios.get(id)).build());
        });
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void procesarCarrito(CarritoRequest request, StreamObserver<CarritoResponse> responseObserver) {
        // Validar carrito vacío
        if (request.getItemsCount() == 0) {
            finalizarConError(responseObserver, "ERROR: Carrito vacío");
            return;
        }

        // Validar stock y valores positivos (sin descontar todavía)
        for (Producto p : request.getItemsList()) {
            if (p.getCantidad() <= 0 || p.getPrecio() <= 0) {
                finalizarConError(responseObserver, "ERROR: Cantidad o precio inválido");
                return;
            }
            if (inventario.getOrDefault(p.getId(), 0) < p.getCantidad()) {
                finalizarConError(responseObserver, "ERROR: Stock insuficiente para " + p.getNombre());
                return;
            }
        }

        // Procesar descuento de inventario
        double subtotal = 0;
        for (Producto p : request.getItemsList()) {
            int nuevoStock = inventario.get(p.getId()) - p.getCantidad();
            inventario.put(p.getId(), nuevoStock);
            subtotal += p.getPrecio() * p.getCantidad();
            actualizarFilaInterfaz(p.getId(), nuevoStock);
        }

        double impuestos = subtotal * 0.16;
        CarritoResponse res = CarritoResponse.newBuilder()
                .setTransaccionId(UUID.randomUUID().toString())
                .setEstado("EXITOSO")
                .setTotalNeto(subtotal)
                .setImpuestos(impuestos)
                .setTotalPagar(subtotal + impuestos)
                .build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    private void finalizarConError(StreamObserver<CarritoResponse> obs, String msg) {
        obs.onNext(CarritoResponse.newBuilder().setEstado(msg).build());
        obs.onCompleted();
    }

    private void actualizarFilaInterfaz(String id, int stock) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(id)) {
                tableModel.setValueAt(stock, i, 2);
                break;
            }
        }
    }
}