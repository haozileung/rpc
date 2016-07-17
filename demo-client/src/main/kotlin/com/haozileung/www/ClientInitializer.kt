package com.haozileung.www;

import com.haozileung.api.service.IEchoService
import com.haozileung.www.init.RpcClientConfig
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong


fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Main")
    val rootContext = AnnotationConfigApplicationContext()
    rootContext.addBeanFactoryPostProcessor(RpcClientConfig())
    logger.info("Started...")
    rootContext.refresh()
    val echoService: IEchoService = rootContext.getBean(IEchoService::class.java)
    val time = AtomicLong(0)
    val successCount = AtomicInteger(0)
    val failCount = AtomicInteger(0)
    val totalThread = 5
    val perThreadCount = 100000
    val echoSize = 4
    val totalCount = totalThread * perThreadCount
    val count = CountDownLatch(totalThread)
    for (j in 1..totalThread) {
        Thread(Runnable {
            for (i in 1..perThreadCount) {
                val data = RandomStringUtils.random(echoSize, true, true)
                val start = System.currentTimeMillis()
                val result = echoService.echo(data)
                val end = System.currentTimeMillis()
                if (data.equals(result)) {
                    successCount.incrementAndGet()
                } else {
                    failCount.incrementAndGet()
                    logger.error("fail on data: $data")
                }
                time.addAndGet((end - start))
            }
            count.countDown()
        }).run()
    }
    count.await()
    println("total count is $totalCount")
    println("total time is ${time.get()}")
    println("avg time is ${time.get() / totalCount.toDouble()}")
    println("successRate is ${successCount.toDouble() / totalCount.toDouble()}")
    println("failRate is ${failCount.toDouble() / totalCount.toDouble()}")
    println("tps is ${totalCount.toDouble() / (time.toDouble() / 1000)}")
    rootContext.close()
}
