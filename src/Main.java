public class Main {
    public static void main(String[] args) throws InterruptedException {
        ThreadPool pool = new ThreadPool(2);

        pool.execute(() -> System.out.println("Task 1"));
        pool.execute(() -> System.out.println("Task 2"));

        pool.shutdown();
        pool.awaitTermination();
    }
}