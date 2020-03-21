package be.jdevelopment.tailrec;

public class Main {

    public static void main(String[] args) {
        Fibo f = new Fibo();
        System.out.println(System.currentTimeMillis());
        f.fibonacci(1000000);
        System.out.println(System.currentTimeMillis());
    }

}
