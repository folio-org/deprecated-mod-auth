FROM mhart/alpine-node:latest
#FROM node:latest
#Should I just use latest?

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package.json /usr/src/app/
COPY . /usr/src/app/
RUN npm install
EXPOSE 8081
#ENTRYPOINT ["sh" "-c"]
CMD ["node","src/Server.js","-p","8081"]


