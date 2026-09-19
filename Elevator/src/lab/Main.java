package lab;

import java.util.Scanner;

public class Main {

	private static final int MIN_FLOOR = 1;
	private static final int MAX_FLOOR = 6;

	public static void main(String[] args) {

		Elevator elevator = new Elevator(1);

		RequestManager requestManager = new RequestManager();

		ElevatorController controller = new ElevatorController(elevator, requestManager, MIN_FLOOR, MAX_FLOOR);

		Scanner scanner = new Scanner(System.in);

		while (true) {

			printSimulation(controller);

			System.out.println("1. เรียกลิฟต์จากด้านนอก");

			System.out.println("2. กดเลือกชั้นในลิฟต์");

			System.out.println("3. เดินหน้า 1 Step");

			System.out.println("4. Auto Simulation");

			System.out.println("0. จบโปรแกรม");

			// =================================================
			// รับเมนู
			// =================================================

			String choice;

			while (true) {

				System.out.print("เลือกคำสั่ง: ");

				choice = scanner.nextLine().trim();

				if (choice.equals("0") || choice.equals("1") || choice.equals("2") || choice.equals("3")
						|| choice.equals("4")) {

					break;
				}

				System.out.println("[Error] กรุณาเลือก 0-4");
			}

			// =================================================
			// จบโปรแกรม
			// =================================================

			if (choice.equals("0")) {

				System.out.println("ปิดระบบจำลอง");

				break;
			}

			switch (choice) {

			// =============================================
			// 1. เรียกลิฟต์จากด้านนอก
			// =============================================

			case "1":

				int floor;

				// -----------------------------
				// รับชั้น
				// -----------------------------

				while (true) {

					System.out.print("คุณอยู่ชั้นไหน (" + MIN_FLOOR + "-" + MAX_FLOOR + "): ");

					try {

						floor = Integer.parseInt(scanner.nextLine().trim());

						if (floor < MIN_FLOOR || floor > MAX_FLOOR) {

							System.out.println("[Error] กรุณาเลือกชั้น " + MIN_FLOOR + "-" + MAX_FLOOR);

							continue;
						}

						break;

					} catch (NumberFormatException e) {

						System.out.println("[Error] กรุณากรอกตัวเลข");
					}
				}

				String direction;

				// -----------------------------
				// รับ UP / DOWN
				// -----------------------------

				while (true) {

					System.out.print("กดปุ่มไหน " + "(U = ขึ้น, D = ลง): ");

					String dir = scanner.nextLine().trim().toUpperCase();

					if (dir.equals("U")) {

						if (floor == MAX_FLOOR) {

							System.out.println("[Error] ชั้นบนสุด" + "ไม่สามารถกด UP ได้");

							continue;
						}

						direction = "UP";

						break;

					} else if (dir.equals("D")) {

						if (floor == MIN_FLOOR) {

							System.out.println("[Error] ชั้นล่างสุด" + "ไม่สามารถกด DOWN ได้");

							continue;
						}

						direction = "DOWN";

						break;

					} else {

						System.out.println("[Error] กรุณากด U หรือ D");
					}
				}

				controller.addHallRequest(new HallRequest(floor, direction));

				break;

			// =============================================
			// 2. เลือกชั้นในลิฟต์
			// =============================================

			case "2":

				if (!controller.isWaitingForDestinationInput()) {

					System.out.println("[Error] ตอนนี้ยังไม่มีผู้โดยสาร" + "กำลังขึ้นลิฟต์");

					break;
				}

				while (true) {

					System.out.print("เลือกชั้นปลายทาง (" + MIN_FLOOR + "-" + MAX_FLOOR + "): ");

					int destination;

					try {

						destination = Integer.parseInt(scanner.nextLine().trim());

					} catch (NumberFormatException e) {

						System.out.println("[Error] กรุณากรอกตัวเลข");

						continue;
					}

					if (destination < MIN_FLOOR || destination > MAX_FLOOR) {

						System.out.println("[Error] กรุณาเลือกชั้น " + MIN_FLOOR + "-" + MAX_FLOOR);

						continue;
					}

					/*
					 * ถ้า Controller บอกว่า input ถูกต้องแล้ว จึงออกจาก loop
					 */

					if (controller.addDestination(destination)) {

						break;
					}
				}

				break;

			// =============================================
			// 3. Step
			// =============================================

			case "3":

				controller.step();

				break;

			// =============================================
			// 4. Auto Simulation
			// =============================================

			case "4":

				System.out.println("\n>> เริ่ม Auto Simulation\n");

				/*
				 * ถ้ายังไม่ได้เลือก Destination จะไม่ยอมออกเดินทาง
				 */

				if (!controller.prepareForMovement()) {

					break;
				}

				while (controller.canAutoRun()) {

					controller.step();

					printSimulation(controller);

					try {

						Thread.sleep(600);

					} catch (InterruptedException e) {

						Thread.currentThread().interrupt();

						break;
					}
				}

				// เจอคนรอระหว่างทาง
				if (controller.isWaitingForDestinationInput()) {

					System.out.println(">> มีคนขึ้นลิฟต์แล้ว");

					System.out.println(">> กดข้อ 2 ได้หลายครั้ง" + "เพื่อเลือกหลายชั้น");

					System.out.println(">> เมื่อเลือกครบแล้ว " + "กด 3 หรือ 4 " + "เพื่อเดินทางต่อ");

				} else {

					System.out.println(">> Auto Simulation " + "เสร็จสิ้น");
				}

				break;
			}
		}

		scanner.close();
	}

	// =====================================================
	// แสดงสถานะลิฟต์
	// =====================================================

	private static void printSimulation(ElevatorController controller) {

		Elevator elevator = controller.getElevator();

		RequestManager manager = controller.getRequestManager();

		System.out.println("\n==================================================");

		System.out.println("           ELEVATOR SIMULATION SYSTEM");

		System.out.println("==================================================");

		// =================================================
		// แสดงชั้น 6 -> 1
		// =================================================

		for (int floor = MAX_FLOOR; floor >= MIN_FLOOR; floor--) {

			String display = "   |   ";

			if (elevator.getCurrentFloor() == floor) {

				if (elevator.isDoorOpen()) {

					display = "[ OPEN ]";

				} else if (elevator.getDirection().equals("UP")) {

					display = "[E-▲]";

				} else if (elevator.getDirection().equals("DOWN")) {

					display = "[E-▼]";

				} else {

					display = "[E-■]";
				}
			}

			System.out.printf(" ชั้น %d | %s%n", floor, display);
		}

		System.out.println("--------------------------------------------------");

		System.out.printf("ทิศทาง: %s | ประตู: %s%n",

				elevator.getDirection(),

				elevator.isDoorOpen() ? "เปิดอยู่" : "ปิดสนิท");

		// =================================================
		// ArrayList
		// =================================================

		System.out.println("ชั้นที่ถูกเลือกในลิฟต์: " + manager.getDestinationFloors());

		// =================================================
		// Queue
		// =================================================

		System.out.println("Queue ผู้รอด้านนอก: " + manager.getRequestQueue());

		if (controller.isWaitingForDestinationInput()) {

			System.out.println("สถานะ: กำลังรับผู้โดยสาร " + "/ สามารถเลือกหลายชั้นได้");
		}

		System.out.println("==================================================");
	}
}