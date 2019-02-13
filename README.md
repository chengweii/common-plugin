# 通用组件
## 【delay-job】延时任务组件（待开发）
基于 redis/数据库 实现的分布式延时任务组件。

## 【distributed-lock】分布式锁组件（已完成）
基于 redis setNx 实现，通过命令、注解方式使用分布式锁。

## 【distributed-tcc】分布式补偿事务(TCC)组件（已完成）
基于 数据库+定时任务+线程池 实现的准实时分布式补偿事务，通过定时任务轮询加载轮询周期内的待补偿事务，然后按照自定义的重试策略执行补偿动作。

## 【distributed-cache】分布式缓存组件（开发中）
基于 Redis 实现的分布式缓存组件，支持多级缓存、定时自动更新缓存、Cache-Aside模式缓存。

> 实现方案：  
1.用户自定义维护缓存KEY及各缓存KEY之间的关系；  
2.获取数据时首先到缓存KEY列表检查KEY是否存在，然后检查缓存是否存在，不存在则从数据库拉取数据，拉取时根据KEY做分布式锁，避免重复拉取。  
3.拉取到数据则缓存起来，同时维护缓存KEY到缓存KEY列表，然后直接返回用户。  
4.缓存存在则根据缓存KEY关系模糊匹配所有依赖的缓存是否失效，有效则直接返回，无效则重新从数据库拉取数据。
5.依托定时任务更新将要到期的缓存，避免透库。

## 【flow-filter】流量过滤组件（待开发）
基于 MQ和线程池 实现无效流量的过滤和有效流量的异步化削峰处理，避免无效流量直接进入浪费服务端资源以及均衡流量的处理。例如：竞拍最后一秒出价行为；秒杀库存不足时的无效请求行为；其他高峰流量请求的异步化处理。

## 【flow-limiter】流量控制组件（待开发）
封装阿里的 Sentinel，实现限流、降级、过载保护的流控功能，提供应用级的保护与监控。

## 【param-validator】参数验证组件（已完成）
提供了两种参数验证器，一种基于hibernate-validator的参数验证器，一种类似于Guava的Precondition的参数验证器。

## 【sharding-data】分库分表组件（待开发）
封装sharding-jdbc，实现大数据量表的分库分表。

## 【link-tracker】链路跟踪组件（待开发）
基于java-agent，实现本地调用堆栈的链路跟踪，完成性能监控等工作。

