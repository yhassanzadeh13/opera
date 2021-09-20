lint:
	@mvn checkstyle:checkstyle
test:
	@mvn clean install
	@mvn compile
	@mvn test
generate:
	@mvn clean install
	@mvn compile