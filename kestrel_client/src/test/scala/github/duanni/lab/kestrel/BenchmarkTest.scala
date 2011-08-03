package github.duanni.lab.kestrel

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 *
 * @author duanni
 */
@RunWith(classOf[JUnitRunner])
class BenchmarkTest extends FunSuite {
  val servers = "192.168.17.234:22133"
  val logger = LoggerFactory.getLogger(getClass)
  val threads = List(10, 30, 50, 100, 200, 300, 1)
  //  val threads = List(5, 10)
  val values = List("x" * 64, "x" * 128, "x" * 512, "x" * 1024, "x" * 4096, "x" * 16384)
  //  val values = List("a" * 512, "x" * 1024)
  val queue = "spam"
  val totalItems = 500000

  test("kestrel client test") {
    val List(put, putAndGet) = Base(threads, values, queue, totalItems, servers)
    println("put and get")
    putAndGet.start()
    println("put")
    put.start()
  }
}