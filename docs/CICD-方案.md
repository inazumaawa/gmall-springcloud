# gmall-springcloud CI/CD 方案（Jenkins 版）

> 目标：`git push` → 自动构建 → 打镜像 → 推 Harbor → 部署 K8s，全流程无人值守。
> 适用范围：`inazumaawa/gmall-springcloud` 仓库，K8s 集群命名空间 `my-springcloud`，Master 节点 `192.168.10.16`。

---

## 0. 为什么用 Jenkins 而不是 GitHub Actions

原方案用 GitHub Actions 自托管 Runner，但在 Master 节点（CentOS 7）上直接撞墙：

| 组件      | CentOS 7 提供               | 实际要求 | 后果                                                                                   |
| --------- | --------------------------- | -------- | -------------------------------------------------------------------------------------- |
| libstdc++ | GLIBCXX 3.4.19（GCC 4.8.5） | 3.4.21+  | `Runner.Listener: version GLIBCXX_3.4.21 not found`，Runner 本体起不来               |
| glibc     | 2.17                        | 2.28+    | 即使绕过第一道墙，`actions/checkout@v4`（Node 20 动作）仍报 `GLIBC_2.28 not found` |

GitHub 官方支持列表里 Linux 最低是 **CentOS 8 / RHEL 8**，CentOS 7 属不支持范围。要在它上面救活 Runner，等于同时升级 libstdc++ 和 glibc 两个核心库，风险极高。

**Jenkins 天然绕开这个问题**：Jenkins 是纯 JVM 应用，只依赖 glibc 2.17 + 一个能跑的 JDK（Temurin 21 的 Linux x64 二进制正是以 CentOS 7.9 为基线构建的）。Action 那套"插件用 Node 20 执行"的机制在 Jenkins 里也不存在——流水线脚本直接跑 shell。

代价：需要自己维护构建机环境（JDK / Maven / Node），以及 GitHub 无法回调内网 Jenkins，触发方式改为**轮询**。

---

## 1. 总体架构

```
┌──────────────────────────────────────────────────────────────────────┐
│ 开发机 (Windows)                                                      │
│   git push  ──SSH──►  GitHub  inazumaawa/gmall-springcloud            │
└───────────────────────────────▲──────────────────────────────────────┘
                                │ Jenkins 定时轮询（出站，无需公网入口）
                                │ git fetch
┌───────────────────────────────┴──────────────────────────────────────┐
│ K8s Master 192.168.10.16  （Harbor 宿主机 + Jenkins 宿主机）            │
│                                                                       │
│  ┌────────────────────────┐      docker push      ┌────────────────┐  │
│  │ Jenkins :8081          │ ────────────────────► │ Harbor :9090   │  │
│  │  · mvn package (JDK21) │                       │ project: gmall │  │
│  │  · docker build        │                       └───────┬────────┘  │
│  │  · kubectl apply       │                               │ pull      │
│  └───────────┬────────────┘                               ▼           │
│              │ kubectl apply                     ┌──────────────────┐ │
│              └─────────────────────────────────► │ K8s my-springcloud│ │
│                                                   │ 15 个微服务+前端 │ │
│                                                   │ Nacos/Redis/MySQL│ │
│                                                   └──────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

**为什么必须用内网构建节点**：Harbor 与 K8s 都在 `192.168.10.x` 内网。Jenkins 直接装在 Master 节点上，拉代码、构建、推镜像、部署全在内网完成，Harbor/K8s 无需暴露公网。

**触发方式的取舍**：

| 方式             | 前提                                                     | 建议                                  |
| ---------------- | -------------------------------------------------------- | ------------------------------------- |
| Poll SCM（轮询） | 只需 Jenkins 能出网访问 GitHub                           | **本方案默认**，`H/5 * * * *` |
| GitHub Webhook   | GitHub 能反向访问 Jenkins（需公网 IP/端口映射/内网穿透） | 有公网入口时再启用，实时性更好        |

---

## 2. 现状盘点

### 2.1 已具备

| 项           | 现状                                                                                                                    |
| ------------ | ----------------------------------------------------------------------------------------------------------------------- |
| 代码仓库     | `git@github.com:inazumaawa/gmall-springcloud.git`，分支 `main`，首次提交 `885b4d0`                                |
| 构建         | 根`pom.xml` 聚合 16 个模块（`common` 为依赖库，不单独出镜像）                                                       |
| Dockerfile   | 15 个 Java 服务各有独立 Dockerfile（`eclipse-temurin:17-jdk-alpine`）+ 前端 `mi.com/Dockerfile`（`nginx:alpine`） |
| K8s 清单     | `k8s/*.yaml` 共 16 个（15 服务 + frontend）+ `ingress.yaml`                                                         |
| 基础组件     | Nacos / Redis / MySQL / OBS 已部署在`my-springcloud`                                                                  |
| 内网端口约定 | gateway`30080`、frontend `30088`、nacos `30848/30948`                                                             |

### 2.2 服务与端口对照表（流水线构建矩阵的依据）

| 镜像名              | Maven 模块              | 产物 jar                                 | 容器端口 | K8s Deployment      |
| ------------------- | ----------------------- | ---------------------------------------- | -------- | ------------------- |
| gateway             | `gateway`             | `gateway-1.0-SNAPSHOT.jar`             | 8080     | gateway             |
| auth                | `auth`                | `auth-1.0-SNAPSHOT.jar`                | 8081     | auth                |
| goods               | `Goods`               | `Goods-1.0-SNAPSHOT.jar`               | 8082     | goods               |
| carts               | `carts`               | `carts-1.0-SNAPSHOT.jar`               | 8083     | carts               |
| order               | `order`               | `order-1.0-SNAPSHOT.jar`               | 8084     | order               |
| pay                 | `pay`                 | `pay-1.0-SNAPSHOT.jar`                 | 8085     | pay                 |
| address             | `address`             | `address-1.0-SNAPSHOT.jar`             | 8086     | address             |
| favorites           | `favorites`           | `favorites-1.0-SNAPSHOT.jar`           | 8087     | favorites           |
| review              | `review`              | `review-1.0-SNAPSHOT.jar`              | 8088     | review              |
| user-center         | `user-center`         | `user-center-1.0-SNAPSHOT.jar`         | 8089     | user-center         |
| admin               | `admin`               | `admin-1.0-SNAPSHOT.jar`               | 8090     | admin               |
| coupon              | `coupon`              | `coupon-1.0-SNAPSHOT.jar`              | 8091     | coupon              |
| ai-customer-service | `ai_customer_service` | `ai_customer_service-1.0-SNAPSHOT.jar` | 8092     | ai-customer-service |
| obs                 | `obs`                 | `obs-1.0-SNAPSHOT.jar`                 | 8093     | obs                 |
| customer-ws         | `customer_ws`         | `customer_ws-1.0-SNAPSHOT.jar`         | 8094     | customer-ws         |
| frontend            | `mi.com`              | `dist/`（Vite 构建产物）               | 80       | frontend            |

> 注意：模块目录首字母大写的 `Goods`，其 jar 名也是 `Goods-1.0-SNAPSHOT.jar`；镜像名统一小写 `goods`，与 K8s 清单保持一致。

### 2.3 缺口（实施前需补齐）

| # | 缺口                                                              | 影响                                                         | 处理建议                                                                                                                                                         |
| - | ----------------------------------------------------------------- | ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1 | `customer_ws` 模块**没有 Dockerfile，也没有 k8s 清单**    | WebSocket 客服功能无法部署                                   | **已处理**：补 `customer_ws/Dockerfile`（端口 8094）、`k8s/customer-ws.yaml`、`nacos_config-k8s/customer-ws-server-dev.yml`，k8s 版 `gateway-dev.yml` 增加 `/chat/**`、`/ws/**` 路由，并加入流水线矩阵 |
| 2 | `nacos_config/`、`nacos_config-k8s/` 已被 `.gitignore` 排除 | 新集群无法从仓库还原配置                                     | **不纳入 CI/CD 自动同步**（见 2.4 说明）；保持 gitignore（含明文 MySQL 密码 / SMTP 授权码 / API Key / 支付宝私钥），配置变更走人工导入或手动触发的独立 Job |
| 3 | `k8s/*.md`（mysql/nacos/redis）混在清单目录                     | `kubectl apply -f k8s/` 可能报 `no recognized extension` | **已处理**：转成合法多文档 YAML（补 `---`）并移入 `k8s/infra/`，见 8.3；`k8s/` 只保留业务清单                                                        |
| 4 | 老脚本`k8s/build-images.sh` 指向阿里云镜像仓库                  | 与 Harbor 方案冲突                                           | **已处理**：确认废弃，直接删除（功能由 Jenkinsfile 的 build/push 阶段替代）                                                                                |
| 5 | 各服务`bootstrap.yml` 依赖环境变量注入 Nacos 地址               | 本地与集群行为不一致                                         | 集群已在 Deployment 的`env` 里注入，保持现状即可                                                                                                               |

### 2.4 关于 Nacos 配置是否纳入 CI/CD（结论：不自动同步）

`nacos_config/`（本地）与 `nacos_config-k8s/`（集群）是同一批配置的两份导出副本，**服务端 Nacos 才是唯一事实来源**。流水线不推送配置，理由三条：

| 理由                           | 说明                                                                                                                           |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------ |
| 配置是运行时状态，不是构建产物 | 每次代码 push 都回写配置，会把运维在 Nacos 控制台上的调整覆盖掉，属于高危操作                                                  |
| 变更频率与评审方式不同         | 配置改动少、需人工确认；代码提交频繁、可自动执行，两者不应共用一条流水线                                                       |
| 密钥不能进仓库                 | 这些 yml 含明文 MySQL 密码 / SMTP 授权码 / 百炼 API Key / 支付宝私钥，`.gitignore` 已排除，若纳入 CI/CD 就必须入库，不可接受 |

**什么时候需要人工导入**：只在新集群重建、或新增了服务的 dataId 时。做法是登录 Nacos 控制台（`30848`）用「导入配置」逐份上传 `nacos_config-k8s/*.yml`；若想脚本化，可用 Nacos OpenAPI 写一个**手动触发**的独立 Job（不要挂在 push 触发的流水线上）：

```bash
curl -X POST 'http://<nacos-svc>:8848/nacos/v1/cs/configs' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'dataId=gateway-dev.yml' -d 'group=DEFAULT_GROUP' -d 'type=yaml' \
  -d 'username=<nacos账号>' -d 'password=<nacos密码>' \
  --data-urlencode 'content@gateway-dev.yml'
```

> 关于 `customer_ws`：k8s 侧缺的 `customer-ws-server-dev.yml` 已补到 `nacos_config-k8s/`，同时 k8s 版 `gateway-dev.yml` 也补上了 `/chat/**`、`/ws/**` 两条路由与 `/ws/**` 白名单。**新集群重建时这两份要一起导入**，否则网关会返回 404。

---

## 3. 阶段一：搭建 Harbor 私有仓库

### 3.1 前置检查

```bash
# Master 节点上确认 docker 与 compose 可用
docker version
docker compose version || docker-compose version

# 确认 80/443 留给 Ingress、9090 是否空闲（Harbor 自带 nginx，默认绑 80，与 Ingress 的 80/443 冲突）
ss -lntp | grep -E ':(80|443|9090)\s'

# 顺便确认 Jenkins 要用的 8081 没被占用
ss -lntp | grep ':8081\s' || echo "8081 空闲"
```

> **关键点**：Master 上 Nginx Ingress 占用 `80/443`，而 Harbor 默认也用 80，二者不能共存 → **Harbor 统一改用 `9090`**，全文所有镜像地址均写作 `192.168.10.16:9090/<项目>/<镜像名>`。若你实际用的是其他端口，请把下文所有 `9090` 替换为实际值。

### 3.2 安装（HTTP 模式，内网够用）

```bash
# 1. 下载离线安装包（版本按需替换）
cd /opt
wget https://github.com/goharbor/harbor/releases/download/v2.14.0/harbor-offline-installer-v2.14.0.tgz
tar xzvf harbor-offline-installer-v2.14.0.tgz

# 2. 准备配置
cd /opt/harbor
cp harbor.yml.tmpl harbor.yml
```

编辑 `harbor.yml`，只保留 HTTP、改掉冲突端口：

```yaml
hostname: 192.168.10.16
http:
  port: 9090            # 必改！默认 80 会与 Ingress 冲突
# https:                # 整段注释掉（内网不启用 TLS）
data_volume: /data/harbor
harbor_admin_password: <你的管理员密码>
```

```bash
# 3. 安装并启动
./install.sh

# 4. 验证
docker ps | grep harbor
curl -I http://192.168.10.16:9090
```

> **已经是「80 端口」的 Harbor 想改到 9090**：改完 `harbor.yml` 后执行 `./prepare && docker compose up -d`（`./prepare` 会按新端口重新生成 Harbor 内部 nginx 配置），再按 3.4 节把宿主机与各节点的 registry 配置同步到新端口。**已推送的镜像不会丢**——Harbor 按「项目/仓库名」存储，仓库名里不含端口号。

### 3.3 建项目与机器人账号

1. 浏览器打开 `http://192.168.10.16:9090`，用 `admin` 登录；
2. **项目 → 新建项目**：名称 `gmall`，访问级别**私有**；
3. 进入 `gmall` → **机器人账户 → 新建**：名称 `ci`，权限勾选 `推送` + `拉取`；
4. 记下生成的 **用户名**（形如 `robot$gmall+ci`）与 **Token**，只显示一次，后面配到 Jenkins 凭据。

### 3.4 各节点放行 HTTP 仓库

**Jenkins/Master 宿主机（docker 引擎）** —— `/etc/docker/daemon.json`：

```json
{
  "insecure-registries": ["192.168.10.16:9090"]
}
```

```bash
systemctl restart docker
```

**K8s 工作节点（containerd 运行时）** —— `/etc/containerd/config.toml`：

```toml
[plugins."io.containerd.grpc.v1.cri".registry.mirrors."192.168.10.16:9090"]
  endpoint = ["http://192.168.10.16:9090"]

[plugins."io.containerd.grpc.v1.cri".registry.configs."192.168.10.16:9090".tls]
  insecure_skip_verify = true
```

```bash
systemctl restart containerd
```

**集群侧拉取凭据**（每个命名空间一次）：

```bash
kubectl -n my-springcloud create secret docker-registry harbor-cred \
  --docker-server=192.168.10.16:9090 \
  --docker-username='robot$gmall+ci' \
  --docker-password='<机器人账号 Token>'
```

---

## 4. 阶段二：在 Master 节点安装 Jenkins

位置：K8s **Master 节点**（`192.168.10.16`），这里同时有 `docker`、`kubectl` 和内网可达的 Harbor。

### 4.1 版本选型（决定了 JDK 版本）

| 组件    | 选型                            | 理由                                                                                                                                                                      |
| ------- | ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| JDK     | **Temurin 21**（tarball） | CentOS 7 官方源最高只有`java-11-openjdk`，而 Jenkins LTS 2.479.1 之后**必须 Java 17 或 21**；Temurin 21 的 Linux x64 二进制正是以 CentOS 7.9 为基线构建，可直接跑 |
| Jenkins | **LTS（war 包）**         | 不依赖 yum 源，CentOS 7 已 EOL，用 war 最省事                                                                                                                             |

> 若想省事，也可临时用一个支持 Java 17 的旧 LTS（如 2.541.x，已于 2026-04 EOL）；不建议，直接用 21 更省心。

### 4.2 安装 JDK 21

```bash
cd /opt

# 通过 Adoptium 官方 API 取当前最新 JDK 21 的 Linux x64 tarball 地址（避免手抄版本号）
JDK_URL=$(curl -fsSL "https://api.adoptium.net/v3/assets/latest/21/hotspot?os=linux&architecture=x64&image_type=jdk&vendor=eclipse" \
          | grep -o 'https://github.com/[^"]*\.tar\.gz' | head -n1)
echo "$JDK_URL"
curl -fL -o /tmp/jdk21.tar.gz "$JDK_URL"

mkdir -p /opt/jdk
tar xzf /tmp/jdk21.tar.gz -C /opt/jdk
JDK_DIR=$(ls -d /opt/jdk/jdk-21* | head -n1)
ln -sfn "$JDK_DIR" /opt/jdk/current

/opt/jdk/current/bin/java -version     # 应打印 openjdk version "21.x"

# 写入系统级环境变量：脚本内的 export 只作用于脚本子进程，脚本退出即失效，必须落到启动文件
cat >/etc/profile.d/java.sh <<'EOF'
# JDK 21 (Temurin)
export JAVA_HOME=/opt/jdk/current
export PATH=$JAVA_HOME/bin:$PATH
EOF
chmod 644 /etc/profile.d/java.sh

source /etc/profile.d/java.sh          # 当前 shell 立即生效；新开的 shell 会自动读取
java -version                          # 这次不加绝对路径也应能打印
echo "$JAVA_HOME"                      # 期望 /opt/jdk/current
```

> **原理**：`./java-install.sh` 是当前 shell fork 出的子进程，脚本里 `export PATH=...` 只改了子进程自己的环境表，脚本退出即随进程销毁，父 shell 从头到尾没被影响。环境变量要持久化，只能写进 shell 的**启动文件**——`/etc/profile.d/*.sh` 会被所有 login shell（SSH 登录、`su -`、`bash -l`）自动执行。

> **与 Jenkins 无关**：第 4.4 节的 systemd 单元已显式声明 `JAVA_HOME` 与 `PATH`，即使跳过这步，Jenkins 服务照样能起来。这里配的是给你敲命令、以及第 4.6 节 `sudo -u jenkins bash -lc` 自检用的。

### 4.3 安装 Maven

把下面的内容整段写进 `/root/maven.sh`（heredoc 里的 `EOF` 顶格，不要缩进）：

```bash
#!/bin/bash
# 用途：安装 Maven 到 /opt 并写入 /etc/profile.d/maven.sh（幂等，可重复执行）
set -e
cd /opt

# ---- 1) 取版本号：读中央仓库元数据，只保留 3.9.x 正式版（排除 rc/alpha/beta）----
# 不能用 dlcdn 目录页抓版本号：dlcdn 只留每线最新版，且页面里会混入 3.10.0-rc-1 这类无正式发行包的版本号
MVN_VER=$(curl -fsSL https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/maven-metadata.xml \
          | grep -oE '<version>3\.9\.[0-9]+</version>' | grep -oE '3\.9\.[0-9]+' | sort -V | tail -n1)
[ -n "$MVN_VER" ] || MVN_VER=3.9.16          # 网络不通时的兜底版本
echo "即将安装 Maven $MVN_VER"

# ---- 2) 下载：阿里云中央镜像优先，失败回退 Maven 中央仓库（两者都永久保留所有版本，不会 404）----
FILE="apache-maven-${MVN_VER}-bin.tar.gz"
rm -f /tmp/maven.tar.gz
curl -fL -o /tmp/maven.tar.gz \
  "https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/${MVN_VER}/${FILE}" \
  || curl -fL -o /tmp/maven.tar.gz \
  "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/${MVN_VER}/${FILE}"

# 校验拿到的是有效 tar.gz，避免把 404 页面当成包解开
tar tzf /tmp/maven.tar.gz >/dev/null || { echo "下载文件损坏，请重试"; exit 1; }

# ---- 3) 解包 + 软链 ----
tar xzf /tmp/maven.tar.gz -C /opt
ln -sfn "/opt/apache-maven-${MVN_VER}" /opt/maven

# ---- 4) 用绝对路径自检（此刻 PATH 还没生效，靠 JAVA_HOME 兼容 Java 21）----
JAVA_HOME=/opt/jdk/current /opt/maven/bin/mvn -v

# ---- 5) 写入 /etc/profile.d（profile.d 按文件名排序加载，java.sh 先于 maven.sh，故 JAVA_HOME 已就位）----
cat >/etc/profile.d/maven.sh <<'EOF'
# Maven
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
EOF
chmod 644 /etc/profile.d/maven.sh
```

脚本是子进程，跑完当前 shell 的 `PATH` 不会变，所以还要补一条让当前 shell 生效：

```bash
source /etc/profile.d/maven.sh
mvn -v                                 # 应打印 Apache Maven 3.9.x + Java version: 21 + Java home: /opt/jdk/current
```

> **为什么不用 dlcdn + archive 组合**：dlcdn 是「每线只留最新版」的发布镜像，`apache-maven-${MVN_VER}-bin.tar.gz` 一旦版本下线就 404；而中央仓库（`repo1.maven.org`，阿里云镜像同源）对 Maven 本体是永久留存、带校验和的历史归档，版本号也直接来自 `maven-metadata.xml`，不会再抓到 RC 版本。

### 4.4 安装 Jenkins

```bash
# 1. 专用用户
useradd -r -m -d /var/lib/jenkins -s /bin/bash jenkins
usermod -aG docker jenkins          # 让 jenkins 能用宿主 docker
id jenkins                          # 确认输出里含 docker 组

# 2. 下载 LTS war
mkdir -p /opt/jenkins
curl -fL -o /opt/jenkins/jenkins.war https://get.jenkins.io/war-stable/latest/jenkins.war
ls -lh /opt/jenkins/jenkins.war

# 3. 字体依赖（不可跳过）
# 原因：Java 启动 WebApp 时要通过系统 fontconfig 初始化字体；CentOS 7 最小化安装不带 fontconfig 与任何字体，
#       Jenkins 会抛 java.lang.RuntimeException: Fontconfig head is null → Failed to start Jetty → 8081 无监听。
#       注意 -Djava.awt.headless=true 只表示"不弹图形窗口"，并不代表不需要字体库。
yum install -y fontconfig dejavu-sans-fonts dejavu-serif-fonts
fc-cache -fv
fc-list | head -5                   # 能列出字体即正常

# 4. systemd 单元
cat >/etc/systemd/system/jenkins.service <<'EOF'
[Unit]
Description=Jenkins CI
After=network-online.target docker.service
Wants=network-online.target
Requires=docker.service

[Service]
Type=simple
User=jenkins
Group=jenkins
Environment="JAVA_HOME=/opt/jdk/current"
Environment="JENKINS_HOME=/var/lib/jenkins"
Environment="PATH=/opt/jdk/current/bin:/opt/maven/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/bin"
ExecStart=/opt/jdk/current/bin/java -Djava.awt.headless=true -Xmx1024m \
          -jar /opt/jenkins/jenkins.war --httpPort=8081
Restart=on-failure
RestartSec=10
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
EOF

# 5. 启动
systemctl daemon-reload
systemctl enable --now jenkins

# 6. 等首次启动完成（war 96M，要先解压内置插件，30~60s 后 secrets/ 目录才会出现）
for i in $(seq 1 30); do
  [ -f /var/lib/jenkins/secrets/initialAdminPassword ] && break
  sleep 5
done
systemctl is-active jenkins                                   # 期望 active
journalctl -u jenkins --no-pager | grep -m1 "fully up and running"

# 7. 初始密码
cat /var/lib/jenkins/secrets/initialAdminPassword

# 若仍无该文件或服务不是 active，用日志定位原因（重点看最后一个 Caused by）
systemctl show jenkins -p NRestarts -p SubState
journalctl -u jenkins --no-pager | grep -i "caused by"

# 8. 放行 8081（CentOS 7 默认开启 firewalld，未放行的端口从外部访问会被静默丢包 = 浏览器一直转圈）
firewall-cmd --permanent --add-port=8081/tcp
firewall-cmd --reload
firewall-cmd --list-ports               # 应含 8081/tcp

# 9. 确认对外 IP（文档里的 192.168.10.16 是示例值，以实际为准），并验证监听已在 0.0.0.0
ip a | grep -w inet
ss -lntp | grep ':8081'                 # 应显示 0.0.0.0:8081 或 :::8081，若只有 127.0.0.1 则是绑定问题
```

浏览器打开 `http://192.168.10.16:8081`，粘贴初始密码 → **不要点「安装推荐的插件」** → 选 **选择插件来安装** → 点上方「无」取消全选 → 安装（秒级完成）→ 创建管理员账号 → 实例地址保持默认。

> **为什么跳过推荐插件**：向导里的「安装推荐的插件」要连官方 `updates.jenkins.io` 下载约 100 个插件，国内直连基本会卡在进度条上一动不动（服务本身没坏，日志里早已打印 `Jenkins is fully up and running`）。正确顺序是：先跳过、把 Jenkins 用起来，再按 4.5.1 确认或更换更新源，然后按需装插件。

### 4.5 更换/确认插件更新源并补装插件

#### 4.5.1 确认/更换插件更新中心

Jenkins 默认源是官方 `https://updates.jenkins.io/update-center.json`。**先验证，再改**——写进一个不可达的地址，向导页会因为拿不到插件元数据而一直转圈，表现成「网页打不开」。

```bash
# 先 curl 探测目标源，返回 200 才写进配置
curl -sI https://updates.jenkins.io/update-center.json | head -1          # 官方源

# 国内直连官方源慢时，可换镜像；注意镜像目录结构会变，务必先用 curl 验证，
# 返回 404 说明路径已失效，不要写进配置（写错会导致向导页卡死）
curl -sI https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json | head -1
curl -sI https://mirrors.ustc.edu.cn/jenkins/updates/update-center.json | head -1
```

验证通过后按下面改（URL 换成你验证可用的那个）：

```bash
systemctl stop jenkins

cat >/var/lib/jenkins/hudson.model.UpdateCenter.xml <<'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<sites>
  <site>
    <id>default</id>
    <url>https://updates.jenkins.io/update-center.json</url>
  </site>
</sites>
EOF
chown jenkins:jenkins /var/lib/jenkins/hudson.model.UpdateCenter.xml

systemctl start jenkins
sleep 40
systemctl is-active jenkins
curl -s -o /dev/null -w "本机 HTTP 状态：%{http_code}\n" http://127.0.0.1:8081/login
```

> 验证是否生效：**Manage Jenkins → Plugins → Advanced settings** 最下方能看到 Update Site 的 URL。
> 排查经验：向导页/插件页卡住不一定是服务挂了，先用 `journalctl -u jenkins --no-pager | grep -m1 "fully up and running"` 确认服务已就绪，再查更新源可达性。

#### 4.5.2 按需安装插件

**Manage Jenkins → Plugins → Available plugins**，搜索并勾选：

| 插件                           | 用途                             |
| ------------------------------ | -------------------------------- |
| Pipeline (Workflow Aggregator) | 声明式流水线                     |
| Git / Git client               | 拉取仓库                         |
| Credentials Binding            | `withCredentials` 注入账号密码 |
| Timestamper                    | `timestamps()` 时间戳          |
| Workspace Cleanup              | 工作区清理                       |
| Docker Pipeline（可选）        | 容器化构建步骤                   |
| Localization: Chinese（可选）  | 界面中文                         |

### 4.6 让 Jenkins 能操作集群

```bash
# 1. 先确认 kubeconfig 的真实位置（不要写死路径）
#    kubeadm 初始化后，原始文件在 /etc/kubernetes/admin.conf；
#    /root/.kube/config 只是一份「可选副本」，很多环境（例如本次部署）根本没有生成它
ls -l /root/.kube/config /etc/kubernetes/admin.conf 2>&1

# 2. 把集群访问凭据交给 jenkins 用户
#    Jenkins 以 jenkins 身份运行，读的是 /var/lib/jenkins，而不是 /root
mkdir -p /var/lib/jenkins/.kube
cp /etc/kubernetes/admin.conf /var/lib/jenkins/.kube/config   # 若第 1 步显示 /root/.kube/config 存在，用它亦可
chown -R jenkins:jenkins /var/lib/jenkins/.kube
chmod 600 /var/lib/jenkins/.kube/config     # 内含客户端私钥，必须 600

# 3. 放行 SELinux 对 jenkins 家目录的限制（若 SELinux 为 Enforcing）
getenforce
# Enforcing 时如后续 git/docker 报 Permission denied，执行：
# setenforce 0    （或按需为 /var/lib/jenkins 打标签）

# 4. 自检（以 jenkins 身份执行）
#    cd /tmp 不可省略：bash -lc 会继承当前工作目录，若你此刻恰好在 /root 下，
#    Maven 会因读不到 /root 而打印「cd: /root: 权限不够」这种与真正问题无关的噪音
sudo -u jenkins bash -lc 'cd /tmp && java -version; mvn -v; docker ps >/dev/null && echo "docker OK"; kubectl -n my-springcloud get deploy'
```

> `kubectl` 权限：直接复用 `/etc/kubernetes/admin.conf`（cluster-admin）最省事；若要收权限，可为 jenkins 单独签发只允许 `my-springcloud` 命名空间写操作的 kubeconfig。
>
> 排障锚点：第 4 步若报 `The connection to the server localhost:8080 was refused`，含义是 **kubectl 没找到 kubeconfig**（退化成了默认的 localhost:8080），回到第 1 步核对文件位置、属主是否为 jenkins。

### 4.7 Maven 国内镜像（强烈建议）

```bash
sudo -u jenkins mkdir -p /var/lib/jenkins/.m2
cat >/var/lib/jenkins/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>aliyun maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
chown -R jenkins:jenkins /var/lib/jenkins/.m2
```

---

## 5. 阶段三：镜像命名与标签规范

| 项         | 规范                                                                                             |
| ---------- | ------------------------------------------------------------------------------------------------ |
| 仓库地址   | `192.168.10.16:9090/gmall/<镜像名>`                                                            |
| 标签       | `<git-sha 前 7 位>`（不可变，用于审计与精确回滚）+ `latest`（清单引用）                      |
| 基础镜像   | Java：`eclipse-temurin:17-jdk-alpine`；前端：`nginx:alpine`                                  |
| 构建上下文 | Java 服务：`<模块>/target`（jar 所在目录）；前端：`mi.com`（含 `dist/` 与 `nginx.conf`） |
| 拉取策略   | `imagePullPolicy: Always`（配合 `latest` 保证每次都拉新）                                    |

---

## 6. 阶段四：Jenkins 凭据与任务配置

### 6.1 凭据清单

**Manage Jenkins → Credentials → System → Global credentials → Add Credentials**：

| ID              | Kind                          | 内容                                                                                                | 用途                      |
| --------------- | ----------------------------- | --------------------------------------------------------------------------------------------------- | ------------------------- |
| `github-ssh`  | SSH Username with private key | Username 填`git`；Private Key 粘贴开发机上 `~/.ssh/id_ed25519` 的**完整内容**（含首尾行） | 拉取私有仓库              |
| `harbor-cred` | Username with password        | 用户名`robot$gmall+ci`，密码为机器人 Token                                                        | `docker login` 推送镜像 |

> - `github-ssh` 用的就是之前为 GitHub 生成的那对 ed25519 密钥，公钥已在 GitHub 账号里，私钥在 `C:\Users\Hibiki\.ssh\id_ed25519`。
> - 不想用 SSH 的话，可改用 Kind = `Username with password`（用户名填 GitHub 账号、密码填 PAT），并在任务里把仓库地址换成 HTTPS 形式。

### 6.2 新建流水线任务

1. **新建任务** → 名称 `gmall-ci-cd` → 类型选 **Pipeline** → OK；
2. **General**：勾选 *Discard old builds*，保持 30 份（Jenkinsfile 里也配了）；
3. **Build Triggers**：勾选 **Poll SCM**，日程填 `H/5 * * * *`（每 5 分钟检查一次，`H` 让时间点散列，避免整点拥堵）；
4. **Pipeline**：
   - Definition：**Pipeline script from SCM**
   - SCM：**Git**
   - Repository URL：`git@github.com:inazumaawa/gmall-springcloud.git`
   - Credentials：`github-ssh`
   - Branches to build：`*/main`
   - Script Path：`Jenkinsfile`
5. 保存。

> 首次必须在 Jenkins 内置节点跑一次 `git ls-remote` 验证凭据可用：任务页点 **立即构建** 即可，若报 `Permission denied (publickey)` 说明私钥粘错或公钥没在 GitHub。

### 6.3 关于 Webhook（可选）

若之后给 Jenkins 配了公网入口（或内网穿透），可改为实时触发：

1. 装 **GitHub** 插件；
2. 任务里勾选 *GitHub hook trigger for GITScm polling*；
3. GitHub 仓库 → Settings → Webhooks → Add webhook，Payload URL 填 `http://<公网地址>/github-webhook/`，Content type 选 `application/json`。

内网环境默认用 **Poll SCM** 即可，无需暴露端口。

---

## 7. 阶段五：Jenkinsfile 全文

文件位置：仓库根目录 [Jenkinsfile](../Jenkinsfile)（任务里的 Script Path 指的就是它），已随本次改动落库。

> 下面的全文与仓库中的 `Jenkinsfile` 保持一致；若两者冲突，**以仓库中的文件为准**。

```groovy
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()                                  // 部署串行，避免并发滚动互相打断
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 60, unit: 'MINUTES')
    }

    environment {
        HARBOR_REGISTRY = '192.168.10.16:9090'
        HARBOR_PROJECT  = 'gmall'
        K8S_NAMESPACE   = 'my-springcloud'
        JAVA_HOME       = '/opt/jdk/current'
        MAVEN_HOME      = '/opt/maven'
        PATH            = '/opt/jdk/current/bin:/opt/maven/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/bin'
        SERVICES        = 'gateway auth goods carts order pay address favorites review user-center admin coupon ai-customer-service obs customer-ws frontend'
    }

    triggers {
        pollSCM('H/5 * * * *')                                     // 内网无法被 GitHub 回调，用轮询
    }

    stages {

        stage('准备') {
            steps {
                script {
                    env.IMAGE_TAG = env.GIT_COMMIT.take(7)
                }
                echo "构建号 ${env.BUILD_NUMBER} / 代码版本 ${env.IMAGE_TAG}"
                sh 'java -version; mvn -v'
            }
        }

        // ---------- 1. 构建 ----------
        stage('Maven 打包') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('构建前端产物') {
            steps {
                // CentOS 7 的 glibc 2.17 跑不了 Node 20，用容器构建，产物落回工作区
                sh '''#!/bin/bash
                    set -e
                    docker run --rm \
                      -v "$WORKSPACE/mi.com":/app:z \
                      -w /app \
                      node:20-bullseye-slim \
                      sh -c "npm ci && npm run build"
                    ls -l "$WORKSPACE/mi.com/dist" | head
                '''
            }
        }

        // ---------- 2. 构建并推送镜像 ----------
        stage('登录 Harbor') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'harbor-cred',
                                                  usernameVariable: 'HARBOR_USER',
                                                  passwordVariable: 'HARBOR_PASS')]) {
                    sh 'echo "$HARBOR_PASS" | docker login "$HARBOR_REGISTRY" -u "$HARBOR_USER" --password-stdin'
                }
            }
        }

        stage('构建并推送后端镜像') {
            steps {
                sh '''#!/bin/bash
                    set -e
                    services=(
                      "gateway:gateway:gateway-1.0-SNAPSHOT.jar"
                      "auth:auth:auth-1.0-SNAPSHOT.jar"
                      "goods:Goods:Goods-1.0-SNAPSHOT.jar"
                      "carts:carts:carts-1.0-SNAPSHOT.jar"
                      "order:order:order-1.0-SNAPSHOT.jar"
                      "pay:pay:pay-1.0-SNAPSHOT.jar"
                      "address:address:address-1.0-SNAPSHOT.jar"
                      "favorites:favorites:favorites-1.0-SNAPSHOT.jar"
                      "review:review:review-1.0-SNAPSHOT.jar"
                      "user-center:user-center:user-center-1.0-SNAPSHOT.jar"
                      "admin:admin:admin-1.0-SNAPSHOT.jar"
                      "coupon:coupon:coupon-1.0-SNAPSHOT.jar"
                      "ai-customer-service:ai_customer_service:ai_customer_service-1.0-SNAPSHOT.jar"
                      "obs:obs:obs-1.0-SNAPSHOT.jar"
                      "customer-ws:customer_ws:customer_ws-1.0-SNAPSHOT.jar"
                    )
                    for s in "${services[@]}"; do
                      IFS=: read -r name module jar <<< "$s"
                      IMG="$HARBOR_REGISTRY/$HARBOR_PROJECT/$name"
                      echo "==> $name  (模块 $module / $jar)"
                      docker build -f "$module/Dockerfile" "$module/target" \\
                        -t "$IMG:$IMAGE_TAG" -t "$IMG:latest"
                      docker push "$IMG:$IMAGE_TAG"
                      docker push "$IMG:latest"
                    done
                '''
            }
        }

        stage('构建并推送前端镜像') {
            steps {
                sh '''#!/bin/bash
                    set -e
                    IMG="$HARBOR_REGISTRY/$HARBOR_PROJECT/frontend"
                    docker build -f mi.com/Dockerfile mi.com -t "$IMG:$IMAGE_TAG" -t "$IMG:latest"
                    docker push "$IMG:$IMAGE_TAG"
                    docker push "$IMG:latest"
                '''
            }
        }

        // ---------- 3. 部署 ----------
        stage('部署到 K8s') {
            steps {
                sh '''#!/bin/bash
                    set -e
                    kubectl apply -n "$K8S_NAMESPACE" -f k8s/
                    for d in $SERVICES; do
                      kubectl -n "$K8S_NAMESPACE" rollout restart "deployment/$d"
                    done
                    for d in $SERVICES; do
                      kubectl -n "$K8S_NAMESPACE" rollout status "deployment/$d" --timeout=240s
                    done
                '''
            }
        }
    }

    post {
        always {
            sh 'kubectl -n my-springcloud get deploy -o wide || true; kubectl -n my-springcloud get pods -o wide || true'
        }
        failure {
            echo '构建失败。排查顺序：阶段日志 → kubectl describe pod → Harbor 是否有新 tag → 凭据是否过期'
        }
        success {
            echo "部署完成，镜像标签 ${env.IMAGE_TAG}"
        }
    }
}
```

### 7.1 四点设计说明

- **镜像引用方式**：清单里写 `:latest` + `imagePullPolicy: Always`，流水线用 `apply` + `rollout restart` 触发重拉。
  优点是无占位符、可手动 `kubectl apply -f k8s/`、每次部署只滚动一次。
  代价是集群里运行的镜像不经 SHA 固定；精确回滚请用 Harbor 中的历史 `<sha>` 标签手动 `kubectl set image`。
- **前端为什么用容器构建**：CentOS 7 的 glibc 2.17 跑不了 Node 20（官方二进制按 glibc 2.28 构建），所以在 `node:20-bullseye-slim` 容器里执行 `npm ci && npm run build`，产物通过挂载写回工作区。若 `npm ci` 因 lockfile 平台差异报 `@rollup/rollup-linux-x64-gnu` / esbuild 平台包缺失，改用 `npm install`，或在容器内重新生成 `package-lock.json` 后提交。
- **`-v ...:z`**：CentOS 7 默认 SELinux Enforcing 时，绑定挂载需要 `:z` 重打标签，否则容器内读不到工作区文件。
- **若要改为 SHA 固定**：清单中把 tag 写成占位符 `__IMAGE_TAG__`，流水线增加 `sed "s/__IMAGE_TAG__/$IMAGE_TAG/g"` 渲染到临时目录后再 `apply`，同时把 `imagePullPolicy` 改为 `IfNotPresent`。

---

## 8. 阶段六：K8s 清单改造

对 `k8s/` 下 **15 个业务服务 + frontend** 的 yaml 统一做两处修改（`ingress.yaml` 无镜像，跳过）。

### 8.1 镜像地址指向 Harbor（已完成）

16 个业务清单（15 服务 + frontend）已**直接改好**，无需在 Master 节点上跑 sed：

```yaml
# 改前
          image: gateway:1.0
          imagePullPolicy: IfNotPresent

# 改后
          image: 192.168.10.16:9090/gmall/gateway:latest
          imagePullPolicy: Always
```

命名差异已处理：`Goods`→`goods`、`ai_customer_service`→`ai-customer-service`、`customer_ws`→`customer-ws`、`mi-frontend`→`frontend`。

自检命令（任意机器均可执行，用来确认没有漏改）：

```bash
grep -rn "image:" k8s/*.yaml | grep -v "192.168.10.16:9090"   # 期望：无输出
grep -rn "IfNotPresent" k8s/*.yaml                            # 期望：无输出（k8s/infra/ 除外）
```

### 8.2 补 `imagePullSecrets`（已完成）

16 个 Deployment 的 `spec.template.spec` 下已加上（缩进与 `containers` 同级）：

```yaml
    spec:
      imagePullSecrets:
        - name: harbor-cred
      containers:
        - name: gateway
          ...
```

> 该 Secret 需先在集群里创建（第 3 章「集群侧拉取凭据」）：`kubectl -n my-springcloud create secret docker-registry harbor-cred ...`

### 8.3 清单目录结构（已完成）

| 路径                 | 内容                                      | 参与流水线`kubectl apply` |
| -------------------- | ----------------------------------------- | --------------------------- |
| `k8s/*.yaml`       | 15 个业务服务（含 `customer-ws`）+`frontend` + `ingress` | ✅ 是                       |
| `k8s/infra/*.yaml` | mysql / nacos / redis 基础组件清单        | ❌ 否，仅手动重建时使用     |

基础组件**不纳入 CI/CD**，三条理由：它们没有源码模块与 Dockerfile（用的是第三方镜像）；它们是有状态服务、数据在 NFS 上；每次构建都 apply 一遍没有收益，只有连带重启的风险。

需要重建基础组件时手动执行：

```bash
kubectl apply -f k8s/infra/mysql.yaml
kubectl apply -f k8s/infra/nacos.yaml
kubectl apply -f k8s/infra/redis.yaml
```

`k8s/infra/` 是子目录，而 `kubectl apply -f k8s/` **默认不递归子目录**，所以基础组件不会被流水线扫到。若想彻底不依赖该行为，可把流水线命令写成显式的 `kubectl apply -f k8s/*.yaml`。

---

## 9. 验收清单

按下表顺序逐项验证，任一项失败先解决再往下走。

| #  | 验证项             | 命令 / 操作                                                         | 期望结果                                     |
| -- | ------------------ | ------------------------------------------------------------------- | -------------------------------------------- |
| 1  | Harbor 可用        | `curl -I http://192.168.10.16:9090`                               | `HTTP/1.1 200`                             |
| 2  | 本地能推镜像       | `docker login 192.168.10.16:9090 -u 'robot$gmall+ci'`             | `Login Succeeded`                          |
| 3  | Jenkins 起来了     | `systemctl status jenkins` + 浏览器 `http://192.168.10.16:8081` | 服务 active，页面能登录                      |
| 4  | Jenkins 能操作集群 | 见 4.6 自检命令                                                     | 能列出 nodes 与 deploy，`docker ps` 正常   |
| 5  | 拉代码成功         | 任务页**立即构建**，看 *准备* 阶段日志                      | 无`Permission denied (publickey)`          |
| 6  | 流水线全绿         | 同上，观察 6 个 stage                                               | 全部成功（`Finished: SUCCESS`）            |
| 7  | 轮询生效           | push 一次代码，等 5 分钟内                                          | 自动出现新的构建记录                         |
| 8  | 镜像已入库         | Harbor 控制台 →`gmall` 项目                                      | 16 个仓库（15 服务 + frontend），tag 含本次`<sha>` 与 `latest` |
| 9  | 业务可用           | 浏览器`http://192.168.10.16:30088`                                | 前端页面正常加载，接口有数据                 |
| 10 | 网关直连           | `curl http://192.168.10.16:30080/goods/list`                      | 返回 JSON 商品列表                           |

---

### 9.1 回滚

**整批回滚（推荐）**：把某个 Deployment 退回上一个版本。

```bash
kubectl -n my-springcloud rollout history deployment/gateway
kubectl -n my-springcloud rollout undo    deployment/gateway
kubectl -n my-springcloud rollout status  deployment/gateway
```

**按镜像标签回滚**：定位到具体某次构建，适合跨多个版本回退。

```bash
# 1. 在 Harbor 里找到目标 tag（即 git sha 前 7 位），例如 a1b2c3d
# 2. 直接改镜像
kubectl -n my-springcloud set image deployment/gateway \
  gateway=192.168.10.16:9090/gmall/gateway:a1b2c3d

# 3. 确认
kubectl -n my-springcloud get deploy gateway \
  -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'
```

**Jenkins 侧回滚**：任务页 `Replay` 或 `Build with Parameters`（若后续加了 tag 参数），一键重跑历史版本。

> 注意：`rollout undo` 依赖 `revisionHistoryLimit`（默认 10）。也就是最多能回退 10 个版本，超过的旧 ReplicaSet 会被清理。

---

### 9.2 排障速查

按"从外到内"的顺序定位，先看现象归属哪一层。

| 现象                                                       | 大概率原因                            | 排查动作                                                                                                           |
| ---------------------------------------------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Jenkins 页面打不开（连接超时）                             | 服务没起 / 防火墙未放行               | `systemctl status jenkins`、`firewall-cmd --list-ports`；日志见 `journalctl -u jenkins -n 100 --no-pager`    |
| `active` 但 8081 无监听（拒绝连接）                      | Jenkins 启动即崩，systemd 在崩溃循环  | `systemctl show jenkins -p NRestarts`；`journalctl -u jenkins --no-pager \| grep -i "caused by"`                |
| 报`Fontconfig head is null` → `Failed to start Jetty` | CentOS 7 最小化安装缺 fontconfig/字体 | `yum install -y fontconfig dejavu-sans-fonts dejavu-serif-fonts && systemctl restart jenkins`（见 4.4 节步骤 3） |
| 构建一开始就失败                                           | 凭据失效 / SSH 不通                   | 控制台日志*准备* 阶段；`ssh -T git@github.com`（在 jenkins 用户下执行）                                        |
| `mvn` 报 `JAVA_HOME not found`                         | systemd 单元没设环境变量              | `systemctl show jenkins -p Environment`，对照 4.4 节                                                             |
| Maven 下载依赖卡死                                         | 走官方源，外网慢                      | 确认`~/.m2/settings.xml` 已按 4.7 节换成阿里云镜像                                                               |
| 前端 stage 报`npm ci` 失败                               | `package-lock.json` 缺失            | 降级为`npm install`，或在开发机补 lock 文件后提交                                                                |
| `docker login` 报 x509                                   | 未把 Harbor 加进 insecure 列表        | 检查`/etc/docker/daemon.json` 后 `systemctl restart docker`                                                    |
| `docker push` 报 401/403                                 | 机器人账号无 push 权限                | Harbor → 项目`gmall` → 成员，确认 `robot$gmall+ci` 是 **开发者** 及以上                                |
| Pod 一直`ImagePullBackOff`                               | 缺`imagePullSecrets`                | `kubectl -n my-springcloud describe pod <pod>`，看 Events；对照 8.2 节                                           |
| Pod`CrashLoopBackOff`                                    | 应用自身起不来，与 CI 无关            | `kubectl -n my-springcloud logs <pod> --previous`                                                                |
| `rollout status` 超时                                    | 探针/依赖没就绪                       | `kubectl -n my-springcloud get pod -w`，再看 `describe` 的探针失败原因                                         |
| 代码推了但没触发构建                                       | 轮询间隔没到 / 分支不匹配             | 等 5 分钟；或任务配置里`Branch Specifier` 是否写的是 `*/main`                                                  |

**看实时日志**：

```bash
journalctl -u jenkins -f            # Jenkins 本体
# 单次构建的完整日志：任务页 → 构建号 → Console Output
```

**进容器兜底**：

```bash
kubectl -n my-springcloud exec -it deploy/gateway -- sh
```

---

## 10. 后续演进

按性价比排序，前两项建议尽早做。

1. **~~补 `customer_ws` 的流水线~~（已完成）**：`customer_ws/Dockerfile`、`k8s/customer-ws.yaml`、`nacos_config-k8s/customer-ws-server-dev.yml`、k8s 版网关路由均已补齐，模块已进流水线矩阵。
2. **Maven 本地仓库持久化 + 并行构建**：把 `~/.m2` 挂到固定目录避免每次全量下载；多模块可加 `-T 1C` 并行编译，缩短反馈时间。
3. **GitOps 化（ArgoCD）**：把部署阶段从 Jenkins 中拆出，Jenkins 只负责构建推镜像并更新清单仓库，ArgoCD 监听清单仓库做实际部署。好处是部署状态可审计、可一键回滚到任意 commit。
4. **配置即代码**：`Jenkinsfile` 已在仓库里，可再进一步——把 Jenkins 的 Job 也用 JCasC（Jenkins Configuration as Code）和 Job DSL 描述，机器重建后一条命令恢复。
5. **镜像瘦身**：运行镜像从 `eclipse-temurin:17-jdk-alpine` 换成 `17-jre-alpine`，单镜像可省 100MB+；配合多阶段构建效果更明显。
6. **流水线分阶段加速**：把"构建镜像"拆成并行分支（`parallel`），16 个服务串行构建的耗时能压到 1/3 左右。
