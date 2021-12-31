lint:
	@mvn checkstyle:checkstyle
lint-verbose:
	@mvn -e checkstyle:checkstyle
test:
	@mvn clean install
	@mvn compile
	@mvn test
generate:
	@mvn clean install
	@mvn compile