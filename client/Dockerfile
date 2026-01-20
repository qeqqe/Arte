FROM node:24-alpine AS builder
WORKDIR /app
RUN npm install -g pnpm
COPY package*.json ./
RUN pnpm install
COPY . .
RUN pnpm run build
FROM node:24-alpine AS production
WORKDIR /app
RUN npm install -g pnpm
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
RUN pnpm install --only=production
EXPOSE 3000
CMD ["pnpm", "start"]