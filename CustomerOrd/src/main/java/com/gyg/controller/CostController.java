package com.gyg.controller;

import com.gyg.config.exception.GlobalException;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 郭永钢
 */
@RestController
public class CostController {
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	Redisson redisson;

	static final String REDIS_LOCK = "gyg_lock";

	@Value("${server.port}")
	String port;

	/**
	 * 没有加锁，高并发会出现超卖
	 *
	 * @return
	 */
	@GetMapping("/buy")
	public String buy() {
		String s = redisTemplate.opsForValue().get("good:001");
		int number = s == null ? 0 : Integer.parseInt(s);
		if (number > 0) {
			int new_number = number - 1;
			redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
			System.out.println("端口：" + port + "\t剩余：" + new_number);
			return "端口：" + port + "\t剩余：" + new_number;
		}
		System.out.println("端口：" + port + "\t商品已售完");
		return "端口：" + port + "\t商品已售完";
	}

	/**
	 * JVM版单机锁
	 */
	@GetMapping("/buy1")
	public String buy1() {
		synchronized (this) {
			String s = redisTemplate.opsForValue().get("good:001");
			int number = s == null ? 0 : Integer.parseInt(s);

			if (number > 0) {
				int new_number = number - 1;
				redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
				System.out.println("端口：" + port + "\t剩余：" + new_number);
				return "端口：" + port + "\t剩余：" + new_number;
			}
			System.out.println("端口：" + port + "\t商品已售完");
			return "端口：" + port + "\t商品已售完";
		}
	}

	/**
	 * 采用redis锁
	 * 缺陷：处理逻辑时，自己的锁过期，会误删别人的锁
	 *
	 * @return
	 */
	@GetMapping("/buy2")
	public String buy2() throws Exception {

		String redis_lock = Thread.currentThread().getName() + UUID.randomUUID().toString();
		try {
			// 分开没有原子性，不能保证
//			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock);
//			redisTemplate.expire(REDIS_LOCK, 10, TimeUnit.SECONDS);

			// 原子性命令
			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock, 10, TimeUnit.SECONDS);

			if (!flag) {
				return "资源被占用！";
			}
			String s = redisTemplate.opsForValue().get("good:001");
			int number = s == null ? 0 : Integer.parseInt(s);
			if (number > 0) {
				int new_number = number - 1;
				redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
				System.out.println("端口：" + port + "\t剩余：" + new_number);
				return "端口：" + port + "\t剩余：" + new_number;
			}
			System.out.println("端口：" + port + "\t商品已售完");
			return "端口：" + port + "\t商品已售完";
		} catch (Exception e) {
			throw new GlobalException();
		} finally {
			redisTemplate.delete(REDIS_LOCK);
		}
	}

	/**
	 * 采用redis锁
	 * 采用事务解决
	 *
	 * @return
	 */
	@GetMapping("/buy3")
	public String buy3() throws Exception {

		String redis_lock = Thread.currentThread().getName() + UUID.randomUUID().toString();
		try {
			// 分开没有原子性，不能保证
//			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock);
//			redisTemplate.expire(REDIS_LOCK, 10, TimeUnit.SECONDS);

			// 原子性命令
			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock, 10, TimeUnit.SECONDS);

			if (!flag) {
				return "资源被占用！";
			}
			String s = redisTemplate.opsForValue().get("good:001");
			int number = s == null ? 0 : Integer.parseInt(s);
			if (number > 0) {
				int new_number = number - 1;
				redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
				System.out.println("端口：" + port + "\t剩余：" + new_number);
				return "端口：" + port + "\t剩余：" + new_number;
			}
			System.out.println("端口：" + port + "\t商品已售完");
			return "端口：" + port + "\t商品已售完";
		} catch (Exception e) {
			throw new GlobalException();
		} finally {
			while (true) {
				redisTemplate.watch(REDIS_LOCK);
				if (redis_lock.equals(redisTemplate.opsForValue().get(REDIS_LOCK))) {
					redisTemplate.setEnableTransactionSupport(true);
					redisTemplate.multi();
					redisTemplate.delete(REDIS_LOCK);
					List<Object> list = redisTemplate.exec();
					if (list == null){
						// 被修改，继续
						continue;
					}
				}
				redisTemplate.unwatch();
				break;
			}
		}
	}

	/**
	 * 采用redis锁
	 * 采用lua脚本
	 * 缺陷，锁过期无法续命。且为redis单体，redis集群没有强一致性，只有AP没有C，zoo是CP
	 * @return
	 */
	@GetMapping("/buy4")
	public String buy4() throws Exception {

		String redis_lock = Thread.currentThread().getName() + UUID.randomUUID().toString();
		try {
			// 分开没有原子性，不能保证
//			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock);
//			redisTemplate.expire(REDIS_LOCK, 10, TimeUnit.SECONDS);

			// 原子性命令
			Boolean flag = redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, redis_lock, 10, TimeUnit.SECONDS);

			if (!flag) {
				return "资源被占用！";
			}
			String s = redisTemplate.opsForValue().get("good:001");
			int number = s == null ? 0 : Integer.parseInt(s);
			if (number > 0) {
				int new_number = number - 1;
				redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
				System.out.println("端口：" + port + "\t剩余：" + new_number);
				return "端口：" + port + "\t剩余：" + new_number;
			}
			System.out.println("端口：" + port + "\t商品已售完");
			return "端口：" + port + "\t商品已售完";
		} catch (Exception e) {
			throw new GlobalException();
		} finally {
			String script = "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
					"then\n" +
					"return redis.call('del',KEYS[1])\n" +
					"else\n" +
					"   return 0\n" +
					"end";
			DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script,Long.class);

			Long execute = redisTemplate.execute(redisScript, Collections.singletonList(REDIS_LOCK), redis_lock);
			if (execute==1){
				System.out.println("解锁成功！");
			}
		}
	}

	/**
	 * 采用Redisson分布式锁
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/buy5")
	public String buy5() throws Exception {
		RLock lock = redisson.getLock(REDIS_LOCK);
		lock.lock();
		try {
			String s = redisTemplate.opsForValue().get("good:001");
			int number = s == null ? 0 : Integer.parseInt(s);
			if (number > 0) {
				int new_number = number - 1;
				redisTemplate.opsForValue().set("good:001", String.valueOf(new_number));
				System.out.println("端口：" + port + "\t剩余：" + new_number);
				return "端口：" + port + "\t剩余：" + new_number;
			}
			System.out.println("端口：" + port + "\t商品已售完");
			return "端口：" + port + "\t商品已售完";
		} catch (Exception e) {
			throw new GlobalException();
		} finally {
			//直接解锁，并发太大，会报错
			if (lock.isLocked()) {
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		}
	}
}
