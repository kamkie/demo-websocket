package net.devopssolutions.demo.ws.server.util

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LoggingThreadPoolExecutor : ThreadPoolExecutor {
    private val log = org.slf4j.LoggerFactory.getLogger(LoggingThreadPoolExecutor::class.java)

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>)
    : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), CallerRunsPolicy()) {
    }

    override fun execute(command: Runnable) {
        super.execute(Runnable {
            try {
                command.run()
            } catch (e: Exception) {
                log.warn("uncached exception in", e)
            }
        })
    }
}
