
package lab;

import java.util.Scanner;

public class Main {

    private static final int MIN_FLOOR = 1;
    private static final int MAX_FLOOR = 6;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // สร้างลิฟต์ 2 ตัว
        Elevator elevator1 = new Elevator(1);
        Elevator elevator2 = new Elevator(1);

        // สร้าง RequestManager แยกกัน
        RequestManager manager1 = new RequestManager();
        RequestManager manager2 = new RequestManager();

        // สร้าง Controller แยกกัน
        ElevatorController controller1 =
            new ElevatorController(
                elevator1, manager1, MIN_FLOOR, MAX_FLOOR
            );

        ElevatorController controller2 =
            new ElevatorController(
                elevator2, manager2, MIN_FLOOR, MAX_FLOOR
            );

        // สร้างระบบควบคุมลิฟต์ 2 ตัว
        ElevatorSystem system =
            new ElevatorSystem(controller1, controller2);

        int choice;

        do {
        	System.out.println("=====================================");
            System.out.println("           ELEVATOR SYSTEM (Demo)          ");
            System.out.println("=====================================");
            System.out.println("1. Call Elevator");
            System.out.println("2. Select Destination");
            System.out.println("3. Move One Step");
            System.out.println("4. Auto Simulation");
            System.out.println("5. Show Elevator Status");
            System.out.println("0. Exit");
            System.out.println("=====================================");

            choice = readInt("Select menu: ");

            switch (choice) {

                case 1:
                    callElevator(system);
                    break;

                case 2:
                    selectDestination(system);
                    break;

                case 3:
                    moveOneStep(system);
                    break;

                case 4:
                    autoSimulation(system);
                    break;

                case 5:
                    printSimulation(
                        system.getElevator1(),
                        system.getElevator2()
                    );
                    break;

                case 0:
                    System.out.println("Exit Elevator System");
                    break;

                default:
                    System.out.println("Invalid menu. Try again.");
            }

        } while (choice != 0);

        scanner.close();
    }

    // เรียกลิฟต์จากด้านนอก
    private static void callElevator(ElevatorSystem system) {

        int floor = readInt("Enter your floor (1-6): ");

        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            System.out.println("Invalid floor.");
            return;
        }

        String direction;

        while (true) {
            System.out.print("Direction (U/D): ");
            direction = scanner.nextLine().trim().toUpperCase();

            if (direction.equals("U") || direction.equals("D")) {
                break;
            }

            System.out.println("Please enter U or D.");
        }

        if (floor == MAX_FLOOR && direction.equals("U")) {
            System.out.println("Cannot go UP from the top floor.");
            return;
        }

        if (floor == MIN_FLOOR && direction.equals("D")) {
            System.out.println("Cannot go DOWN from the ground floor.");
            return;
        }

        String fullDirection =
            direction.equals("U") ? "UP" : "DOWN";

        HallRequest request =
            new HallRequest(floor, fullDirection);

        system.callElevator(request);

    }

    // เลือกชั้นปลายทางภายในลิฟต์
    private static void selectDestination(ElevatorSystem system) {

        int elevatorNumber = readInt("Select Elevator (1 or 2): ");

        if (elevatorNumber != 1 && elevatorNumber != 2) {
            System.out.println("Invalid elevator number.");
            return;
        }

        int floor = readInt("Enter destination floor (1-6): ");

        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            System.out.println("Invalid floor.");
            return;
        }

        system.selectDestination(elevatorNumber, floor);
    }

    // เคลื่อนที่หนึ่งขั้น
    private static void moveOneStep(ElevatorSystem system) {

        int elevatorNumber = readInt("Select Elevator (1 or 2): ");

        ElevatorController controller;

        if (elevatorNumber == 1) {
            controller = system.getElevator1();
        } else if (elevatorNumber == 2) {
            controller = system.getElevator2();
        } else {
            System.out.println("Invalid elevator number.");
            return;
        }

        controller.step();

        printSimulation(
            system.getElevator1(),
            system.getElevator2()
        );
    }

    // จำลองการทำงานอัตโนมัติของลิฟต์ทั้งสองตัว
    private static void autoSimulation(ElevatorSystem system) {

        boolean hasWork = system.hasWork();

        if (!hasWork) {
            System.out.println("No elevator movement available.");
            return;
        }

        while (system.hasWork()) {
            system.moveElevators();

            printSimulation(
                system.getElevator1(),
                system.getElevator2()
            );

            if (!system.hasWork()) {
                break;
            }

            // หยุดเมื่อมีลิฟต์รอให้ผู้โดยสารเลือกปลายทาง
            if (system.getElevator1().isWaitingForDestinationInput()
                || system.getElevator2().isWaitingForDestinationInput()) {
                break;
            }
        }

        System.out.println("Auto Simulation finished.");
    }

    // แสดงสถานะลิฟต์ทั้งสองตัว
    private static void printSimulation(
        ElevatorController controller1,
        ElevatorController controller2
    ) {

        Elevator e1 = controller1.getElevator();
        Elevator e2 = controller2.getElevator();

        System.out.println("\n========== ELEVATOR STATUS ==========");

        for (int floor = MAX_FLOOR; floor >= MIN_FLOOR; floor--) {

            String display1 = getElevatorDisplay(e1, floor, "E1");
            String display2 = getElevatorDisplay(e2, floor, "E2");

            System.out.println(
                "Floor " + floor + " | " + display1 + " | " + display2
            );
        }

        System.out.println("-------------------------------------");

        System.out.println(
            "E1: Floor " + e1.getCurrentFloor()
            + " | Direction: " + e1.getDirection()
            + " | Door: " + (e1.isDoorOpen() ? "OPEN" : "CLOSED")
        );

        System.out.println(
            "E2: Floor " + e2.getCurrentFloor()
            + " | Direction: " + e2.getDirection()
            + " | Door: " + (e2.isDoorOpen() ? "OPEN" : "CLOSED")
        );

        System.out.println("\nE1 Destinations: "
            + controller1.getRequestManager().getDestinationFloors());

        System.out.println("E1 Hall Requests: "
            + controller1.getRequestManager().getRequestQueue());

        System.out.println("\nE2 Destinations: "
            + controller2.getRequestManager().getDestinationFloors());

        System.out.println("E2 Hall Requests: "
            + controller2.getRequestManager().getRequestQueue());

        System.out.println("=====================================");
    }

    // สร้างสัญลักษณ์แสดงตำแหน่งและทิศทาง
    private static String getElevatorDisplay(
        Elevator elevator,
        int floor,
        String name
    ) {

        if (elevator.getCurrentFloor() != floor) {
            return "       ";
        }

        if (elevator.isDoorOpen()) {
            return "[" + name + " OPEN]";
        }

        if (elevator.getDirection().equals("UP")) {
            return "[" + name + " ▲]";
        }

        if (elevator.getDirection().equals("DOWN")) {
            return "[" + name + " ▼]";
        }

        return "[" + name + " ■]";
    }

    // รับค่าตัวเลขและป้องกันการกรอกข้อมูลผิด
    private static int readInt(String message) {

        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();

            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
