package com.mycompany.carritogrpc;

import com.tienda.grpc.CarritoServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CarritoGRPCserver extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel model;

    public CarritoGRPCserver() {
        setTitle("Servidor de Inventario (gRPC)");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        model = new DefaultTableModel(new Object[]{"ID", "Producto", "Stock"}, 0);
        tablaInventario = new JTable(model);
        add(new JScrollPane(tablaInventario));
        setVisible(true);

        new Thread(() -> {
            try {
                Server server = ServerBuilder.forPort(50051)
                        .addService(new CarritoServiceImpl(model))
                        .build();
                server.start();
                server.awaitTermination();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public static void main(String[] args) { new CarritoGRPCserver(); }
}