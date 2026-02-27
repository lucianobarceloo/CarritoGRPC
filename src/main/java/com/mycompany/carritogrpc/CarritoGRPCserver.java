package com.mycompany.carritogrpc;

import com.tienda.grpc.CarritoServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarritoGRPCserver {
    public static void main(String[] args) throws InterruptedException {
        // Configurar el servidor en el puerto 50051 y añadir el servicio
        Server server = ServerBuilder.forPort(50051)
                .addService(new CarritoServiceImpl())
                .build();

        try {
            server.start();
            System.out.println("Servidor de Carrito iniciado en el puerto 50051...");
            
            // Mantener el servidor funcionando hasta que se apague
            server.awaitTermination();
            
        } catch (IOException ex) {
            Logger.getLogger(CarritoGRPCserver.class.getName())
                  .log(Level.SEVERE, null, ex);
        }
    }
}