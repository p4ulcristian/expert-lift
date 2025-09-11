#!/bin/bash

# Exit on any error
set -e

# Function to handle errors
handle_error() {
    echo "❌ Error: $1 failed with exit code $2"
    exit $2
}

echo "Running shadow-cljs release site.prod flex.prod customizer.prod labs.prod..."
npx shadow-cljs release site.prod flex.prod customizer.prod labs.prod || handle_error "shadow-cljs compilation" $?

echo "Running webpack production build..."
npx webpack --config-name production --mode production || handle_error "webpack build" $?

echo "Generating MD5 checksums..."
./start-md5sum.sh \
   project/resources/public/js/libs/flex.js \
   project/resources/public/js/libs/site.js \
   project/resources/public/js/libs/customizer.js \
   project/resources/public/js/libs/labs.js \
   project/resources/public/js/core/flex.js \
   project/resources/public/js/core/site.js \
   project/resources/public/js/core/customizer.js \
   project/resources/public/js/core/labs.js || handle_error "MD5 checksum generation" $?

echo "Building JAR file..."
clj -X:prod :jar expert-lift.jar :main-class app.backend.main || handle_error "JAR build" $?

echo "✅ Production build complete. JAR file: expert-lift.jar"
