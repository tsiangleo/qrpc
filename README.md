# qrpc
a simple rpc which supports synchronous call, asynchronous call, callback, future and timeout.

## feature
- 支持异步调用，提供future、callback的能力。
- 能够传输基本类型、自定义业务类型、异常类型。
- 要处理超时场景，服务端处理时间较长时，客户端在指定时间内跳出本次调用。
- 提供RPC上下文，客户端可以透传数据给服务端。
- 提供Hook，让开发人员进行RPC层面的AOP。
- 用zookeeper注册服务，实现负载均衡和故障转移。