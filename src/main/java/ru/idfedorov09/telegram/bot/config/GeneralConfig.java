package ru.idfedorov09.telegram.bot.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class GeneralConfig {

    @Bean
    public Gson gson(){
        return new Gson();
    }

    private ThreadPoolTaskExecutor executor(int corePoolSize, int maxPoolSize, int queCapacity, String prefix){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queCapacity);
        executor.setThreadNamePrefix(prefix);
        return executor;
    }

    @Bean(name = "linearThread")
    public Executor linearThreadExecutor() {
        ThreadPoolTaskExecutor executor = executor(
                1,
                1,
                Integer.MAX_VALUE,
                "bot-linear-thread-"
        );
        executor.initialize();
        return executor;
    }


    @Bean(name = "infinityThread")
    public Executor infinityThreadExecutor() {
        ThreadPoolTaskExecutor executor = executor(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                "bot-async-thread-"
        );
        executor.initialize();
        return executor;
    }

}
