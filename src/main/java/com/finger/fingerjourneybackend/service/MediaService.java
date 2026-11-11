// MediaService
// 역할: 관리자가 올린 이미지 파일을 저장하고 Media 정보를 만든다. (API-ADM-017)
// - 파일은 서버의 업로드 폴더(app.media.upload-dir)에 UUID 이름으로 저장하고,
//   /media/{파일명} 주소로 내려준다. (WebConfig에서 이 폴더를 연결)
// - 저장소를 S3 등으로 바꿀 때는 이 클래스의 파일 저장 부분만 교체하면 된다.

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.MediaUploadResponseDto;
import com.finger.fingerjourneybackend.entity.Media;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class MediaService {

    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "/media/";

    private static final Set<String> MEDIA_TYPES = Set.of("IMAGE", "ICON", "BACKGROUND");
    // SVG는 스크립트를 담을 수 있어 허용하지 않는다.
    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final MediaRepository mediaRepository;
    private final Path uploadDir;

    public MediaService(
            MediaRepository mediaRepository,
            @Value("${app.media.upload-dir:./uploads}") String uploadDir) {
        this.mediaRepository = mediaRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public MediaUploadResponseDto upload(MultipartFile file, String mediaType) {
        String originalName = file == null ? null : StringUtils.getFilename(file.getOriginalFilename());
        validate(file, mediaType, originalName);

        // 저장 파일명은 항상 새로 만든다. (원본 이름의 경로 문자나 덮어쓰기를 막기 위함)
        String storedName = UUID.randomUUID() + "." + extensionOf(originalName);
        Path target = uploadDir.resolve(storedName);

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target);
        } catch (IOException e) {
            log.error("미디어 파일 저장 실패: {}", target, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Media media = new Media();
        media.setFileUrl(URL_PREFIX + storedName);
        media.setMediaType(mediaType);
        media.setOriginalFileName(originalName.length() > 255 ? originalName.substring(0, 255) : originalName);

        try {
            Media saved = mediaRepository.save(media);

            return MediaUploadResponseDto.builder()
                    .mediaId(saved.getMediaId())
                    .fileUrl(saved.getFileUrl())
                    .build();
        } catch (RuntimeException e) {
            // DB 저장에 실패하면 남은 파일을 지운다.
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                log.warn("저장 실패 후 파일 정리에 실패: {}", target);
            }
            throw e;
        }
    }

    private void validate(MultipartFile file, String mediaType, String originalName) {
        if (file == null || file.isEmpty() || !StringUtils.hasText(originalName)) {
            throw unsupported("업로드할 파일이 없습니다.");
        }
        if (mediaType == null || !MEDIA_TYPES.contains(mediaType)) {
            throw unsupported("mediaType은 IMAGE, ICON, BACKGROUND 중 하나여야 합니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw unsupported("파일 용량은 5MB 이하여야 합니다.");
        }
        if (!EXTENSIONS.contains(extensionOf(originalName))) {
            throw unsupported("png, jpg, jpeg, gif, webp 파일만 올릴 수 있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw unsupported("이미지 파일만 올릴 수 있습니다.");
        }
    }

    private String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex < 0 ? "" : fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private CustomException unsupported(String message) {
        return new CustomException(ErrorCode.UNSUPPORTED_FILE, message);
    }
}
