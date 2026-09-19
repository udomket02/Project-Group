package lab;

public class ElevatorController {

	private final Elevator elevator;

	private final RequestManager requestManager;

	private final int minFloor;
	private final int maxFloor;

	private boolean waitingForDestinationInput;

	private boolean destinationChosenForCurrentBoarding;

	private String boardingDirection;

	public ElevatorController(Elevator elevator, RequestManager requestManager, int minFloor, int maxFloor) {

		this.elevator = elevator;

		this.requestManager = requestManager;

		this.minFloor = minFloor;

		this.maxFloor = maxFloor;

		waitingForDestinationInput = false;

		destinationChosenForCurrentBoarding = false;

		boardingDirection = "IDLE";
	}

	// =====================================================
	// เรียกลิฟต์จากด้านนอก
	// =====================================================

	public void addHallRequest(HallRequest request) {

		int floor = request.getFloor();

		String direction = request.getDirection();

		// ตรวจชั้น
		if (floor < minFloor || floor > maxFloor) {

			System.out.println("[Error] ไม่มีชั้น " + floor);

			return;
		}

		// ตรวจ UP / DOWN
		if (!direction.equals("UP") && !direction.equals("DOWN")) {

			System.out.println("[Error] ต้องเลือก UP หรือ DOWN");

			return;
		}

		// ชั้นบนสุดกด UP ไม่ได้
		if (floor == maxFloor && direction.equals("UP")) {

			System.out.println("[Error] ชั้นบนสุดไม่สามารถกด UP ได้");

			return;
		}

		// ชั้นล่างสุดกด DOWN ไม่ได้
		if (floor == minFloor && direction.equals("DOWN")) {

			System.out.println("[Error] ชั้นล่างสุดไม่สามารถกด DOWN ได้");

			return;
		}

		// เพิ่มเข้า Queue
		if (!requestManager.addHallRequest(request)) {

			System.out.println(">> ปุ่มเรียกลิฟต์ชั้น " + floor + " (" + direction + ") ถูกกดอยู่แล้ว");

			return;
		}

		System.out.println(">> เพิ่มคำขอ: " + request);

		// ถ้าลิฟต์อยู่ชั้นเดียวกันและว่าง
		if (elevator.getCurrentFloor() == floor

				&& elevator.getDirection().equals("IDLE")

				&& !elevator.isDoorOpen()

				&& !requestManager.hasDestinations()) {

			serveHallRequestAtCurrentFloor(direction);
		}
	}

	// =====================================================
	// กดเลือกชั้นจากในลิฟต์
	// =====================================================

	public boolean addDestination(int floor) {

		if (!waitingForDestinationInput || !elevator.isDoorOpen()) {

			System.out.println("[Error] ตอนนี้ยังไม่มีผู้โดยสาร" + "กำลังขึ้นลิฟต์");

			return false;
		}

		// ตรวจชั้น
		if (floor < minFloor || floor > maxFloor) {

			System.out.println("[Error] ไม่มีชั้น " + floor);

			return false;
		}

		// อยู่ชั้นนี้แล้ว
		if (floor == elevator.getCurrentFloor()) {

			System.out.println("[Error] ลิฟต์อยู่ชั้น " + floor + " อยู่แล้ว");

			return false;
		}

		// ถ้ากลุ่มนี้กด UP
		if (boardingDirection.equals("UP") && floor < elevator.getCurrentFloor()) {

			System.out.println("[Error] กด UP " + "จึงต้องเลือกชั้นที่สูงกว่า");

			return false;
		}

		// ถ้ากลุ่มนี้กด DOWN
		if (boardingDirection.equals("DOWN") && floor > elevator.getCurrentFloor()) {

			System.out.println("[Error] กด DOWN " + "จึงต้องเลือกชั้นที่ต่ำกว่า");

			return false;
		}

		/*
		 * อย่างน้อยมีคนหนึ่งคน เลือกชั้นสำหรับการขึ้นครั้งนี้แล้ว
		 */
		destinationChosenForCurrentBoarding = true;

		// ถ้าชั้นนี้ถูกกดอยู่แล้ว
		if (!requestManager.addDestination(floor)) {

			System.out.println(">> ชั้น " + floor + " ถูกเลือกอยู่แล้ว");

			return true;
		}

		System.out.println(">> เพิ่มปลายทางชั้น " + floor);

		return true;
	}

	// =====================================================
	// เดินลิฟต์ 1 Step
	// =====================================================

	public void step() {

		// เตรียมปิดประตู
		if (!prepareForMovement()) {

			return;
		}

		// =================================================
		// ถ้าลิฟต์กำลัง IDLE
		// และมีคนรออยู่ชั้นเดียวกัน
		// =================================================

		if (elevator.getDirection().equals("IDLE")) {

			String directionHere = requestManager.getFirstHallDirectionAtFloor(elevator.getCurrentFloor());

			if (!directionHere.equals("IDLE")) {

				serveHallRequestAtCurrentFloor(directionHere);

				return;
			}
		}

		// หาทิศทาง
		chooseDirection();

		// ไม่มีงาน
		if (elevator.getDirection().equals("IDLE")) {

			System.out.println(">> ไม่มีงานที่ต้องทำ");

			return;
		}

		String movingDirection = elevator.getDirection();

		int currentFloor = elevator.getCurrentFloor();

		// =================================================
		// เดินขึ้น
		// =================================================

		if (movingDirection.equals("UP")) {

			elevator.setCurrentFloor(currentFloor + 1);
		}

		// =================================================
		// เดินลง
		// =================================================

		else {

			elevator.setCurrentFloor(currentFloor - 1);
		}

		System.out.println(">> ลิฟต์มาถึงชั้น " + elevator.getCurrentFloor());

		// =================================================
		// ตรวจว่ามีคนลงชั้นนี้หรือไม่
		// =================================================

		boolean stoppedForDestination = requestManager.removeDestination(elevator.getCurrentFloor());

		if (stoppedForDestination) {

			System.out.println(">> ถึงชั้นที่ถูกเลือกไว้");
		}

		boolean pickedUp = false;

		// =================================================
		// ถ้าไม่มี Destination เหลือ
		// สามารถรับคำขอที่ชั้นนี้ได้
		// =================================================

		if (!requestManager.hasDestinations()) {

			String waitingDirection = requestManager.getFirstHallDirectionAtFloor(elevator.getCurrentFloor());

			if (!waitingDirection.equals("IDLE")) {

				pickedUp = serveHallRequestAtCurrentFloor(waitingDirection);
			}
		}

		// =================================================
		// ถ้ายังมี Destination
		// รับเฉพาะคนที่ไปทิศเดียวกัน
		// =================================================

		else {

			pickedUp = serveHallRequestAtCurrentFloor(movingDirection);
		}

		// =================================================
		// มีคนลง แต่ไม่มีคนใหม่ขึ้น
		// =================================================

		if (stoppedForDestination && !pickedUp) {

			elevator.openDoor();

			System.out.println(">> ประตูเปิดอัตโนมัติ");
		}
	}

	// =====================================================
	// เตรียมลิฟต์ก่อนออกเดินทาง
	// =====================================================

	public boolean prepareForMovement() {

		/*
		 * มีคนเพิ่งขึ้นลิฟต์ ต้องเลือกอย่างน้อย 1 ชั้นก่อน
		 */

		if (waitingForDestinationInput) {

			if (!destinationChosenForCurrentBoarding) {

				System.out.println("[Error] กรุณาเลือกชั้นปลายทาง" + "อย่างน้อย 1 ชั้น");

				return false;
			}

			waitingForDestinationInput = false;

			destinationChosenForCurrentBoarding = false;

			boardingDirection = "IDLE";

			if (elevator.isDoorOpen()) {

				elevator.closeDoor();

				System.out.println(">> ประตูปิดอัตโนมัติ");
			}

			return true;
		}

		// ประตูเปิดจากการส่งคนลง
		if (elevator.isDoorOpen()) {

			elevator.closeDoor();

			System.out.println(">> ประตูปิดอัตโนมัติ");
		}

		return true;
	}

	// =====================================================
	// เลือกทิศทางการเดิน
	// =====================================================

	private void chooseDirection() {

		int currentFloor = elevator.getCurrentFloor();

		String currentDirection = elevator.getDirection();

		// ยังมีงานข้างบน
		if (currentDirection.equals("UP") && requestManager.hasWorkAhead(currentFloor, "UP")) {

			return;
		}

		// ยังมีงานข้างล่าง
		if (currentDirection.equals("DOWN") && requestManager.hasWorkAhead(currentFloor, "DOWN")) {

			return;
		}

		// =================================================
		// ดู Destination ในลิฟต์ก่อน
		// =================================================

		if (requestManager.hasDestinations()) {

			int targetFloor = requestManager.getDestinationFloors().get(0);

			if (targetFloor > currentFloor) {

				elevator.setDirection("UP");

			} else if (targetFloor < currentFloor) {

				elevator.setDirection("DOWN");

			} else {

				elevator.setDirection("IDLE");
			}

			return;
		}

		// =================================================
		// ไม่มี Destination
		// ไปหา Hall Request
		// =================================================

		if (requestManager.hasHallRequests()) {

			HallRequest request = requestManager.peekHallRequest();

			int targetFloor = request.getFloor();

			if (targetFloor > currentFloor) {

				elevator.setDirection("UP");

			} else if (targetFloor < currentFloor) {

				elevator.setDirection("DOWN");

			} else {

				elevator.setDirection(request.getDirection());
			}

			return;
		}

		elevator.setDirection("IDLE");
	}

	// =====================================================
	// รับคนจากด้านนอก
	// =====================================================

	private boolean serveHallRequestAtCurrentFloor(String direction) {

		int currentFloor = elevator.getCurrentFloor();

		// ไม่มี Request ที่ตรง
		if (!requestManager.removeHallRequestsAtFloor(currentFloor, direction)) {

			return false;
		}

		elevator.setDirection(direction);

		elevator.openDoor();

		waitingForDestinationInput = true;

		destinationChosenForCurrentBoarding = false;

		boardingDirection = direction;

		System.out.println(">> รับคนที่รอชั้น " + currentFloor + " (" + direction + ")");

		System.out.println(">> ประตูเปิดอัตโนมัติ");

		System.out.println(">> กดข้อ 2 ได้หลายครั้ง" + "เพื่อเลือกหลายชั้น");

		return true;
	}

	// =====================================================
	// Auto Simulation
	// =====================================================

	public boolean canAutoRun() {

		// ต้องรอคนเลือกชั้นก่อน
		if (waitingForDestinationInput) {

			return false;
		}

		return requestManager.hasHallRequests()

				|| requestManager.hasDestinations()

				|| elevator.isDoorOpen();
	}

	// =====================================================
	// Getter
	// =====================================================

	public boolean isWaitingForDestinationInput() {

		return waitingForDestinationInput;
	}

	public Elevator getElevator() {

		return elevator;
	}

	public RequestManager getRequestManager() {

		return requestManager;
	}
}