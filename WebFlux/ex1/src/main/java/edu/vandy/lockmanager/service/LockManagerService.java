package edu.vandy.lockmanager.service;

import java.util.List;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LockManagerService {

	Mono<LockManager> create(Integer permitCount);

	Mono<Lock> acquire(LockManager lockManager);

	Flux<Lock> acquire(LockManager lockManager, int permits);

	Mono<Boolean> release(LockManager lockManager, List<Lock> locks);

	Mono<Boolean> release(LockManager lockManager, Lock lock);

}
