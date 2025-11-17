FROM node:24

WORKDIR /app

COPY package*.json ./
COPY proxy.docker.conf.json ./proxy.docker.conf.json

RUN npm install -g @angular/cli
RUN npm install

COPY . .

EXPOSE 4200

CMD ["ng", "serve", "--host", "0.0.0.0", "--proxy-config", "proxy.docker.conf.json"]