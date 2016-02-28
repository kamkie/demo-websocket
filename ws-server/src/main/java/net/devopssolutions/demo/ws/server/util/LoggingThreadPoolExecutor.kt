package net.devopssolutions.demo.ws.server.util

import java.util.concurrent.*

class LoggingThreadPoolExecutor : ThreadPoolExecutor {
    private val log = org.slf4j.LoggerFactory.getLogger(LoggingThreadPoolExecutor::class.java)

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) {
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, handler: RejectedExecutionHandler) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler) {
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory, handler: RejectedExecutionHandler) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler) {
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
