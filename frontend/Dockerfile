FROM node:18-alpine AS build
WORKDIR /app
COPY package.json tsconfig.json webpack.config.js ./
RUN npm install
COPY src ./src
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
