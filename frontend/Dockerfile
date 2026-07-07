FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
ARG VITE_API_URL
RUN npm run build

FROM node:22-alpine
RUN npm install -g serve
WORKDIR /app
COPY --from=builder /app/dist .
EXPOSE 80
CMD ["serve", "-s", ".", "-l", "80"]
