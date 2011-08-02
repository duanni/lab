package github.duanni.lab.kestrel

import org.slf4j.LoggerFactory
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.utils.AddrUtil
import net.rubyeye.xmemcached.command.KestrelCommandFactory
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import scala.actors.Actor._

@RunWith(classOf[JUnitRunner])
class BaseBenchmarkTest extends FunSuite {

  val logger = LoggerFactory.getLogger(getClass)
  val processors = Runtime.getRuntime.availableProcessors() + 1;
  val v = "x" * 1024
  val items = 500000;
  val queue = "spam"
  val thread = processors

  def getClient(poolSize: Int) = {
    val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses("192.168.17.234:22133"));
    builder.setCommandFactory(new KestrelCommandFactory());
    builder.setConnectionPoolSize(poolSize);
    //    builder.setSocketOption(StandardSocketOption.SO_RCVBUF, 32 * 1024); // 设置接收缓存区为32K，默认16K
    //    builder.setSocketOption(StandardSocketOption.SO_SNDBUF, 16 * 1024); // 设置发送缓冲区为16K，默认为8K
    //    builder.setSocketOption(StandardSocketOption.TCP_NODELAY, false); // 启用nagle算法，提高吞吐量，默认关闭
    val client = builder.build();
    client.setPrimitiveAsString(true);
    client.setOpTimeout(5000)
    client
  }

  val client = {

  }
  val countDown = new CountDownLatch(items)

  test("kestrel client test") {
    //    process(put, eachThreadForGetOrPut)
    //    process(get, eachThreadForGetOrPut)
    process(putAndGet, eachThreadForGetAndPut)
  }

  def eachThreadForGetOrPut(func: => Unit, count: Int) {
    actor {
      for (i <- 0 until count)
        func
    }
  }

  def eachThreadForGetAndPut(func: => Unit, count: Int) {
    val getActor = actor {
      loopWhile(countDown.getCount != 0) {
        reactWithin(500) {
          case _ =>
            val v: String = client.get(queue)
            v
            countDown.countDown()
          //            logger.info("get => " + v)
        }
      }
    }

    actor {
      for (i <- 0 until count) {
        func
        getActor ! "get"
      }
    }
    //    logger.debug("actor create.")
  }

  def process(func: => Unit, eachThread: (=> Unit, Int) => Unit) {
    val start = System.currentTimeMillis();
    println("start. items " + items)

    val count = items / thread
    for (i <- 0 until thread) {
      eachThread(func, count)
    }

    countDown.await()
    val end = System.currentTimeMillis() - start
    println("times " + end + " (ms)")
    Thread.sleep(1000)
    client.shutdown()
  }

  def put() {
    client.set(queue, 0, v)
    countDown.countDown()
  }

  def get() {
    val v: String = client.get(queue)
    v
    countDown.countDown()
  }

  def putAndGet() {
    client.set(queue, 0, v);
  }

}