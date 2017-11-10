package com.homeworks;


import com.sun.prism.shader.DrawSemiRoundRect_ImagePattern_AlphaTest_Loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class MainClass {
    public static final int CARS_COUNT = 4;
    public static CyclicBarrier cb = new CyclicBarrier(4);

    public static CountDownLatch latch = new CountDownLatch(CARS_COUNT);
    public static CountDownLatch prepareForTheRacelatch = new CountDownLatch(CARS_COUNT);
    static AtomicInteger winner = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10));
        }
        for (int i = 0; i < cars.length; i++) {
            new Thread(cars[i]).start();

        }
        try {
            prepareForTheRacelatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
        }

        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        }


    }
}
 class Car implements Runnable {

    private static int CARS_COUNT;


    static {
        CARS_COUNT = 0;
    }

    private Race race;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }
    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            MainClass.prepareForTheRacelatch.countDown(); // отсчитываем количество готовых к гонке участников
                MainClass.cb.await();

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);


            }
        }
    }

 abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}
 class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (length==40) {      // отщелкиваем 4 раза на послежнем стейдже пути для подтверждения окончания гонки
                MainClass.latch.countDown();
                if(MainClass.winner.incrementAndGet()==1) {  // находим победителя по факту завершения последнего этапа
                    System.out.println(c.getName()+" WIN!!!");
                }
            }
        }
    }
}
 class Tunnel extends Stage {
     Semaphore smp = new Semaphore(MainClass.CARS_COUNT/2);

     public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }

    @Override
    public void go(Car c) {

        {
            try {

               System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                smp.acquire();                          // пропускаем только двух участников в семафор (туннель)
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                smp.release();         // освобождаем семафор(туннель)
                System.out.println(c.getName() + " закончил этап: " + description);

            }
        }
    }
}
 class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() { return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}
