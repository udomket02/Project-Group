
package lab;

public class ElevatorSystem {

    private final ElevatorController elevator1;
    private final ElevatorController elevator2;

    public ElevatorSystem(ElevatorController elevator1,
                          ElevatorController elevator2) {
        this.elevator1 = elevator1;
        this.elevator2 = elevator2;
    }

    // เลือกลิฟต์ที่อยู่ใกล้ชั้นที่เรียกมากที่สุด
    public void callElevator(HallRequest request) {

        int floor = request.getFloor();

        int distance1 = Math.abs(
            elevator1.getElevator().getCurrentFloor() - floor
        );

        int distance2 = Math.abs(
            elevator2.getElevator().getCurrentFloor() - floor
        );

        if (distance1 <= distance2) {
            System.out.println("ระบบเลือก ELEVATOR 1");
            elevator1.addHallRequest(request);
        } else {
            System.out.println("ระบบเลือก ELEVATOR 2");
            elevator2.addHallRequest(request);
        }
    }

    // เลือกชั้นปลายทางของลิฟต์
    public void selectDestination(int elevatorNumber, int floor) {

        if (elevatorNumber == 1) {
            elevator1.addDestination(floor);
        } else if (elevatorNumber == 2) {
            elevator2.addDestination(floor);
        } else {
            System.out.println("ไม่พบหมายเลขลิฟต์");
        }
    }

    // สั่งให้ลิฟต์แต่ละตัวเคลื่อนที่หนึ่งขั้น
    public void moveElevators() {

        if (elevator1.canAutoRun()) {
            elevator1.step();
        }

        if (elevator2.canAutoRun()) {
            elevator2.step();
        }
    }

    public boolean hasWork() {
        return elevator1.canAutoRun() || elevator2.canAutoRun();
    }

    public ElevatorController getElevator1() {
        return elevator1;
    }

    public ElevatorController getElevator2() {
        return elevator2;
    }
}
