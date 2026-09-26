package com.zh.hengyi.application.service.file;

import cn.hutool.core.util.StrUtil;
import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import com.zh.hengyi.common.enums.file.image.GoodsImageEnum;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.enums.EnumConvertUtil;
import com.zh.hengyi.config.oss.aliyun.AliOssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AliOssUtil aliOssUtil;

    /**
     // 1 通用oss上传单张图片
     * 边界情况：1 形参不存在
     *         2 图片类型转换失败
     *         3 图片上传失败
     */
    @Override
    public String uploadImage(MultipartFile file, String fileType) {
        // 1、校验文件、文件类型存在
        validFileAndTpyeExist(file, fileType);

        // 2、图片类型转换(如主图枚举->主图目录)
        GoodsImageEnum goodsImageEnum = EnumConvertUtil.strToEnum(GoodsImageEnum.class, fileType);
        if (goodsImageEnum == null) {
            throw new BusinessException(ResultCode.IMAGE_CONVERT_ERROR);
        }

        // 3、图片上传
        try {
            return aliOssUtil.upload(file, goodsImageEnum.getDir());
        } catch (IOException e) {
            throw new BusinessException(ResultCode.UPLOAD_IMAGE_OSS_ERROR);
        }
    }


    /**
     * 2、通用oss批量上传图片（复用单个逻辑）
     * 边界情况：1 上传多张图片时，部分上传失败 ----> 解决：部分成功、部分失败，继续执行，返回成功列表  + 失败信息
     */
    @Override
    public FileBatchUploadVO uploadImages(List<MultipartFile> fileList, String fileType) {
        FileBatchUploadVO dto = new FileBatchUploadVO();
        List<String> successUrlList = new ArrayList<>();
        int failCount = 0;
        StringBuilder failMsgSb = new StringBuilder();

        for (int i = 0; i < fileList.size(); i++) {
            MultipartFile file = fileList.get(i);
            try {
                String url = uploadImage(file,fileType);
                successUrlList.add(url);
            } catch (BusinessException e) {
                //部分失败，内部抛出业务异常，不终止循环
                failCount++;
                failMsgSb.append("第").append(i+1).append("张：").append(e.getMessage()).append(";");
            }
        }
        dto.setSuccessUrlList(successUrlList);
        dto.setFailCount(failCount);
        dto.setFailMsg(failMsgSb.toString());
        return dto;
    }



    /**
     * 3、通用oss删除图片
     */
    @Override
    public void deleteImgByUrl(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException(ResultCode.IMG_HTTP_NOT_EXIST);
        }
        aliOssUtil.deleteByUrl(fileUrl);
    }

    @Override
    public void deleteVideoByUrl(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException(ResultCode.VIDEO_NOT_EXIST);
        }
        aliOssUtil.deleteByUrl(fileUrl);
    }



    private void validFileAndTpyeExist(MultipartFile file, String fileType) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_NOT_EXIST);
        }
        if (fileType == null) {
            throw new BusinessException(ResultCode.FILE_TYPE_EXIST);
        }
    }
}
