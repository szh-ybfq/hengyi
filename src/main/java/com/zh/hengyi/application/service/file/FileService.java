package com.zh.hengyi.application.service.file;

import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String uploadImage(MultipartFile file,String fileType);
    FileBatchUploadVO uploadImages(List<MultipartFile> fileList, String fileType);

    void deleteImgByUrl(String fileUrl);
    void deleteVideoByUrl(String fileUrl);


//    String uploadImages(MultipartFile file,String fileType);
}
