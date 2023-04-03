# imitate_bilibili
大致目标是模仿bilibili实现基本的功能...强化自己编写业务API的能力及增加框架储备。
离完成还差得多的项目，还有很多的功能需要学习才能实现，希望自己可以坚持下去。<br>
目前采用了SpringBoot + Mybatis-plus + Minio<br>
后期预计会用上Dubbo(Feign) + Nacos + WebSocket对业务进行优化、解耦，搜索大概会使用ELK实现
但这也是后话了，在校学生，项目开发经验不足，一个很不规范的项目。
如果看到了能对您有所帮助，也是不错的。
### 每天能坚持完成一点就很好。
####2023/4/3 
debug--记录调试日志;info--生产级别会使用的，一般用于阶段性日志;warn--警告性的运行时异常;error--意料之外的异常。<br>
原本的日志切面采用了异步输出的方式，但考虑到生产会切换成info级别，那也没有必要了，完成大部分info日志转debug日志的重构<br>
由自定义线程池类转换为了使用Spring的@Aysnc的自定义线程池<br>
学习了DTO, VO, PO之间使用ObjectMapper可以快速转换...<br>
去除了不必要的Service接口...据说大部分的项目都用不到整个Service重构，不必担心<br>

