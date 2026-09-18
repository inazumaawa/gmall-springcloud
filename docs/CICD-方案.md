# gmall-springcloud CI/CD 方案

> 目标：`git push` → 自动构建 → 打镜像 → 推 Harbor → 部署 K8s，全流程无人值守。
> 适用范围：`inazumaawa/gmall-springcloud` 仓库，K8s 集群命名空间 `my-springcloud`，Master 节点 `192.168.10.16`。

---

## 1. 总体架构

```
┌──────────────────────────────────────────────────────────────────────┐
│ 开发机 (Windows)                                                      │
│   git push  ──SSH──►  GitHub  inazumaawa/gmall-springcloud            │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ GitHub Actions 事件触发
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ K8s Master 192.168.10.16  （同时是 Harbor 宿主机 + 自托管 Runner 宿主机）│
│                                                                       │
│  ┌────────────────────────┐      docker push      ┌────────────────┐  │
│  │ GitHub Actions Runner  │ ────────────────────► │ Harbor :8080   │  │
│  │  · mvn package 17      │                       │ project: gmall │  │
│  │  · docker build        │                       └───────┬────────┘  │
│  │  · kubectl apply       │                               │ pull      │
│  └───────────┬────────────┘                               ▼           │
│              │ kubectl apply                     ┌──────────────────┐ │
│              └─────────────────────────────────► │ K8s my-springcloud│ │
│                                                   │ 14 个微服务+前端 │ │
│                                                   │ Nacos/Redis/MySQL│ │
│                                                   └──────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

**为什么必须用自托管 Runner**：Harbor 与 K8s 都在内网 `192.168.10.x`，GitHub 托管 Runner 无法访问。Runner 直接装在 Master 节点上，构建、推镜像、部署全在内网完成，Harbor/K8s 无需暴露到公网。

---

## 2. 现状盘点

### 2.1 已具备

| 项           | 现状                                                                                                                    |
| ------------ | ----------------------------------------------------------------------------------------------------------------------- |
| 代码仓库     | `git@github.com:inazumaawa/gmall-springcloud.git`，分支 `main`，首次提交 `885b4d0`                                |
| 构建         | 根`pom.xml` 聚合 16 个模块（`common` 为依赖库，不单独出镜像）                                                       |
| Dockerfile   | 14 个 Java 服务各有独立 Dockerfile（`eclipse-temurin:17-jdk-alpine`）+ 前端 `mi.com/Dockerfile`（`nginx:alpine`） |
| K8s 清单     | `k8s/*.yaml` 共 15 个（14 服务 + frontend）+ `ingress.yaml`                                                         |
| 基础组件     | Nacos / Redis / MySQL / OBS 已部署在`my-springcloud`                                                                  |
| 内网端口约定 | gateway`30080`、frontend `30088`、nacos `30848/30948`                                                             |

### 2.2 服务与端口对照表（CI 构建矩阵的依据）

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
| frontend            | `mi.com`              | `dist/`（Vite 构建产物）               | 80       | frontend            |

> 注意：模块目录首字母大写的 `Goods`，其 jar 名也是 `Goods-1.0-SNAPSHOT.jar`；镜像名统一小写 `goods`，与 K8s 清单保持一致。

### 2.3 缺口（实施前需补齐）

| # | 缺口                                                              | 影响                                                         | 处理建议                                                                                                    |
| - | ----------------------------------------------------------------- | ------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| 1 | `customer_ws` 模块**没有 Dockerfile，也没有 k8s 清单**    | WebSocket 客服功能无法部署                                   | 参照`gateway/Dockerfile` 补一个，端口按 `bootstrap.yml` 配置补 `k8s/customer-ws.yaml`，并加入 CI 矩阵 |
| 2 | `nacos_config/`、`nacos_config-k8s/` 已被 `.gitignore` 排除 | 新集群无法从仓库还原配置                                     | 配置只存在 Nacos 服务端；重建集群时从 Nacos 控制台导出/导入，或改用 Nacos OpenAPI 脚本化                    |
| 3 | `k8s/*.md`（mysql/nacos/redis）混在清单目录                     | `kubectl apply -f k8s/` 可能报 `no recognized extension` | 移到`docs/` 或 `k8s/ops/`                                                                               |
| 4 | 老脚本`k8s/build-images.sh` 指向阿里云镜像仓库                  | 与 Harbor 方案冲突                                           | 改造为 Harbor 版本，或在文档中废弃                                                                          |
| 5 | 各服务`bootstrap.yml` 依赖环境变量注入 Nacos 地址               | 本地与集群行为不一致                                         | 集群已在 Deployment 的`env` 里注入，保持现状即可                                                          |

---

## 3. 阶段一：搭建 Harbor 私有仓库

### 3.1 前置检查

```bash
# Master 节点上确认 docker 与 compose 可用
docker version
docker compose version || docker-compose version

# 确认 80/443 已被 Nginx Ingress 占用，Harbor 要换端口
ss -lntp | grep -E ':(80|443)\s'
```

> **关键点**：Master 上 Nginx Ingress 已占用 `80/443`，Harbor 默认也用 80，必须改端口（下文用 `8080`，请按实际空闲端口调整）。

### 3.2 安装（HTTP 模式，内网够用）

```bash
# 1. 下载离线安装包（版本按需替换）
cd /opt
wget https://github.com/goharbor/harbor/releases/download/v2.11.0/harbor-offline-installer-v2.11.0.tgz
tar xzvf harbor-offline-installer-v2.11.0.tgz

# 2. 准备配置
cd /opt/harbor
cp harbor.yml.tmpl harbor.yml
```

编辑 `harbor.yml`，只保留 HTTP、改掉冲突端口：

```yaml
hostname: 192.168.10.16
http:
  port: 8080            # 必改！避开 Ingress 的 80
# https:                # 整段注释掉（内网不启用 TLS）
data_volume: /data/harbor
harbor_admin_password: <你的管理员密码>
```

```bash
# 3. 安装并启动
./install.sh

# 4. 验证
docker ps | grep harbor
curl -I http://192.168.10.16:8080
```

### 3.3 建项目与机器人账号

1. 浏览器打开 `http://192.168.10.16:8080`，用 `admin` 登录；
2. **项目 → 新建项目**：名称 `gmall`，访问级别**私有**；
3. 进入 `gmall` → **机器人账户 → 新建**：名称 `ci`，权限勾选 `推送` + `拉取`；
4. 记下生成的 **用户名**（形如 `robot$gmall+ci`）与 **Token**，只显示一次，后面配到 GitHub Secrets。

### 3.4 各节点放行 HTTP 仓库

**Runner/Master 宿主机（docker 引擎）** —— `/etc/docker/daemon.json`：

```json
{
  "insecure-registries": ["192.168.10.16:8080"]
}
```

```bash
systemctl restart docker
```

**K8s 工作节点（containerd 运行时）** —— `/etc/containerd/config.toml`：

```toml
[plugins."io.containerd.grpc.v1.cri".registry.mirrors."192.168.10.16:8080"]
  endpoint = ["http://192.168.10.16:8080"]

[plugins."io.containerd.grpc.v1.cri".registry.configs."192.168.10.16:8080".tls]
  insecure_skip_verify = true
```

```bash
systemctl restart containerd
```

**集群侧拉取凭据**（每个命名空间一次）：

```bash
kubectl -n my-springcloud create secret docker-registry harbor-cred \
  --docker-server=192.168.10.16:8080 \
  --docker-username='robot$gmall+ci' \
  --docker-password='<机器人账号 Token>'
```

---

## 4. 阶段二：注册自托管 Runner

位置：K8s **Master 节点**（`192.168.10.16`），因为这里同时有 `docker`、`kubectl` 和内网可达的 Harbor。

### 4.1 网页取注册令牌

GitHub 仓库 → **Settings → Actions → Runners → New self-hosted runner** → 选 Linux / x64，页面会给出下载与 `config.sh` 命令（含 token，有效期 1 小时）。

### 4.2 安装

```bash
# 1. 安装依赖
apt-get update && apt-get install -y curl git jq docker.io

# 2. 建独立用户（不建议用 root 跑 Runner）
useradd -m -s /bin/bash github-runner
usermod -aG docker github-runner

# 3. 下载并解压 Runner
su - github-runner
mkdir -p ~/actions-runner && cd ~/actions-runner
curl -o actions-runner.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.319.1/actions-runner-linux-x64-2.319.1.tar.gz
tar xzf actions-runner.tar.gz

# 4. 注册（URL 与 token 以网页显示为准），打上自定义标签 gmall
./config.sh --url https://github.com/inazumaawa/gmall-springcloud \
            --token <网页给的注册令牌> \
            --labels gmall \
            --name k8s-master-runner --unattended

# 5. 装成系统服务并启动
sudo ./svc.sh install github-runner
sudo ./svc.sh start
sudo ./svc.sh status
```

### 4.3 Runner 环境自检

```bash
sudo -u github-runner docker ps              # docker 可用
sudo -u github-runner kubectl get nodes      # kubeconfig 就位
sudo -u github-runner kubectl -n my-springcloud get deploy
```

> `kubectl` 权限：`/root/.kube/config` 复制到 `~github-runner/.kube/config` 并 `chown github-runner:github-runner`（或给该用户单独签发受限 kubeconfig，只允许 `my-springcloud` 命名空间写操作）。
> JDK 17 与 Maven 由 workflow 里的 `actions/setup-java` 自动准备，无需预装。

### 4.4 验证 Runner 上线

网页 **Settings → Actions → Runners** 应显示 `k8s-master-runner` 状态为 **Idle**（绿点）。

---

## 5. 阶段三：镜像命名与标签规范

| 项         | 规范                                                                                             |
| ---------- | ------------------------------------------------------------------------------------------------ |
| 仓库地址   | `192.168.10.16:8080/gmall/<镜像名>`                                                            |
| 标签       | `<git-sha>`（不可变，用于审计与精确回滚）+ `latest`（清单引用）                              |
| 基础镜像   | Java：`eclipse-temurin:17-jdk-alpine`；前端：`nginx:alpine`                                  |
| 构建上下文 | Java 服务：`<模块>/target`（jar 所在目录）；前端：`mi.com`（含 `dist/` 与 `nginx.conf`） |
| 拉取策略   | `imagePullPolicy: Always`（配合 `latest` 保证每次都拉新）                                    |

---

## 6. 阶段四：Workflow 全文

文件位置：`.github/workflows/ci-cd.yml`

```yaml
name: CI/CD - Build & Deploy to K8s

on:
  push:
    branches: [ main ]
    paths-ignore:
      - '**.md'
      - 'docs/**'
  workflow_dispatch:        # 支持手动触发

concurrency:
  group: deploy-main
  cancel-in-progress: false # 部署串行，避免并发滚动互相打断

env:
  HARBOR_REGISTRY: ${{ secrets.HARBOR_REGISTRY }}   # 192.168.10.16:8080
  HARBOR_PROJECT:  ${{ secrets.HARBOR_PROJECT }}    # gmall
  K8S_NAMESPACE:   my-springcloud
  IMAGE_TAG:       ${{ github.sha }}

jobs:
  build-and-deploy:
    runs-on: [ self-hosted, linux, x64, gmall ]
    steps:
      - name: 拉取代码
        uses: actions/checkout@v4

      # ---------- 1. 构建 ----------
      - name: 准备 JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      - name: Maven 打包（跳过测试）
        run: mvn -B -DskipTests clean package

      - name: 构建前端产物
        working-directory: mi.com
        run: |
          npm ci
          npm run build        # 输出 mi.com/dist

      # ---------- 2. 构建并推送镜像 ----------
      - name: 登录 Harbor
        run: |
          echo "${{ secrets.HARBOR_PASSWORD }}" \
            | docker login "$HARBOR_REGISTRY" -u "${{ secrets.HARBOR_USERNAME }}" --password-stdin

      - name: 构建并推送后端镜像
        run: |
          set -euo pipefail
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
          )
          for s in "${services[@]}"; do
            IFS=: read -r name module jar <<< "$s"
            IMG="$HARBOR_REGISTRY/$HARBOR_PROJECT/$name"
            echo "==> $name  (模块 $module / $jar)"
            docker build -f "$module/Dockerfile" "$module/target" \
              -t "$IMG:$IMAGE_TAG" -t "$IMG:latest"
            docker push "$IMG:$IMAGE_TAG"
            docker push "$IMG:latest"
          done

      - name: 构建并推送前端镜像
        run: |
          set -euo pipefail
          IMG="$HARBOR_REGISTRY/$HARBOR_PROJECT/frontend"
          docker build -f mi.com/Dockerfile mi.com -t "$IMG:$IMAGE_TAG" -t "$IMG:latest"
          docker push "$IMG:$IMAGE_TAG"
          docker push "$IMG:latest"

      # ---------- 3. 部署 ----------
      - name: 应用 K8s 清单
        run: kubectl apply -n "$K8S_NAMESPACE" -f k8s/

      - name: 滚动更新全部工作负载
        run: |
          set -euo pipefail
          for d in gateway auth goods carts order pay address favorites review \
                   user-center admin coupon ai-customer-service obs frontend; do
            kubectl -n "$K8S_NAMESPACE" rollout restart "deployment/$d"
          done

      - name: 等待滚动完成
        run: |
          set -euo pipefail
          for d in gateway auth goods carts order pay address favorites review \
                   user-center admin coupon ai-customer-service obs frontend; do
            kubectl -n "$K8S_NAMESPACE" rollout status "deployment/$d" --timeout=240s
          done

      - name: 输出部署快照
        if: always()
        run: |
          kubectl -n "$K8S_NAMESPACE" get deploy -o wide
          kubectl -n "$K8S_NAMESPACE" get pods -o wide
```

### 6.1 两点设计说明

- **镜像引用方式**：清单里写 `:latest` + `imagePullPolicy: Always`，CI 用 `apply` + `rollout restart` 触发重拉。
  优点是无占位符、可手动 `kubectl apply -f k8s/`、每次部署只滚动一次。
  代价是集群里运行的镜像不经 SHA 固定；精确回滚请用 Harbor 中的历史 `<sha>` 标签手动 `kubectl set image`。
- **若要改为 SHA 固定**：清单中把 tag 写成占位符 `__IMAGE_TAG__`，CI 增加 `sed "s/__IMAGE_TAG__/$IMAGE_TAG/g"` 渲染到临时目录后再 `apply`，同时把 `imagePullPolicy` 改为 `IfNotPresent`。

---

## 7. 阶段五：K8s 清单改造

对 `k8s/` 下 **15 个 yaml** 统一做三处修改。

### 7.1 镜像地址指向 Harbor

```yaml
# 改前
          image: gateway:1.0
          imagePullPolicy: IfNotPresent

# 改后
          image: 192.168.10.16:8080/gmall/gateway:latest
          imagePullPolicy: Always
```

批量替换（在仓库根目录执行，注意 `Goods`→`goods`、`ai_customer_service`→`ai-customer-service` 命名差异）：

```bash
sed -i 's#image: gateway:1.0#image: 192.168.10.16:8080/gmall/gateway:latest#'   k8s/gateway.yaml
sed -i 's#image: auth:1.0#image: 192.168.10.16:8080/gmall/auth:latest#'         k8s/auth.yaml
sed -i 's#image: goods:1.0#image: 192.168.10.16:8080/gmall/goods:latest#'       k8s/goods.yaml
sed -i 's#image: carts:1.0#image: 192.168.10.16:8080/gmall/carts:latest#'       k8s/carts.yaml
sed -i 's#image: order:1.0#image: 192.168.10.16:8080/gmall/order:latest#'       k8s/order.yaml
sed -i 's#image: pay:1.0#image: 192.168.10.16:8080/gmall/pay:latest#'           k8s/pay.yaml
sed -i 's#image: address:1.0#image: 192.168.10.16:8080/gmall/address:latest#'   k8s/address.yaml
sed -i 's#image: favorites:1.0#image: 192.168.10.16:8080/gmall/favorites:latest#' k8s/favorites.yaml
sed -i 's#image: review:1.0#image: 192.168.10.16:8080/gmall/review:latest#'     k8s/review.yaml
sed -i 's#image: user-center:1.0#image: 192.168.10.16:8080/gmall/user-center:latest#' k8s/user-center.yaml
sed -i 's#image: admin:1.0#image: 192.168.10.16:8080/gmall/admin:latest#'       k8s/admin.yaml
sed -i 's#image: coupon:1.0#image: 192.168.10.16:8080/gmall/coupon:latest#'     k8s/coupon.yaml
sed -i 's#image: ai-customer-service:1.0#image: 192.168.10.16:8080/gmall/ai-customer-service:latest#' k8s/ai-customer-service.yaml
sed -i 's#image: obs:1.0#image: 192.168.10.16:8080/gmall/obs:latest#'           k8s/obs.yaml
sed -i 's#image: mi-frontend:1.0#image: 192.168.10.16:8080/gmall/frontend:latest#' k8s/frontend.yaml

# 统一拉取策略
sed -i 's#imagePullPolicy: IfNotPresent#imagePullPolicy: Always#' k8s/*.yaml
```

### 7.2 补 `imagePullSecrets`

每个 Deployment 的 `spec.template.spec` 下加两行（缩进与 `containers` 同级）：

```yaml
    spec:
      imagePullSecrets:
        - name: harbor-cred
      containers:
        - name: gateway
          ...
```

### 7.3 清理清单目录

```bash
mkdir -p docs
git mv k8s/mysql.md k8s/nacos.md k8s/redis.md docs/
```

否则 `kubectl apply -f k8s/` 会因非 YAML 文件报错。

---

## 8. 阶段六：Secrets 与变量清单

GitHub 仓库 → **Settings → Secrets and variables → Actions → New repository secret**：

| Secret              | 值                     | 说明                  |
| ------------------- | ---------------------- | --------------------- |
| `HARBOR_REGISTRY` | `192.168.10.16:8080` | Harbor 地址（含端口） |
| `HARBOR_PROJECT`  | `gmall`              | Harbor 项目名         |
| `HARBOR_USERNAME` | `robot$gmall+ci`     | Harbor 机器人账号     |
| `HARBOR_PASSWORD` | `<机器人 Token>`     | 只显示一次，妥善保存  |

无需 `KUBE_CONFIG`：Runner 跑在 Master 节点，直接用本机 kubeconfig。

---

## 9. 验收清单

按下表顺序逐项验证，任一项失败先解决再往下走。

| #  | 验证项            | 命令 / 操作                                                 | 期望结果                                  |
| -- | ----------------- | ----------------------------------------------------------- | ----------------------------------------- |
| 1  | Harbor 可用       | `curl -I http://192.168.10.16:8080`                       | `HTTP/1.1 200`                          |
| 2  | 本地能推镜像      | `docker login 192.168.10.16:8080 -u 'robot$gmall+ci'`     | `Login Succeeded`                       |
| 3  | Runner 在线       | 网页 Settings → Actions → Runners                         | `k8s-master-runner` 为 Idle             |
| 4  | Runner 能操作集群 | 见 4.3 自检命令                                             | 能列出 nodes 与 deploy                    |
| 5  | 流水线全绿        | push 一次代码，观察 Actions 页面                            | build / push / deploy 三步全绿            |
| 6  | 镜像已入库        | Harbor 控制台 →`gmall` 项目                              | 15 个仓库，含`<sha>` 与 `latest` 标签 |
| 7  | Pod 已更新        | `kubectl -n my-springcloud get pods`                      | 全部`Running`，`RESTARTS` 不增长      |
| 8  | 滚动成功          | `kubectl -n my-springcloud rollout status deploy/gateway` | `successfully rolled out`               |
| 9  | 业务可用          | 浏览器`http://192.168.10.16:30088`                        | 商城首页正常，接口有响应                  |
| 10 | 网关直连          | `curl http://192.168.10.16:30080/goods/list`              | 返回业务 JSON                             |

### 9.1 回滚

```bash
# 方式一：回滚到上一个 ReplicaSet（最快）
kubectl -n my-springcloud rollout undo deployment/gateway

# 方式二：指定回滚到某个历史版本
kubectl -n my-springcloud rollout history deployment/gateway
kubectl -n my-springcloud rollout undo deployment/gateway --to-revision=3

# 方式三：按 SHA 精确回退镜像
kubectl -n my-springcloud set image deployment/gateway gateway=192.168.10.16:8080/gmall/gateway:<旧sha>
```

### 9.2 排障速查

| 现象                       | 定位命令                                           | 常见原因                                                             |
| -------------------------- | -------------------------------------------------- | -------------------------------------------------------------------- |
| 镜像拉取失败               | `kubectl -n my-springcloud describe pod <pod>`   | 节点未配 insecure-registries / 缺`harbor-cred` / Harbor 项目非公开 |
| Runner 不接任务            | `journalctl -u actions.runner.* -f`              | 标签不匹配 / Runner 离线 / 服务未启动                                |
| 构建卡在`mvn`            | 查看 Actions 日志                                  | Runner 无外网、Maven 中央仓拉不动 → 配`settings.xml` 走阿里云镜像 |
| `kubectl apply` 报扩展名 | —                                                 | `k8s/*.md` 未移走                                                  |
| 前端 404                   | `kubectl -n my-springcloud logs deploy/frontend` | 构建产物路径错误 /`nginx.conf` 未随镜像打进                        |

---

## 10. 后续演进（可选）

1. **补 `customer_ws`**：加 Dockerfile + `k8s/customer-ws.yaml`，纳入 CI 矩阵与滚动列表。
2. **Maven 国内镜像**：Runner 访问中央仓库慢，在 Runner 用户目录放 `~/.m2/settings.xml` 指向阿里云。
3. **GitOps（ArgoCD）**：镜像 SHA 回写到清单仓库 → ArgoCD 自动同步，替代 workflow 里的 `kubectl` 步骤，天然免疫 `apply`/`set image` 的镜像回退冲突。
4. **配置即代码**：把 `nacos_config/` 里的密钥替换为 `${MYSQL_PASSWORD}` 等占位符后重新入库，真实值通过 K8s Secret 或 CI 变量注入。
5. **镜像瘦身**：Java 基础镜像换 `eclipse-temurin:17-jre-alpine`（JDK→JRE），单镜像可省约 150MB。
