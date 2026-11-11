package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.MediaUploadResponseDto;
import com.finger.fingerjourneybackend.entity.Media;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @Mock
    private MediaRepository mediaRepository;

    @TempDir
    private Path uploadDir;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, uploadDir.toString());
    }

    @Test
    void uploadStoresFileUnderGeneratedNameAndSavesMedia() throws IOException {
        when(mediaRepository.save(any())).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setMediaId(401L);
            return media;
        });

        MediaUploadResponseDto response = mediaService.upload(
                new MockMultipartFile("file", "printer-step1.png", "image/png", PNG_BYTES), "IMAGE");

        assertThat(response.getMediaId()).isEqualTo(401L);
        assertThat(response.getFileUrl()).startsWith("/media/").endsWith(".png");

        Path stored = uploadDir.resolve(response.getFileUrl().substring("/media/".length()));
        assertThat(Files.readAllBytes(stored)).isEqualTo(PNG_BYTES);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getMediaType()).isEqualTo("IMAGE");
        assertThat(captor.getValue().getOriginalFileName()).isEqualTo("printer-step1.png");
    }

    @Test
    void uploadNeverWritesOutsideTheUploadDirectory() throws IOException {
        when(mediaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MediaUploadResponseDto response = mediaService.upload(
                new MockMultipartFile("file", "../../evil.png", "image/png", PNG_BYTES), "ICON");

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getOriginalFileName()).isEqualTo("evil.png");
        assertThat(Files.exists(uploadDir.resolve(response.getFileUrl().substring("/media/".length())))).isTrue();
        assertThat(Files.exists(uploadDir.getParent().resolve("evil.png"))).isFalse();
    }

    @Test
    void uploadRejectsMissingOrEmptyFile() {
        assertUnsupported(null, "IMAGE");
        assertUnsupported(new MockMultipartFile("file", "empty.png", "image/png", new byte[0]), "IMAGE");
    }

    @Test
    void uploadRejectsUnknownMediaType() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", PNG_BYTES);

        assertUnsupported(file, "VIDEO");
        assertUnsupported(file, null);
    }

    @Test
    void uploadRejectsFilesThatAreNotAllowedImages() {
        assertUnsupported(new MockMultipartFile("file", "run.exe", "application/octet-stream", PNG_BYTES), "IMAGE");
        assertUnsupported(new MockMultipartFile("file", "logo.svg", "image/svg+xml", PNG_BYTES), "IMAGE");
        assertUnsupported(new MockMultipartFile("file", "fake.png", "text/plain", PNG_BYTES), "IMAGE");
        assertUnsupported(new MockMultipartFile("file", "noextension", "image/png", PNG_BYTES), "IMAGE");
    }

    @Test
    void uploadRejectsFilesOverTheSizeLimit() {
        byte[] tooLarge = new byte[(int) MediaService.MAX_FILE_SIZE + 1];

        assertUnsupported(new MockMultipartFile("file", "big.png", "image/png", tooLarge), "IMAGE");
    }

    @Test
    void uploadRemovesStoredFileWhenSavingMediaFails() throws IOException {
        when(mediaRepository.save(any())).thenThrow(new IllegalStateException("DB 오류"));

        assertThatThrownBy(() -> mediaService.upload(
                new MockMultipartFile("file", "a.png", "image/png", PNG_BYTES), "IMAGE"))
                .isInstanceOf(IllegalStateException.class);

        try (Stream<Path> files = Files.list(uploadDir)) {
            assertThat(files.count()).isZero();
        }
    }

    private void assertUnsupported(MultipartFile file, String mediaType) {
        assertThatThrownBy(() -> mediaService.upload(file, mediaType))
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE);
        verify(mediaRepository, never()).save(any());
    }
}
