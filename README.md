# Pure Java HTTP Server
Fast and lightweight HTTP Server

Usage
```java
HttpServer httpServer = new HttpServerImpl(host, port);
WebHandler webHandler = new WebHandler() {
  @Override
  public HttpResponse handleQuery(HttpRequest httpRequest) {
    // Handle requests
    return HttpResponse.build(Status.NOT_FOUND);
  }
  @Override
  public InputStream toStream(File file) throws Exception {
    return new FileInputStream(file); // Convert content to input stream
  }
};
// Set relative path of content folder
webHandler.setContentFolder("WebContent");
// Add context
webHandler.getIndexMap().put("/", "/index.html");
// Set HTTP listener
httpServer.setHttpListener(webHandler);
// Connect server
httpServer.connect();
```