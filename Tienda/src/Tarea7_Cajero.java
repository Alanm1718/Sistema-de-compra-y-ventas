package com.mycompany.tarea7_cajero;

import java.util.Scanner;

public class Tarea7_Cajero {

    // Lee un monto; devuelve -1 si lo que se escribió no es un número
    static long leerMonto(Scanner teclado) {
        if (!teclado.hasNextLong()) {
            teclado.next();
            return -1;
        }
        return teclado.nextLong();
    }

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        long dinero = 500000;   // Saldo inicial: Gs. 500.000
        int op;

        do {
            System.out.println("\n--- MENÚ DEL CAJERO ---");
            System.out.println("1. Consultar saldo");
            System.out.println("2. Depositar");
            System.out.println("3. Retirar");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            if (!teclado.hasNextInt()) {
                System.out.println("Debe ingresar un número del 1 al 4.");
                teclado.next();
                op = 0;
                continue;
            }
            op = teclado.nextInt();

            switch (op) {
                case 1:
                    System.out.printf("Saldo disponible: Gs. %,d%n", dinero);
                    break;

                case 2:
                    System.out.print("Monto a depositar: Gs. ");
                    long ingreso = leerMonto(teclado);
                    if (ingreso <= 0) {
                        System.out.println("Monto inválido, debe ser mayor a cero.");
                    } else {
                        dinero += ingreso;
                        System.out.printf("Depósito realizado. Saldo actual: Gs. %,d%n", dinero);
                    }
                    break;

                case 3:
                    System.out.print("Monto a retirar: Gs. ");
                    long egreso = leerMonto(teclado);
                    if (egreso <= 0) {
                        System.out.println("Monto inválido, debe ser mayor a cero.");
                    } else if (egreso > dinero) {
                        System.out.printf("Fondos insuficientes. Saldo disponible: Gs. %,d%n", dinero);
                    } else {
                        dinero -= egreso;
                        System.out.printf("Retiro realizado. Saldo actual: Gs. %,d%n", dinero);
                    }
                    break;

                case 4:
                    System.out.println("Gracias por usar el cajero. ¡Hasta luego!");
                    break;

                default:
                    System.out.println("Opción no válida, elija del 1 al 4.");
            }
        } while (op != 4);

        teclado.close();
    }
}