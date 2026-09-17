# ============================================================
# 服务端镜像构建脚本（在 /root/springcloud/jar/ 下执行）
# 用法: bash build-images.sh
# 前提: 已将对应 Dockerfile 复制到 jar 文件同目录
# ============================================================
REGISTRY="registry.cn-hangzhou.aliyuncs.com/ptuxzx202180"
VERSION="1.0"

# 微服务列表：镜像名=jar名
# jar 文件和 Dockerfile 已在同一目录
services=(
    "gateway:gateway-1.0-SNAPSHOT.jar"
    "auth:auth-1.0-SNAPSHOT.jar"
    "goods:Goods-1.0-SNAPSHOT.jar"
    "carts:carts-1.0-SNAPSHOT.jar"
    "order:order-1.0-SNAPSHOT.jar"
    "pay:pay-1.0-SNAPSHOT.jar"
    "address:address-1.0-SNAPSHOT.jar"
    "favorites:favorites-1.0-SNAPSHOT.jar"
    "review:review-1.0-SNAPSHOT.jar"
    "user-center:user-center-1.0-SNAPSHOT.jar"
    "admin:admin-1.0-SNAPSHOT.jar"
    "coupon:coupon-1.0-SNAPSHOT.jar"
    "ai-customer-service:ai_customer_service-1.0-SNAPSHOT.jar"
    "obs:obs-1.0-SNAPSHOT.jar"
)

for svc in "${services[@]}"; do
    IMAGE=$(echo "$svc" | cut -d: -f1)
    JAR=$(echo "$svc" | cut -d: -f2)

    echo "============================================"
    echo "Building: $IMAGE -> $REGISTRY/$IMAGE:$VERSION"

    # 创建临时构建目录，复制 jar 和对应 Dockerfile
    mkdir -p /tmp/docker-build-$IMAGE
    cp "$JAR" "/tmp/docker-build-$IMAGE/"
    cp "Dockerfile-$IMAGE" "/tmp/docker-build-$IMAGE/Dockerfile" 2>/dev/null || true

    docker build -t "$REGISTRY/$IMAGE:$VERSION" /tmp/docker-build-$IMAGE/
    if [ $? -ne 0 ]; then
        echo "ERROR: Build failed for $IMAGE"
        rm -rf /tmp/docker-build-$IMAGE
        exit 1
    fi

    rm -rf /tmp/docker-build-$IMAGE
    echo "OK: $IMAGE built successfully"
done

echo "============================================"
echo "All images built! To push:"
for svc in "${services[@]}"; do
    IMAGE=$(echo "$svc" | cut -d: -f1)
    echo "  docker push $REGISTRY/$IMAGE:$VERSION"
done
