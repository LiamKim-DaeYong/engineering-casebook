# Java `join()`

## 개요

`join()`은 다른 스레드가 끝날 때까지 현재 스레드를 기다리게 만드는 메서드다.
주로 메인 스레드가 작업 스레드의 종료를 기다릴 때 사용한다.

`join()`이 호출되면 기다리는 쪽 스레드는 대기 상태로 들어간다.
대상 스레드가 종료되면 다시 실행 가능 상태로 돌아온다.

## 기본 동작

```text
호출 스레드
→ join() 호출
→ 대상 스레드가 종료될 때까지 대기
→ 대상 스레드 종료
→ 호출 스레드 재개
```

`join()`에는 두 가지 형태가 있다.

- `join()`: 대상 스레드가 끝날 때까지 무기한 대기
- `join(long millis)`: 지정한 시간 동안만 대기

## 스레드 상태

`join()`을 호출한 스레드는 대기 상태가 된다.

- `join()`만 호출하면 `WAITING`
- `join(long millis)`를 호출하면 `TIMED_WAITING`

대상 스레드가 끝나거나 시간이 지나면 다시 `RUNNABLE`로 돌아온다.

## 예제

```java
public class JoinMain {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            log("작업 시작");
            sleep(2000);
            log("작업 종료");
        }, "worker");

        worker.start();

        log("main: worker 종료 대기");
        worker.join();
        log("main: worker 종료 확인");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void log(String message) {
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), message);
    }
}
```

실행 흐름은 대략 다음과 같다.

```text
[main] main: worker 종료 대기
[worker] 작업 시작
[worker] 작업 종료
[main] main: worker 종료 확인
```

## 시간 제한 대기

```java
worker.join(1000);
```

이 경우 `worker`가 1초 안에 끝나면 바로 재개되고, 끝나지 않아도 1초가 지나면 `main` 스레드가 다시 실행된다.

## InterruptedException

`join()`은 `InterruptedException`을 던질 수 있다.

이유는 대기 중인 스레드가 인터럽트될 수 있기 때문이다.
그래서 `join()`을 사용할 때는 보통 `throws InterruptedException`으로 넘기거나, `try/catch`로 처리한다.

## `sleep()`과의 차이

- `sleep()`은 현재 스레드를 잠시 멈춘다.
- `join()`은 다른 스레드가 끝날 때까지 현재 스레드를 기다리게 한다.

즉, `sleep()`은 시간 기준이고 `join()`은 대상 스레드 종료 기준이다.

## 핵심 정리

- `join()`은 다른 스레드의 종료를 기다리는 메서드다.
- 무기한 대기하면 `WAITING`, 시간 제한 대기하면 `TIMED_WAITING`이다.
- 대상 스레드가 끝나면 기다리던 스레드는 다시 `RUNNABLE`로 돌아온다.
- `join()`은 `InterruptedException`을 처리해야 한다.
- `sleep()`과 달리 `join()`은 대상 스레드 종료를 기준으로 기다린다.
