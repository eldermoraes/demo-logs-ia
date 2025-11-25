package com.eldermoraes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import java.io.InputStream;

@Path("/")
public class UIResource {

    private static final Logger LOG = Logger.getLogger(UIResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {
        return serveFile("ui/index.html", MediaType.TEXT_HTML);
    }

    @GET
    @Path("styles.css")
    @Produces("text/css")
    public Response getStyles() {
        return serveFile("ui/styles.css", "text/css");
    }

    @GET
    @Path("app.js")
    @Produces("application/javascript")
    public Response getAppJs() {
        return serveFile("ui/app.js", "application/javascript");
    }

    private Response serveFile(String resourcePath, String mediaType) {
        try {
            LOG.infof("Attempting to load resource: %s", resourcePath);
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            
            if (is == null) {
                LOG.errorf("Resource not found: %s", resourcePath);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("File not found: " + resourcePath)
                        .build();
            }
            
            byte[] content = is.readAllBytes();
            is.close();
            
            LOG.infof("Successfully loaded resource: %s (%d bytes)", resourcePath, content.length);
            
            return Response.ok(content)
                    .type(mediaType)
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error loading file: %s", resourcePath);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error loading file: " + e.getMessage())
                    .build();
        }
    }
}
