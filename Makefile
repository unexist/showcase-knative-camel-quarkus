define JSON_TODO
curl -X 'POST' \
  'http://localhost:8080/todo' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "description": "string",
  "done": true,
  "title": "string"
}'
endef
export JSON_TODO

# Tools
todo:
	@echo $$JSON_TODO | bash

list:
	@curl -X 'GET' 'http://localhost:8080/todo' -H 'accept: */*' | jq .

kat-listen:
	kafkacat -t todo-created -b localhost:9092 -C

# Kamel
camel-deploy-rest:
	kamel run --name todo-rest --dev \
		todo-service/src/main/java/dev/unexist/showcase/route/TodoRestRoute.java \
		--save

camel-deploy-kafka:
	kamel run --name todo-kafka --dev -e "KAFKA_BOOTSTRAP_SERVERS=redpanda.default.svc.cluster.local:9092" \
		todo-service/src/main/java/dev/unexist/showcase/route/TodoKafkaRoute.java \
		--save
