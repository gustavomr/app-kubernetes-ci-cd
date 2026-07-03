# App Kubernetes CI/CD

Projeto full stack React + Spring Boot + PostgreSQL com CI/CD via GitHub Actions e deploy em cluster Kubernetes na Oracle Cloud.

## Estrutura

```
├── backend/          Spring Boot (Java 21, Flyway, JPA)
├── frontend/         React + Vite (JavaScript)
├── k8s/
│   ├── database/     PostgreSQL StatefulSet + PVC
│   ├── letsencript/  ClusterIssuer (Let's Encrypt)
│   ├── dev/          Namespace dev
│   ├── uat/          Namespace uat
│   └── prod/         Namespace prod
├── .github/workflows/  CI/CD pipeline
└── docker-compose.yml  Ambiente local
```

## Domínios (sslip.io — gratuito, sem cadastro)

| Ambiente | URL |
|----------|-----|
| Dev | `https://dev.app.141-148-93-60.sslip.io` |
| UAT | `https://uat.app.141-148-93-60.sslip.io` |
| Prod | `https://app.141-148-93-60.sslip.io` |

O sslip.io resolve automaticamente para o IP `141.148.93.60`. O cert-manager gera certificados HTTPS via Let's Encrypt.

## Setup do Cluster (Oracle Cloud + K3s)

### 1. Criar instância na Oracle Cloud

- OS: Ubuntu 22.04 ou 24.04
- Shape: VM.Standard.E2.1.Micro (free tier) ou superior
- Anote o IP público (ex: `141.148.93.60`)

### 2. Liberar portas no Security List / NSG

No Console Oracle > **Networking > Virtual Cloud Networks** > sua VCN > **Security Lists** (ou **Network Security Groups** se a instância tiver NSG associado ao VNIC).

Adicione **Ingress Rules** para:

| Porta | Protocolo | Source CIDR | Descrição |
|-------|-----------|-------------|-----------|
| 80 | TCP | `0.0.0.0/0` | HTTP (Ingress) |
| 443 | TCP | `0.0.0.0/0` | HTTPS (Ingress + cert-manager) |
| 6443 | TCP | `0.0.0.0/0` | Kubernetes API (kubectl) |

> Se a instância tiver um **Network Security Group (NSG)** associado ao VNIC,
> as regras DEVEM ser adicionadas no NSG, não na Security List da subnet.

### 3. Instalar K3s

```bash
# SSH na VPS
ssh ubuntu@141.148.93.60

# Instalar K3s (1 comando, ~30s)
curl -sfL https://get.k3s.io | sh -s - \
  --write-kubeconfig-mode 644
```

### 4. Liberar porta 6443 no iptables

A Oracle aplica uma regra REJECT genérica no iptables que bloqueia portas não explicitamente aceitas (apenas SSH é liberado por padrão).

```bash
# Verificar regras atuais
sudo iptables -L INPUT -n -v --line-numbers

# Adicionar ACCEPT para 6443 antes da REJECT (regra #11)
sudo iptables -I INPUT 11 -p tcp --dport 6443 -j ACCEPT \
  -m comment --comment "Allow Kubernetes API"

# Tornar permanente (não perder no reboot)
sudo apt install -y iptables-persistent
sudo netfilter-persistent save
```

### 5. Adicionar IP externo no TLS do K3s

O certificado TLS do K3s é gerado apenas para IPs internos (`10.0.0.x`, `127.0.0.1`).
Para acessar externamente é necessário adicionar o IP público:

```bash
echo 'tls-san:
  - "141.148.93.60"' | sudo tee /etc/rancher/k3s/config.yaml
sudo systemctl restart k3s

# Aguardar ~30s e verificar
sudo ss -tlnp | grep 6443
```

### 6. Configurar kubectl no seu PC

```powershell
# Copiar kubeconfig da VPS
ssh ubuntu@141.148.93.60 "cat /etc/rancher/k3s/k3s.yaml" `
  > "$env:USERPROFILE\.kube\config"

# Trocar IP interno (127.0.0.1) pelo IP público da VPS
(Get-Content "$env:USERPROFILE\.kube\config") `
  -replace "127.0.0.1", "141.148.93.60" `
  | Set-Content "$env:USERPROFILE\.kube\config"

# Testar conexão
kubectl get nodes
```

### 7. Ingress Controller (Traefik)

O K3s já vem com o **Traefik** instalado como ingress controller padrão.
Nenhuma instalação adicional é necessária — os recursos `Ingress` com `ingressClassName: traefik` são reconhecidos automaticamente.

Para verificar se está rodando:

```bash
kubectl get pods -n kube-system -l app.kubernetes.io/name=traefik
```

### 8. Instalar cert-manager

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml

kubectl wait --namespace cert-manager \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

### 9. Criar ClusterIssuer (Let's Encrypt)

Salve como `letsencrypt-issuer.yaml`:

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: SEU_EMAIL@exemplo.com
    privateKeySecretRef:
      name: letsencrypt-prod-key
    solvers:
      - http01:
          ingress:
            class: traefik
```

Aplique:

```bash
kubectl apply -f letsencrypt-issuer.yaml
```

> Substitua `SEU_EMAIL@exemplo.com` pelo seu e-mail real.

### 10. Gerar KUBE_CONFIG para GitHub Actions

O segredo `KUBE_CONFIG` deve conter o **conteúdo cru** do kubeconfig (YAML), **não** em base64.

```powershell
# Exibir o conteúdo do kubeconfig
Get-Content "$env:USERPROFILE\.kube\config"
```

Copie a saída inteira e adicione como secret **`KUBE_CONFIG`** no GitHub.
> **⚠️ Importante**: Não use base64. O workflow usa `azure/setup-kubectl` e escreve o YAML diretamente em `~/.kube/config`. Basta colar o conteúdo como está no valor do secret.

## Como usar (após setup do cluster)

### 0. Ordem de aplicação dos manifests

```bash
kubectl apply -f k8s/database/
kubectl apply -f k8s/letsencript/
kubectl apply -f k8s/dev/
kubectl apply -f k8s/uat/
kubectl apply -f k8s/prod/
```

### 1. Segredos no Kubernetes

O PostgresSecret está replicado para cada namespace de ambiente:

```bash
kubectl apply -f k8s/database/postgres-secret.yaml   # namespace: database
kubectl apply -f k8s/dev/postgres-secret.yaml
kubectl apply -f k8s/uat/postgres-secret.yaml
kubectl apply -f k8s/prod/postgres-secret.yaml
```

> Altere a senha nos YAMLs antes de aplicar se quiser uma senha diferente.
> Os valores estão em base64, gere com: `[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("sua_senha"))`

### 2. Segredos no GitHub

No repositório > Settings > Secrets and variables > Actions, crie:

| Secret | Descrição | Como gerar |
|--------|-----------|------------|
| `DOCKER_USERNAME` | Usuário do Docker Hub | Seu usuário |
| `DOCKER_PASSWORD` | Token de acesso do Docker Hub | [Account Settings > Security > New Access Token](https://hub.docker.com/settings/security) |
| `KUBE_CONFIG` | kubeconfig (YAML cru, **não** base64) | `Get-Content "$env:USERPROFILE\.kube\config"` e colar o conteúdo |

### 3. Environments no GitHub (opcional)

Settings > Environments > Create:
- `uat` — sem approval
- `prod` — com **Required reviewers** se quiser aprovação manual

### 4. Push na main

O CI/CD fará todo o deploy automático.

## CI/CD — Como funciona

### PR aberto

```yaml
# Job: validate
# Build do backend + frontend (sem deploy)
# Se falhar, o merge é bloqueado
```

### Push na branch `main`

1. Build do backend (Maven) e frontend (npm)
2. Docker build (multi-arch `linux/arm64`) + push para Docker Hub com tags `sha` e `latest`
3. `kubectl apply -f k8s/dev/namespace.yaml` + `kubectl apply -f k8s/dev/`
4. Deploy automático em dev com a tag `sha`
5. Aplicação disponível em `https://dev.app.141-148-93-60.sslip.io`

### Deploy manual (UAT ou PROD)

Pelo navegador:
1. GitHub > **Actions** > workflow **CI/CD** > **Run workflow**
2. Escolher `uat` ou `prod`
3. Informar o **SHA da imagem** (mesmo que rodou em dev)
4. Clicar em **Run workflow**
5. Aplicação disponível em `https://uat.app.141-148-93-60.sslip.io` ou `https://app.141-148-93-60.sslip.io`

Pelo CLI (requer [GitHub CLI](https://cli.github.com/)):

```bash
# Pegar o SHA da última run em dev
gh run list --workflow ci-cd.yml --branch main --json headSha -q '.[0].headSha'

# Deploy no UAT
gh workflow run ci-cd.yml -f environment=uat -f image_tag=2bd5ada5969f3daabf4a0d9080915c78d873adf4

# Deploy no PROD
gh workflow run ci-cd.yml -f environment=prod -f image_tag=2bd5ada5969f3daabf4a0d9080915c78d873adf4
```

## Rodar localmente (desenvolvimento)

```bash
docker compose up --build
```

- Frontend: `http://localhost`
- Backend:  `http://localhost:8080/api/produtos`

Para inserir dados de teste:

```bash
curl -X POST http://localhost:8080/api/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome": "Teclado Mecânico", "preco": 299.90}'
```

## Troubleshooting

### cert-manager: Certificate não emite

O sslip.io às vezes demora para resolver com Let's Encrypt. Verifique:

```bash
kubectl get certificate -n dev
kubectl describe certificate dev-app-tls -n dev
```

Se falhar, mude o issuer para `letsencrypt-staging` para testar.

### PostgreSQL não sobe

```bash
kubectl logs -n database statefulset/postgres
kubectl get pvc -n database  # verificar se o PV foi provisionado
```

### Rollback

Para voltar a versão anterior no dev:

```bash
kubectl rollout undo deployment/backend -n dev
kubectl rollout undo deployment/frontend -n dev
```
