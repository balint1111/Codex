FROM node:18-alpine AS build
WORKDIR /app
COPY package.json tsconfig.json webpack.config.js ./
RUN npm install
COPY src ./src
RUN npm run build \
    && cp src/index.html dist/ \
    && cp src/env.js dist/

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
