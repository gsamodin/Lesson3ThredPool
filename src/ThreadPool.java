import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    private final List<WorkerThread> threads = new LinkedList<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public ThreadPool(int capacity) {
        for (int i = 0; i < capacity; i++) {
            WorkerThread thread = new WorkerThread();
            threads.add(thread);
            thread.start();
        }
    }

    public void execute(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool is shutdown");
        }
        synchronized (taskQueue) {
            taskQueue.addLast(task);
            taskQueue.notify();
        }
    }

    public void shutdown() {
        isShutdown.set(true);
        synchronized (taskQueue) {
            taskQueue.clear(); // Отменяем ожидающие задачи
            taskQueue.notifyAll(); // Прерываем все ждущие потоки
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (WorkerThread thread : threads) {
            thread.join();
        }
    }

    private class WorkerThread extends Thread {
        @Override
        public void run() {
            while (!isShutdown.get() || !taskQueue.isEmpty()) {
                Runnable task;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty() && !isShutdown.get()) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            // Поток прерван при shutdown
                        }
                    }
                    if (taskQueue.isEmpty()) {
                        break; // Завершаем, если задач нет и пул завершен
                    }
                    task = taskQueue.removeFirst();
                }
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("Task failed: " + e.getMessage());
                }
            }
        }
    }
}