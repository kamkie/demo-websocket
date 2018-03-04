package net.devopssolutions.demo.ws.server.util

import mu.KLogging
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LoggingThreadPoolExecutor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>
) : ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        Executors.defaultThreadFactory(),
        CallerRunsPolicy()
) {
    companion object : KLogging()

    override fun execute(command: Runnable) {
        super.execute {
            try {
                command.run()
            } catch (e: Exception) {
                logger.warn("uncached exception in", e)
            }
        }
    }
}
