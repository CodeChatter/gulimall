## github 连接问题解决方案


- 校验连接

访问：https://www.githubstatus.com/ 检验状态

- ssl问题

git config --global http.sslVerify "false"

- 长连接问题

git config --global http.lowSpeedLimit 0

git config --global http.lowSpeedTime 600

- 缓存空间大小问题

git config --global http.postBuffer 5242880000

- 代理问题

git config --global --unset http.proxy

git config --global --unset https.proxy

