package github.duanni.lab.kestrel

import net.rubyeye.xmemcached.utils.AddrUtil
import net.rubyeye.xmemcached.command.KestrelCommandFactory
import scala.actors.Actor._
import net.rubyeye.xmemcached.{MemcachedClient, XMemcachedClientBuilder}
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

abstract class Base(val threads: List[Int],
                    val values: List[String],
                    val queue: String,
                    val totalItems: Int,
                    val servers: String) {
  val logger = LoggerFactory.getLogger(getClass)
  val timeout = 5000

  def getClient(poolSize: Int) = {
    val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers));
    builder.setCommandFactory(new KestrelCommandFactory());
    builder.setConnectionPoolSize(poolSize);
    //    builder.setSocketOption(StandardSocketOption.SO_RCVBUF, 32 * 1024); // 设置接收缓存区为32K，默认16K
    //    builder.setSocketOption(StandardSocketOption.SO_SNDBUF, 16 * 1024); // 设置发送缓冲区为16K，默认为8K
    //    builder.setSocketOption(StandardSocketOption.TCP_NODELAY, false); // 启用nagle算法，提高吞吐量，默认关闭
    val client = builder.build();
    client.setPrimitiveAsString(true);
    //    client.setOpTimeout(5000)
    client
  }

  val formatStr = {
    "%1$-" + ("threads".length + 6) + "d" +
      "%2$-" + ("items/t".length() + 6) + "d" +
      "%3$-" + ("all".length() + 6) + "d" +
      "%4$-" + ("valueLength".length + 6) + "d" +
      "%5$-" + ("tps/s".length() + 6) + ".0f" +
      "%6$.2f\n"
  }


  def start() {
    val client = getClient(Runtime.getRuntime.availableProcessors() + 1)
    logger.debug("start.")
    test(client, "x" * 100, 30, 3000, false) // 30 * 30000
    logger.debug("warm up")
    println("=" * 70)
    println("threads      items/t      all      valueLength      tps/s      times(s)")
    threads.foreach {
      thread =>
        val items = totalItems / thread
        values.foreach {
          value =>
            test(client, value, thread, items, true)

        }
    }
    println("=" * 70)
    catchExp {
      client.shutdown()
    }
  }

  def test(client: MemcachedClient, value: String, threads: Int, itemsEachThread: Int, print: Boolean) {
    val totalItems = threads * itemsEachThread
    val countDown = new CountDownLatch(totalItems)
    val start = System.currentTimeMillis()
    for (t <- 0 until threads) {
      actor {
        for (item <- 0 until itemsEachThread) {
          execute(client, value)
          countDown.countDown()
        }
      }
    }

    countDown.await()
    if (print) {
      val duration = System.currentTimeMillis() - start
      printResult(threads, itemsEachThread, totalItems, value.length, duration)
    }
  }


  def printResult(threads: Int, itemsEachThreads: Int, all: Int, valueLength: Int, times: Long) {
    val sec = (times / 1000.0)
    val tps = all / sec
    //    println(formatStr, threads, itemsEachThreads, all, valueLength, tps, sec)
    printf(formatStr, threads, itemsEachThreads, all, valueLength, tps, sec)
  }

  def catchExp(func: => Unit) {
    try {
      func
    } catch {
      case ex: Exception =>
    }
  }

  def execute(client: MemcachedClient, value: String): Unit
}


class Put(threads: List[Int],
          values: List[String],
          queue: String,
          totalItems: Int,
          servers: String) extends Base(threads, values, queue, totalItems, servers) {

  override def execute(client: MemcachedClient, value: String) {
    catchExp {
      client.set(queue, 0, value, timeout)
    }
  }

}

class PutAndGet(threads: List[Int],
                values: List[String],
                queue: String,
                totalItems: Int,
                servers: String) extends Base(threads, values, queue, totalItems, servers) {
  override def execute(client: MemcachedClient, value: String) {
    catchExp {
      client.set(queue, 0, value, timeout)
      val v = client.get(queue)
      v
    }
  }
}

object Base {
  def apply(threads: List[Int],
            values: List[String],
            queue: String,
            totalItems: Int,
            servers: String): List[Base] = {
    List(
      new Put(threads, values, queue, totalItems, servers),
      new PutAndGet(threads, values, queue, totalItems, servers)
    )
  }
}