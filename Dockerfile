#################################
#  Npm preload dependencies     #
#################################

FROM node:lts-bullseye AS node-deps
WORKDIR /root
COPY package.json .
COPY package-lock.json .
RUN         npm install shadow-cljs -g
RUN         npm install webpack -g
RUN         npm install webpack-cli -g
RUN         npm install



#################################
#  Clojure preload dependecies  #
#################################

FROM clojure:tools-deps AS clojure-deps
WORKDIR /root
COPY deps.edn .
RUN clj -A:prod -Stree



#########################
#  Clojure compilation  #
#########################

FROM        clojure:temurin-21-tools-deps-jammy AS compiler

# No environment variables needed at build time - all are lazy loaded at runtime!

# Copy Node.js binary and npm from the Node.js image
COPY --from=node-deps /usr/local/bin/     /usr/local/bin/
COPY --from=node-deps /usr/local/lib      /usr/local/lib/

# copy resources
COPY      --from=clojure-deps /root/.m2          /root/.m2
COPY      --from=node-deps    /root/node_modules /root/node_modules
COPY      . /root/

# installing node, shadow-cljs and webpack for clojurescript compilation

# compile project
WORKDIR      /root/
# Now we can run build with env vars available
RUN         ./start-prod.sh



#########################
#  Running project      #
#########################

FROM      openjdk:23-slim
COPY      --from=compiler /root                  /root/
WORKDIR   /root/
# JAR is already built, just run it
CMD       ["java", "-jar", "expert-lift.jar"]