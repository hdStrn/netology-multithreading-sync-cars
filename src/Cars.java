import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Cars {

    public static void main(String[] args) {
        Queue<String> cars = new LinkedList<>(); // склад авто
        Lock lock = new ReentrantLock(true); // лок с честностью
        Condition carsEmptyCondition = lock.newCondition(); // условие для ожидания

        int carReleaseFreq = 2000; // частота выпуска авто производителем
        int carReleaseLimit = 10; // кол-во авто, выпускаемых производителем
        Thread manufacturer = new Thread(
                new Manufacturer(carReleaseFreq, carReleaseLimit, cars, lock, carsEmptyCondition));
        manufacturer.start();

        int customerComeFreq = 1200; // частота прихода покупателя в автосалон
        int customerQty = 10; // кол-во покупателей
        for (int i = 0; i < customerQty; i++) {
            try {
                Thread.sleep(customerComeFreq);
            } catch (InterruptedException e) {
                return;
            }
            new Thread(new Customer("Покупатель " + (i + 1), cars, lock, carsEmptyCondition))
                    .start();
        }
    }
}

class Customer implements Runnable {

    private final String customerName;
    private boolean carBought;
    private final Queue<String> cars;
    private final Lock lock;
    private final Condition carsEmptyCondition;

    public Customer(String customerName, Queue<String> cars, Lock lock, Condition carsEmptyCondition) {
        this.customerName = customerName;
        this.cars = cars;
        this.lock = lock;
        this.carsEmptyCondition = carsEmptyCondition;
    }

    @Override
    public void run() {
        while (!carBought) { // покупатель ходит в автосалон пока не купит авто
            System.out.println(customerName + " заходит в автосалон");
            lock.lock();
            if (cars.isEmpty()) { // если авто в наличии нет
                System.out.println("Доступных автомобилей в наличии нет");
                try {
                    // ждет какое-то время пока не появится авто в наличии
                    // если прошло много времени, то уходит - вернется позже
                    long timeWaitingForCar = 2500;
                    boolean awaited = carsEmptyCondition.await(timeWaitingForCar, TimeUnit.MILLISECONDS);
                    if (!awaited) {
                        System.out.println(customerName + " не дождался и ушел. Вернется позже");
                        lock.unlock();
                        int returningTime = 2000; // время, через которое покупатель вернется в автосалон, если не купил авто сразу
                        Thread.sleep(returningTime);
                        continue;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!cars.isEmpty()) { // если авто есть в наличии
                cars.poll(); // забирает авто
                carBought = true;
                System.out.println(customerName + " уехал на новеньком авто");
            }
            lock.unlock();
        }
    }
}

class Manufacturer implements Runnable {

    private final int carReleaseFreq; // частота выпуска авто производителем
    private final int carReleaseLimit; // кол-во авто, выпускаемых производителем
    private Queue<String> cars;
    private final Lock lock;
    private final Condition carsEmptyCondition;

    public Manufacturer(int carReleaseFreq, int carReleaseLimit, Queue<String> cars, Lock lock, Condition carsEmptyCondition) {
        this.carReleaseFreq = carReleaseFreq;
        this.carReleaseLimit = carReleaseLimit;
        this.cars = cars;
        this.lock = lock;
        this.carsEmptyCondition = carsEmptyCondition;
    }

    @Override
    public void run() {
        for (int i = 0; i < carReleaseLimit; i++) {
            try {
                Thread.sleep(carReleaseFreq);
            } catch (InterruptedException e) {
                break;
            }
            lock.lock();
            cars.offer("Автомобиль");
            System.out.println("Производитель выпустил 1 автомобиль");
            carsEmptyCondition.signal();
            lock.unlock();
        }
    }
}
