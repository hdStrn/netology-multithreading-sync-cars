import java.util.LinkedList;
import java.util.Queue;

public class Cars {

    public static void main(String[] args) {
        Queue<String> cars = new LinkedList<>(); // склад авто

        int carReleaseFreq = 2000; // частота выпуска авто производителем
        int carReleaseLimit = 10; // кол-во авто, выпускаемых производителем
        Thread manufacturer = new Thread(new Manufacturer(carReleaseFreq, carReleaseLimit, cars));
        manufacturer.start();

        int customerComeFreq = 1500; // частота прихода покупателя в автосалон
        int customerQty = 10; // кол-во покупателей
        for (int i = 0; i < customerQty; i++) {
            try {
                Thread.sleep(customerComeFreq);
            } catch (InterruptedException e) {
                return;
            }
            new Thread(new Customer(i + 1, cars)).start();
        }
    }
}

class Customer implements Runnable {

    private final int customerNum;
    private boolean carBought;
    private Queue<String> cars;

    public Customer(int customerNum, Queue<String> cars) {
        this.customerNum = customerNum;
        this.cars = cars;
    }

    @Override
    public void run() {
        while (!carBought) { // покупатель ходит в автосалон пока не купит авто
            System.out.println("Покупатель " + customerNum + " заходит в автосалон");
            synchronized (cars) {
                if (cars.isEmpty()) { // если авто в наличии нет
                    System.out.println("Доступных автомобилей в наличии нет");
                    int timeWaitingForCar = 2000;
                    try {
                        // ждет какое-то время пока не появится авто в наличии
                        // если прошло много времени, то уходит - вернется позже
                        cars.wait(timeWaitingForCar);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (!cars.isEmpty()) { // если авто есть в наличии
                    cars.poll(); // забирает авто
                    carBought = true;
                    System.out.println("Покупатель " + customerNum + " уехал на новеньком авто");
                }
            }
        }
    }
}

class Manufacturer implements Runnable {

    private final int carReleaseFreq; // частота выпуска авто производителем
    private final int carReleaseLimit; // кол-во авто, выпускаемых производителем
    private Queue<String> cars;

    public Manufacturer(int carReleaseFreq, int carReleaseLimit, Queue<String> cars) {
        this.carReleaseFreq = carReleaseFreq;
        this.carReleaseLimit = carReleaseLimit;
        this.cars = cars;
    }

    @Override
    public void run() {
        for (int i = 0; i < carReleaseLimit; i++) {
            try {
                Thread.sleep(carReleaseFreq);
            } catch (InterruptedException e) {
                break;
            }
            synchronized (cars) {
                cars.offer("Автомобиль");
                System.out.println("Производитель выпустил 1 автомобиль");
                cars.notify();
            }
        }
    }
}
