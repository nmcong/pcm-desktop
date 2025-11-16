package com.noteflix.pcm.core.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Async utilities for JavaFX applications
 *
 * <p>Provides convenient methods for running background tasks and updating UI. Follows best
 * practices: - All IO/long-running operations run on background threads - UI updates only on JavaFX
 * Application Thread - Proper error handling and cancellation support
 *
 * <p>Usage: <code>
 * Asyncs.runAsync(() -> {
 * // Background work
 * return fetchDataFromDatabase();
 * }, result -> {
 * // UI update (runs on FX thread)
 * label.setText(result);
 * }, error -> {
 * // Error handling (runs on FX thread)
 * showErrorDialog(error);
 * });
 * </code>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class Asyncs {

    private static final int THREAD_POOL_SIZE = 4;
    private static ExecutorService executor;

    static {
        initializeExecutor();
    }

    /**
     * Initialize the executor service
     */
    private static void initializeExecutor() {
        executor =
                new ThreadPoolExecutor(
                        THREAD_POOL_SIZE,
                        THREAD_POOL_SIZE,
                        60L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(),
                        r -> {
                            Thread thread = new Thread(r);
                            thread.setDaemon(true); // Don't prevent JVM shutdown
                            thread.setName("Asyncs-Worker-" + thread.getId());
                            return thread;
                        });

        log.info("Asyncs executor initialized with {} threads", THREAD_POOL_SIZE);
    }

    /**
     * Run a task asynchronously and handle result/error on FX thread
     *
     * @param backgroundTask Task to run in background
     * @param onSuccess      Success callback (runs on FX thread)
     * @param onError        Error callback (runs on FX thread)
     * @param <T>            Result type
     * @return CompletableFuture for advanced chaining
     */
    public static <T> CompletableFuture<T> runAsync(
            Callable<T> backgroundTask, Consumer<T> onSuccess, Consumer<Throwable> onError) {

        CompletableFuture<T> future =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return backgroundTask.call();
                            } catch (Exception e) {
                                throw new CompletionException(e);
                            }
                        },
                        executor);

        future.whenComplete(
                (result, error) -> {
                    Platform.runLater(
                            () -> {
                                if (error != null) {
                                    if (onError != null) {
                                        onError.accept(error.getCause() != null ? error.getCause() : error);
                                    }
                                } else {
                                    if (onSuccess != null) {
                                        onSuccess.accept(result);
                                    }
                                }
                            });
                });

        return future;
    }

    /**
     * Run a task asynchronously with only success callback
     *
     * @param backgroundTask Task to run in background
     * @param onSuccess      Success callback (runs on FX thread)
     * @param <T>            Result type
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> runAsync(
            Callable<T> backgroundTask, Consumer<T> onSuccess) {
        return runAsync(backgroundTask, onSuccess, error -> log.error("Async task failed", error));
    }

    /**
     * Run a void task asynchronously
     *
     * @param backgroundTask Task to run in background
     * @param onSuccess      Success callback (runs on FX thread)
     * @param onError        Error callback (runs on FX thread)
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(
            Runnable backgroundTask, Runnable onSuccess, Consumer<Throwable> onError) {

        CompletableFuture<Void> future = CompletableFuture.runAsync(backgroundTask, executor);

        future.whenComplete(
                (result, error) -> {
                    Platform.runLater(
                            () -> {
                                if (error != null) {
                                    if (onError != null) {
                                        onError.accept(error.getCause() != null ? error.getCause() : error);
                                    }
                                } else {
                                    if (onSuccess != null) {
                                        onSuccess.run();
                                    }
                                }
                            });
                });

        return future;
    }

    /**
     * Run on JavaFX Application Thread
     *
     * @param action Action to run on FX thread
     */
    public static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Create a JavaFX Task for more control (progress, cancellation)
     *
     * @param callable The callable to execute
     * @param <T>      Result type
     * @return JavaFX Task
     */
    public static <T> Task<T> createTask(Callable<T> callable) {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };
    }

    /**
     * Execute a Task with proper lifecycle handling
     *
     * @param task The task to execute
     * @param <T>  Result type
     */
    public static <T> void executeTask(Task<T> task) {
        executor.execute(task);
    }

    /**
     * Execute a Task and bind to callbacks
     *
     * @param task      Task to execute
     * @param onSuccess Success callback
     * @param onError   Error callback
     * @param <T>       Result type
     */
    public static <T> void executeTask(
            Task<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {

        task.setOnSucceeded(
                event -> {
                    if (onSuccess != null) {
                        onSuccess.accept(task.getValue());
                    }
                });

        task.setOnFailed(
                event -> {
                    if (onError != null) {
                        onError.accept(task.getException());
                    }
                });

        executor.execute(task);
    }

    /**
     * Delay execution on FX thread
     *
     * @param delayMillis Delay in milliseconds
     * @param action      Action to run after delay
     */
    public static void delay(long delayMillis, Runnable action) {
        CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)
                .execute(() -> Platform.runLater(action));
    }

    /**
     * Get the executor service (for advanced usage)
     *
     * @return ExecutorService
     */
    public static ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Shutdown the executor (call on application exit)
     */
    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            log.info("Shutting down Asyncs executor...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("Asyncs executor forced shutdown");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
