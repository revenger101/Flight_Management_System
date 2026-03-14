FROM node:22-alpine

WORKDIR /app

# Install dependencies first for better layer caching
COPY package*.json ./
RUN npm ci

# Copy application source
COPY . .

# Vite dev server port
EXPOSE 5173

# Keep container focused on frontend development (no production build step)
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
