# Java `LockSupport`와 `ReentrantLock`

## 개요

`LockSupport`와 `ReentrantLock`은 자바에서 스레드의 대기와 깨우기, 그리고 임계 영역 보호를 더 세밀하게 제어할 때 사용하는 도구다.
`synchronized`가 자바의 기본 동기화 방법이라면, 이 둘은 그보다 조금 더 직접적으로 스레드와 락을 다루는 방법이라고 볼 수 있다.

이 문서는 `LockSupport`가 무엇을 위한 도구인지, `ReentrantLock`이 왜 필요한지,
그리고 둘이 어떤 관계를 가지는지까지 이해하는 데 초점을 둔다.

## 왜 필요한가

`synchronized`만으로도 많은 동시성 문제를 해결할 수 있다.
하지만 실제로는 락 획득을 기다리다가 포기하고 싶을 때도 있고,
정해진 시간까지만 기다리고 싶을 때도 있고,
락을 얻은 스레드만 특정 조건에서 다시 기다리게 만들고 싶을 때도 있다.

이런 상황에서는 락과 대기 방식을 더 세밀하게 제어할 수 있는 도구가 필요하다.
그때 자주 등장하는 것이 `ReentrantLock`이고,
그 내부 대기 메커니즘을 이해할 때 연결되는 것이 `LockSupport`다.

## `LockSupport`는 무엇인가

`LockSupport`는 스레드를 잠시 멈추게 하거나 다시 깨우는 저수준 유틸리티다.
핵심 메서드는 `park()`, `parkNanos()`, `unpark()`다.

- `park()`: 현재 스레드를 대기 상태로 둔다.
- `parkNanos(nanos)`: 현재 스레드를 지정한 시간 동안만 대기시킨다.
- `unpark(thread)`: 대상 스레드가 다시 진행할 수 있게 신호를 준다.

직관적으로 보면 `park()`는 스레드를 잠시 멈추게 하고,
`unpark()`는 다시 진행 가능하게 만드는 동작에 가깝다.
`parkNanos()`는 여기에 시간 제한이 추가된 형태로 이해할 수 있다.

## `parkNanos()`는 언제 쓰는가

항상 다른 스레드가 깨워줄 때까지 무기한 기다릴 필요는 없다.
어떤 경우에는 아주 짧은 시간만 대기한 뒤 다시 상태를 확인하고 싶을 수 있다.

이럴 때 `parkNanos()`를 사용할 수 있다.
즉, `park()`가 무기한 대기에 가깝다면,
`parkNanos()`는 나노초 단위의 시간 제한 대기라고 볼 수 있다.

이 메서드는 정밀한 대기 제어가 필요할 때 사용되지만,
애플리케이션 코드에서 직접 자주 쓰기보다는 동시성 유틸리티 내부 동작을 이해하는 맥락에서 보는 경우가 많다.

`unpark(thread)`가 먼저 호출된 뒤 나중에 `park()`가 실행되면,
바로 통과할 수 있다는 점도 함께 기억할 만하다.

그래서 `LockSupport`는 더 기본적인 수준의 스레드 제어 도구로 볼 수 있다.
자바의 여러 동시성 유틸리티도 내부적으로 이런 방식에 기대어 동작한다.

## `ReentrantLock`은 무엇인가

`ReentrantLock`은 `Lock` 인터페이스의 대표 구현체로,
`synchronized`와 비슷하게 임계 영역을 보호하지만 더 많은 기능을 제공하는 명시적 락이다.

이름의 `Reentrant`는 재진입 가능하다는 뜻이다.
즉, 어떤 스레드가 이미 이 락을 가지고 있다면 같은 스레드는 다시 그 락을 얻을 수 있다.
이 점은 `synchronized`와 비슷하다.

```java
private final ReentrantLock lock = new ReentrantLock();

public void withdraw(int amount) {
    lock.lock();
    try {
        if (balance >= amount) {
            balance -= amount;
        }
    } finally {
        lock.unlock();
    }
}
```

핵심은 `lock()`으로 락을 획득하고,
작업이 끝나면 반드시 `unlock()`으로 반납해야 한다는 점이다.
그래서 보통 `finally`에서 해제한다.

## `synchronized`와 비교하면

`synchronized`는 문법 차원에서 락을 걸어주기 때문에 단순하고 안전하다.
블록을 빠져나오면 자동으로 락이 해제된다.

반면 `ReentrantLock`은 개발자가 직접 락 획득과 해제를 관리해야 한다.
대신 더 다양한 제어 기능을 제공한다.

대표적으로 이런 차이가 있다.

- 락 획득을 시도만 해보는 `tryLock()`
- 일정 시간만 기다리는 `tryLock(timeout)`
- 락 대기 중 인터럽트를 받을 수 있는 `lockInterruptibly()`
- 공정 락 옵션 지원

즉, 단순한 임계 영역 보호라면 `synchronized`도 충분하지만,
락 제어 조건이 복잡해지면 `ReentrantLock`이 더 적합할 수 있다.

## `synchronized`의 한계

`synchronized`의 대표적인 한계는 락을 얻지 못했을 때 대기 방식을 세밀하게 제어하기 어렵다는 점이다.

한 스레드가 이미 락을 가지고 있으면 다른 스레드는 `BLOCKED` 상태로 기다린다.
이때 기본적으로는 락이 풀릴 때까지 계속 기다려야 한다.
즉, 락 획득을 포기하거나 일정 시간만 기다리는 식의 제어가 어렵다.

또 `BLOCKED` 상태에서 인터럽트가 걸렸다고 해서 바로 `RUNNABLE`로 돌아오는 것도 아니다.
즉, `synchronized`의 락 대기 자체는 인터럽트로 즉시 끊기지 않는다.
결국 락이 풀려서 획득할 수 있게 되어야 다음으로 진행할 수 있다.

또 `synchronized`는 공정성을 직접 설정할 수 없다.
먼저 기다린 스레드가 먼저 락을 얻는다고 보장하지 않는다.
그래서 어떤 스레드가 오래 기다리게 될 가능성도 있다.

반면 `ReentrantLock`은 `tryLock()`, 시간 제한 락 획득, `lockInterruptibly()` 같은 인터럽트 가능한 대기,
공정 락 옵션 같은 기능을 제공해서 이런 한계를 보완할 수 있다.

## 공정 락과 비공정 락

`ReentrantLock`은 공정성 옵션을 줄 수 있다.

```java
ReentrantLock fairLock = new ReentrantLock(true);
```

공정 락은 먼저 기다린 스레드가 먼저 락을 얻도록 최대한 보장하려는 방식이다.
반면 기본값인 비공정 락은 순서를 엄격히 보장하지 않는다.

공정 락은 예측 가능성에는 도움이 되지만,
일반적으로 처리량 측면에서는 비공정 락이 더 유리한 경우가 많다.
그래서 기본값이 비공정 락이다.

## 둘의 관계

`LockSupport`와 `ReentrantLock`은 같은 수준의 도구가 아니다.

- `LockSupport`: 스레드를 멈추고 깨우는 저수준 도구
- `ReentrantLock`: 임계 영역 보호와 대기 제어를 제공하는 고수준 락 도구

즉, `ReentrantLock`을 사용할 때 직접 `LockSupport`를 항상 함께 쓰는 것은 아니지만,
동시성 라이브러리 내부에서는 `LockSupport` 같은 메커니즘이 기반이 된다.

그래서 공부할 때는 "`LockSupport`는 스레드 제어의 기반 도구",
"`ReentrantLock`은 애플리케이션 코드에서 직접 사용할 수 있는 명시적 락" 정도로 구분하면 이해하기 쉽다.

## 주의할 점

`ReentrantLock`은 직접 `unlock()`해야 하므로 해제를 빼먹으면 큰 문제가 된다.
락이 풀리지 않으면 다른 스레드가 계속 대기하게 된다.

또 `LockSupport`는 저수준 도구라서 단순히 `park()`와 `unpark()`만 외우면 끝나는 것이 아니다.
언제 멈추고 언제 깨울지 흐름을 잘못 설계하면 디버깅이 어려워질 수 있다.

그래서 일반적인 애플리케이션 코드에서는 가능한 한 `synchronized`, `ReentrantLock`,
`BlockingQueue` 같은 더 높은 수준의 도구를 먼저 사용하는 편이 낫다.

## 핵심 정리

- `LockSupport`는 스레드를 `park()`로 멈추고 `unpark()`로 다시 진행시키는 저수준 도구다.
- `unpark(thread)`가 먼저 호출된 뒤 나중에 `park()`가 실행되면 바로 진행될 수 있다.
- `parkNanos()`를 사용하면 스레드를 무기한이 아니라 지정한 시간 동안만 대기시킬 수 있다.
- `ReentrantLock`은 `synchronized`보다 더 많은 제어 기능을 제공하는 명시적 락이다.
- `ReentrantLock`은 `lock()` 후 반드시 `finally`에서 `unlock()`해야 한다.
- `synchronized`는 락 대기 시간을 제어하기 어렵고 공정성을 직접 설정할 수 없다.
- `synchronized`에서 락을 기다리며 `BLOCKED` 상태인 스레드는 인터럽트가 걸려도 바로 락 대기가 중단되지 않는다.
- `tryLock()`, `lockInterruptibly()`, 공정 락 같은 기능은 `ReentrantLock`의 대표 장점이다.
- `LockSupport`는 기반 메커니즘에 가깝고, `ReentrantLock`은 애플리케이션 코드에서 직접 사용하는 락 도구에 가깝다.
