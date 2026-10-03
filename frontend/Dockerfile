# Stage 1: Build Stage
FROM node:22 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Same-origin API base so calls go through nginx's /api proxy instead of the
# browser hitting the backend's own host:port directly. Vite bakes this in at
# build time, so it can't be changed later via a container env var.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Build the application for production
RUN npm run build

# Stage 2: Production Stage
FROM nginx:stable-alpine

# Copy the React build from the build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration as a template — envsubst (built into the
# nginx image's entrypoint) renders it to conf.d/default.conf on container
# start, so BACKEND_HOST/BACKEND_PORT can vary per environment without a
# separate image per environment.
COPY nginx.conf /etc/nginx/templates/default.conf

# Where the frontend reaches the backend from inside the container network.
# Set per environment (see deployment/environments/<env>/). BACKEND_PORT has
# no default here on purpose — it MUST be passed at deploy time (task
# definition / compose env), or the nginx template renders an empty port and
# every /api request fails.
ENV BACKEND_HOST=backend
ENV BACKEND_PORT=8081

# Expose the port
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
