# Java `interrupt()` and `yield()`

## 개요

`interrupt()`와 `yield()`는 스레드의 실행 흐름을 다룰 때 자주 등장하는 메서드다.
하지만 둘의 의미는 다르다.

- `interrupt()`는 스레드에 중단 요청을 보내는 메서드다.
- `yield()`는 현재 실행 중인 스레드가 다른 스레드에게 실행 기회를 양보하겠다는 힌트를 주는 메서드다.

## `interrupt()`

`interrupt()`는 실행 중인 스레드를 즉시 강제 종료하는 메서드가 아니다.
대신 해당 스레드에 인터럽트 요청 상태를 설정한다.

스레드는 이 신호를 직접 확인하거나, `sleep()`, `wait()`, `join()` 같은 대기 지점에서 반응할 수 있다.

### 인터럽트 상태 확인

인터럽트 상태를 확인하는 방법은 두 가지가 있다.

- `isInterrupted()`
- `Thread.interrupted()`

`isInterrupted()`는 현재 스레드의 인터럽트 상태를 확인만 하고 변경하지 않는다.

`Thread.interrupted()`는 현재 스레드의 인터럽트 상태를 확인한 뒤 `false`로 초기화한다.
상태가 이미 `false`라면 그대로 `false`가 유지된다.

### `sleep()`과 인터럽트

`sleep()` 중인 스레드가 인터럽트를 받으면 `InterruptedException`이 발생할 수 있다.
이때 인터럽트 상태는 보통 `false`로 초기화된다.

즉, `sleep()`은 인터럽트에 반응하는 대표적인 대기 메서드다.

인터럽트는 스레드를 강제로 끝내는 것이 아니라, 스레드가 중단 요청을 확인하고 다시 실행 흐름으로 돌아오게 만드는 신호라고 보는 편이 맞다.
대기 상태에 있던 스레드는 인터럽트를 만나면 깨어나서 다시 실행 가능한 상태로 돌아갈 수 있다.

### 예제

```java
public class InterruptMain {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                log("작업 중");
                sleep(300);
            }
            log("인터럽트 확인, 종료");
        }, "worker");

        thread.start();

        sleep(1000);
        thread.interrupt();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String message) {
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), message);
    }
}
```

## `yield()`

`yield()`는 현재 실행 중인 스레드가 CPU를 다른 스레드에게 양보할 수 있음을 스케줄러에 알리는 힌트다.

중요한 점은 `yield()`가 반드시 실행을 멈추게 하는 것은 아니라는 것이다.
스케줄러는 이 힌트를 무시할 수도 있고, 다른 스레드가 실행될 수도 있다.

### `sleep()`과 차이

- `sleep()`은 현재 스레드를 일정 시간 멈춘다.
- `yield()`는 실행을 잠깐 양보해보겠다는 힌트다.

`sleep()`은 `TIMED_WAITING` 상태로 들어가지만, `yield()`는 일반적으로 `RUNNABLE` 상태에서 스케줄러에게 양보 의사를 전달하는 정도로 이해하면 된다.

### 예제

```java
public class YieldMain {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> work("thread1"), "thread1");
        Thread thread2 = new Thread(() -> work("thread2"), "thread2");

        thread1.start();
        thread2.start();
    }

    private static void work(String name) {
        for (int i = 0; i < 5; i++) {
            log(name + " - " + i);
            Thread.yield();
        }
    }

    private static void log(String message) {
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), message);
    }
}
```

실행 결과는 환경에 따라 달라질 수 있다.
`yield()`는 보장된 제어가 아니라 스케줄러에게 전달하는 힌트이기 때문이다.

## `Thread.stop()`

과거에는 `Thread.stop()`이 있었지만 지금은 deprecated 상태다.

이 메서드는 스레드를 안전하지 않은 시점에 강제로 종료할 수 있어서 문제가 된다.
스레드가 락을 잡고 있거나 공유 데이터를 수정하는 중이라면, 강제 종료로 인해 객체 상태가 깨질 수 있다.
또한 종료 전에 필요한 정리 작업을 수행할 기회도 주지 않는다.

즉, `stop()`은 예측 가능하고 안전한 종료 방식이 아니기 때문에 사용하지 않는다.

## 상태 정리

- `interrupt()`: 스레드에 중단 요청을 남긴다.
- `isInterrupted()`: 인터럽트 상태를 확인만 한다.
- `Thread.interrupted()`: 인터럽트 상태를 확인하고 `false`로 초기화한다.
- `yield()`: 현재 스레드가 실행 기회를 양보하겠다는 힌트다.
- `Thread.stop()`은 강제 종료로 인해 안전하지 않아서 deprecated 됐다.

## 핵심 정리

- `interrupt()`는 강제 종료가 아니라 중단 요청이다.
- 인터럽트는 `sleep()`, `wait()`, `join()` 같은 지점에서 반응하기 쉽다.
- `isInterrupted()`는 상태만 본다.
- `Thread.interrupted()`는 상태를 보고 초기화한다.
- `yield()`는 다른 스레드에게 실행 기회를 넘겨보겠다는 힌트다.
- `yield()`는 반드시 양보를 보장하지 않는다.
- `Thread.stop()`은 공유 상태를 망가뜨릴 수 있어서 deprecated 됐다.
