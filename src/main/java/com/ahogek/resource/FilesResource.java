package com.ahogek.resource;

import com.ahogek.repository.FilesRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * 文件表资源层
 *
 * @author AhogeK ahogek@gmail.com
 * @since 2024-10-20 04:23:09
 */
@Path("/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FilesResource {

    private final FilesRepository filesRepository;

    @Inject
    public FilesResource(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    @GET
    @Path("/test")
    public long count() {
        return filesRepository.count();
    }
}
