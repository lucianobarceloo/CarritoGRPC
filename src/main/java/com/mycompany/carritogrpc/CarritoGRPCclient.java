package com.mycompany.carritogrpc;

import com.tienda.grpc.CarritoRequest;
import com.tienda.grpc.CarritoResponse;
import com.tienda.grpc.CarritoServiceGrpc;
import com.tienda.grpc.Producto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CarritoGRPCclient {
    public static void main(String[] args) {
        // 1. Crear el canal de comunicación hacia el servidor (localhost:50051)
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // Sin cifrado para pruebas locales
                .build();

        // 2. Crear el Stub para realizar la llamada al servicio
        CarritoServiceGrpc.CarritoServiceBlockingStub stub = CarritoServiceGrpc.newBlockingStub(channel);

        // 3. Crear productos de prueba usando el Builder de Protobuf
        Producto p1 = Producto.newBuilder()
                .setId("1")
                .setNombre("Monitor Gamer")
                .setPrecio(4500.0)
                .setCantidad(1)
                .build();

        Producto p2 = Producto.newBuilder()
                .setId("2")
                .setNombre("Teclado Mecánico")
                .setPrecio(1200.0)
                .setCantidad(2)
                .build();

        // 4. Construir la petición (CarritoRequest) con los productos
        CarritoRequest request = CarritoRequest.newBuilder()
                .setUsuarioId("Sebastian_Moreno")
                .addItems(p1)
                .addItems(p2)
                .build();

        // 5. Enviar la petición y recibir la respuesta del servidor
        System.out.println("Enviando productos al servidor gRPC...");
        CarritoResponse response = stub.procesarCarrito(request);

        // 6. Imprimir los resultados en la consola de NetBeans
        System.out.println("===========================================");
        System.out.println("RESPUESTA DEL SERVIDOR:");
        System.out.println("Transacción ID: " + response.getTransaccionId());
        System.out.println("Estado: " + response.getEstado());
        System.out.println("Subtotal: $" + response.getTotalNeto());
        System.out.println("IVA (16%): $" + response.getImpuestos());
        System.out.println("TOTAL A PAGAR: $" + response.getTotalPagar());
        System.out.println("===========================================");

        // 7. Cerrar el canal
        channel.shutdown();
    }
}