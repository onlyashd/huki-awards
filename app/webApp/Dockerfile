# Stage 1: Build the WasmJS frontend
FROM gradle:8.5-jdk21 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN chmod +x ./gradlew

# Build the distribution
RUN ./gradlew :app:webApp:wasmJsBrowserDistribution --no-daemon

# Stage 2: Serve the static files with Nginx
FROM nginx:stable-alpine

# Copy the build output to Nginx's default public directory
# Note: The path depends on your project structure and Kotlin version
COPY --from=build /home/gradle/src/app/webApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

# Add a basic Nginx configuration to support SPA routing
RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html; \
        try_files $uri $uri/ /index.html; \
    } \
    location = /health { \
        access_log off; \
        add_header Content-Type application/json; \
        return 200 '"'"'{"status":"ok"}'"'"'; \
    } \
    # Ensure wasm files are served with the correct MIME type \
    location ~* \.wasm$ { \
        root /usr/share/nginx/html; \
        types { application/wasm wasm; } \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
