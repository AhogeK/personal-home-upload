package com.ahogek.repository;

import com.ahogek.entity.FileSystem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * 文件表数据访问层
 *
 * @author AhogeK ahogek@gmail.com
 * @since 2024-10-20 04:19:21
 */
@ApplicationScoped
public class FileSystemRepository implements PanacheRepository<FileSystem> {

}
