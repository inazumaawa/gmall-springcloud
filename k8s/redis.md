# redis-svc.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis-svc
  namespace: my-springcloud
spec:
  type: NodePort
  ports:
  - name: redisport
    port: 6379
    targetPort: 6379
    nodePort: 30850
  selector:
    app: redis



# redis-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: redis
  name: redis
  namespace: my-springcloud
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - image: registry.cn-hangzhou.aliyuncs.com/ptuxzx202180/redis:6.0.8
        imagePullPolicy: IfNotPresent
        name: redis
        ports:
        - containerPort: 6379