module opera.java {
  requires java.multibase.v1.0.0;
  requires java.rmi;
  requires java.sql;
  requires annotations;
  requires com.github.spotbugs.annotations;
  requires commons.math3;
  requires docker.java.api;
  requires docker.java.core;
  requires docker.java.transport.httpclient5;
  requires docker.java.transport.tck;
  requires org.codehaus.groovy;
  requires org.slf4j;
  requires simpleclient.httpserver;
  requires simpleclient.tracer.otel;
}