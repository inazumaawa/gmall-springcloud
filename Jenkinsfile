// ============================================================================
// gmall-springcloud CI/CD 流水线
//
// 触发：Poll SCM，每 5 分钟检查一次 main 分支
// 流程：Maven 打包 → 构建前端产物 → 登录 Harbor → 构建并推送镜像 → kubectl 部署
//
// 前置依赖（均在 Jenkins 宿主机 192.168.10.16 上）：
//   1. JDK 21  → /opt/jdk/current        （见方案 4.2）
//   2. Maven   → /opt/maven              （见方案 4.3）
//   3. docker 可执行且已配置 insecure-registries 指向 Harbor:9090
//   4. kubectl 可用，凭据位于 /var/lib/jenkins/.kube/config（见方案 4.6）
//   5. Jenkins 凭据 ID：harbor-cred（Harbor 机器人账号，需 push 权限）
// ============================================================================

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
                    env.IMAGE_TAG = env.GIT_COMMIT.take(7)         // 用本次提交的短 SHA 作为镜像标签
                }
                echo "构建号 ${env.BUILD_NUMBER} / 代码版本 ${env.IMAGE_TAG}"
                sh 'java -version; mvn -v'
            }
        }

        // ---------- 1. 构建 ----------
        stage('Maven 打包') {
            steps {
                sh 'mvn -B -DskipTests clean package'              // 产物落在各模块 target/ 下
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
                // 每行格式：镜像名:模块目录:jar 文件名
                // 构建上下文取 target/，与 Dockerfile 中 COPY <jar> app.jar 的写法对应
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
                    # k8s/ 下是业务清单；k8s/infra/ 是mysql/nacos/redis，kubectl 默认不递归子目录，不会被 apply
                    kubectl apply -n "$K8S_NAMESPACE" -f k8s/
                    # 清单里写的是 :latest + imagePullPolicy: Always，需重启才会重新拉镜像
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
