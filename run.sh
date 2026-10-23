#!/bin/sh
# Build the jar if needed and run the CLI: ./run.sh render samples/order-1001.csv
set -e
cd "$(dirname "$0")"
if [ ! -f target/invoicing.jar ]; then
  mvn -B -q -DskipTests package
fi
exec java -Djava.locale.providers=COMPAT -jar target/invoicing.jar "$@"
