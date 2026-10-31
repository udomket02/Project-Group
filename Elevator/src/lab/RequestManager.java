
package lab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class RequestManager {

	// Queue เก็บคำขอจากด้านนอก
	private final Queue<HallRequest> requestQueue;

	// ArrayList เก็บชั้นที่ถูกกดจากด้านใน
	private final ArrayList<Integer> destinationFloors;

	public RequestManager() {
		requestQueue = new LinkedList<>();
		destinationFloors = new ArrayList<>();
	}

	// =====================================================
	// เพิ่ม Hall Request
	// =====================================================

	public boolean addHallRequest(HallRequest request) {

		if (hasSameHallRequest(request.getFloor(), request.getDirection())) {
			return false;
		}

		requestQueue.offer(request);
		return true;
	}

	// =====================================================
	// ตรวจ Hall Request ซ้ำ (Linear Search)
	// =====================================================

	public boolean hasSameHallRequest(int floor, String direction) {

		for (HallRequest request : requestQueue) {

			if (request.getFloor() == floor && request.getDirection().equals(direction)) {
				return true;
			}
		}

		return false;
	}

	// =====================================================
	// เพิ่มชั้นจากด้านในลิฟต์ (Linear Search)
	// =====================================================

	public boolean addDestination(int floor) {

		// ค้นหาว่าชั้นนี้มีอยู่แล้วหรือไม่
		for (int existingFloor : destinationFloors) {

			if (existingFloor == floor) {
				return false;
			}
		}

		destinationFloors.add(floor);
		return true;
	}

	// =====================================================
	// Insertion Sort เรียงชั้นปลายทาง
	// UP = น้อยไปมาก
	// DOWN = มากไปน้อย
	// =====================================================

	public void sortDestinations(String direction) {

		for (int i = 1; i < destinationFloors.size(); i++) {

			int key = destinationFloors.get(i);
			int j = i - 1;

			// เรียงจากน้อยไปมาก
			if (direction.equals("UP")) {

				while (j >= 0 && destinationFloors.get(j) > key) {

					destinationFloors.set(j + 1, destinationFloors.get(j));
					j--;
				}
			}

			// เรียงจากมากไปน้อย
			else if (direction.equals("DOWN")) {

				while (j >= 0 && destinationFloors.get(j) < key) {

					destinationFloors.set(j + 1, destinationFloors.get(j));
					j--;
				}
			}

			destinationFloors.set(j + 1, key);
		}
	}

	// =====================================================
	// ลบชั้นเมื่อเดินทางถึง
	// =====================================================

	public boolean removeDestination(int floor) {

		return destinationFloors.remove(Integer.valueOf(floor));
	}

	// =====================================================
	// มี Destination หรือไม่
	// =====================================================

	public boolean hasDestinations() {

		return !destinationFloors.isEmpty();
	}

	// =====================================================
	// มี Hall Request หรือไม่
	// =====================================================

	public boolean hasHallRequests() {

		return !requestQueue.isEmpty();
	}

	// =====================================================
	// ดูหัว Queue
	// =====================================================

	public HallRequest peekHallRequest() {

		return requestQueue.peek();
	}

	// =====================================================
	// ดูว่าชั้นนี้มีคนกด UP หรือ DOWN หรือไม่
	// =====================================================

	public String getFirstHallDirectionAtFloor(int floor) {

		for (HallRequest request : requestQueue) {

			if (request.getFloor() == floor) {
				return request.getDirection();
			}
		}

		return "IDLE";
	}

	// =====================================================
	// ลบ Hall Request ที่ลิฟต์รับไปแล้ว
	// =====================================================

	public boolean removeHallRequestsAtFloor(int floor, String direction) {

		int queueSize = requestQueue.size();
		boolean found = false;

		for (int i = 0; i < queueSize; i++) {

			HallRequest request = requestQueue.poll();

			if (request.getFloor() == floor && request.getDirection().equals(direction)) {

				found = true;

			} else {

				requestQueue.offer(request);
			}
		}

		return found;
	}

	// =====================================================
	// ตรวจว่าข้างหน้ายังมีงานหรือไม่
	// =====================================================

	public boolean hasWorkAhead(int currentFloor, String direction) {

		// ตรวจชั้นที่กดจากในลิฟต์
		for (int floor : destinationFloors) {

			if (direction.equals("UP") && floor > currentFloor) {
				return true;
			}

			if (direction.equals("DOWN") && floor < currentFloor) {
				return true;
			}
		}

		// ตรวจ Queue ด้านนอก
		for (HallRequest request : requestQueue) {

			if (!request.getDirection().equals(direction)) {
				continue;
			}

			if (direction.equals("UP") && request.getFloor() > currentFloor) {
				return true;
			}

			if (direction.equals("DOWN") && request.getFloor() < currentFloor) {
				return true;
			}
		}

		return false;
	}

	// =====================================================
	// Getter
	// =====================================================

	public Queue<HallRequest> getRequestQueue() {

		return requestQueue;
	}

	public ArrayList<Integer> getDestinationFloors() {

		return destinationFloors;
	}
}
