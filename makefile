lint:
	@mvn checkstyle:checkstyle
lint-verbose:
	@mvn -e checkstyle:checkstyle
check:
	@mvn spotbugs:check
test:
	@mvn clean install
	@mvn compile
	@mvn test
generate:
	@mvn clean install
	@mvn compile
docker-stop-opera:
	docker container stop $$(docker ps -aq --filter name="opera" --format="{{.ID}}") || true