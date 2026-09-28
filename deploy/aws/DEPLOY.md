# AWS deployment (prototype)

This documents the simplest path to a working prototype on AWS: a single EC2 instance
running the whole `docker-compose.yml` stack (MySQL, Redis, RabbitMQ, backend, frontend).
It's intentionally the "one box" version of the architecture — see
[Beyond the prototype](#beyond-the-prototype) for how each piece maps to a managed
AWS service when this needs to scale past a demo.

## 1. Provision the instance

1. Launch an EC2 instance (Ubuntu 22.04 LTS, `t3.small` or larger — MySQL + Redis +
   RabbitMQ + two JVM/Node processes want more than `t3.micro`).
2. Attach a security group allowing:
   - `22/tcp` (SSH) from your IP only
   - `80/tcp` and `443/tcp` (frontend / future TLS) from `0.0.0.0/0`
   - Leave `3306`, `6379`, `5672`, `8080` closed to the internet — they're only reached
     over the compose network between containers.
3. Allocate and associate an Elastic IP so the address survives instance restarts.

## 2. Install Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo apt-get install -y docker-compose-plugin
# log out/in for the group change to apply
```

## 3. Ship the code and configure secrets

```bash
git clone <your-repo-url> code_platform
cd code_platform
cp .env.example .env   # then edit values below
```

`.env` (read by `docker-compose.yml`):

```
MYSQL_ROOT_PASSWORD=<generate a strong password>
JWT_SECRET=<32+ random bytes, e.g. `openssl rand -base64 48`>
CORS_ALLOWED_ORIGINS=http://<your-elastic-ip-or-domain>
```

Never commit `.env` — it already matches an entry in the root `.gitignore`.

## 4. Run it

```bash
docker compose pull        # no-op the first time, useful on redeploys
docker compose build
docker compose up -d
docker compose ps          # confirm mysql/redis/rabbitmq report "healthy"
```

The frontend (Nginx) listens on port 80 and reverse-proxies `/api/*` to the backend
container, so the browser only ever talks to one origin — no CORS/mixed-content
headaches for the prototype.

## 5. Point DNS / TLS at it (optional)

- Point an A record at the Elastic IP.
- Terminate TLS with a managed cert: put the frontend container behind an
  Application Load Balancer with an ACM certificate, or run Certbot on the box and
  add a TLS server block to `frontend/nginx.conf`.

## 6. Redeploys

```bash
git pull
docker compose build backend frontend
docker compose up -d --no-deps backend frontend
```

Flyway migrations in `backend/src/main/resources/db/migration` run automatically on
backend startup.

## Beyond the prototype

The single-box compose stack is deliberately the fastest way to get something live.
When this needs to hold real traffic, each stateful service has a drop-in managed
replacement — swap the connection env vars in `docker-compose.yml`, nothing in the
application code changes:

| Component | Prototype (this repo) | Managed AWS equivalent |
|---|---|---|
| MySQL     | `mysql` container      | Amazon RDS for MySQL (Multi-AZ) |
| Redis     | `redis` container      | Amazon ElastiCache for Redis |
| RabbitMQ  | `rabbitmq` container   | Amazon MQ (RabbitMQ engine) |
| Backend   | EC2 container          | ECS/Fargate service behind an ALB |
| Frontend  | EC2 container (Nginx)  | S3 + CloudFront static hosting |

See `deploy/aws/cloud-architecture.md` for a short note on how those pieces connect.
