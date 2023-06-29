package group.rxcloud.ava.aigc.ai.ffmpeg;

import group.rxcloud.ava.aigc.ai.AIGCException;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Ffmpeg 视频处理服务
 */
@Slf4j
@Service
public class FfmpegVideoService {

    public static String DEFAULT_VIDEO_FORMAT = "mp4";

    public static int DEFAULT_WIDTH = 1080;
    public static int DEFAULT_HEIGHT = 1920;
    public static int VIDEO_BITRATE = 90000;
    public static int DEFAULT_FPS = 30;
    public static int DEFAULT_IMAGE_NUM_PER_SECONDS = 1;

    public static void main(String[] args) {
        FfmpegVideoService ffmpegVideoService = new FfmpegVideoService();
        {
            ffmpegVideoService.createMp4FromImages(
                    "./demo/video",
                    "./demo/voidideo/demo.mp4");
        }
        {
            ffmpegVideoService.mergeMp4WithAudio(
                    "./demo/video/demo.mp4",
                    "./demo/video/audio.mp3",
                    "./demo/video/demo_with_audio.mp4");
        }
    }

    public void createMp4FromImages(String imageDirectory, String outputMp4Path) {
        File[] images = new File(imageDirectory).listFiles();
        createMp4FromImages(images, outputMp4Path, DEFAULT_FPS, DEFAULT_IMAGE_NUM_PER_SECONDS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void createMp4FromImages(File[] images, String outputMp4Path) {
        createMp4FromImages(images, outputMp4Path, DEFAULT_FPS, DEFAULT_IMAGE_NUM_PER_SECONDS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void createMp4FromImages(File[] images, String outputMp4Path, int fps, int imageNumPerSeconds) {
        createMp4FromImages(images, outputMp4Path, fps, imageNumPerSeconds, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 从图片数组中创建视频
     */
    public void createMp4FromImages(File[] images, String outputMp4Path, int fps, int imageNumPerSeconds, int width, int height) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputMp4Path, width, height);

        try {
            // 设置视频编码层模式
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            // 设置视频为30帧每秒
            recorder.setFrameRate(fps);
            // 设置视频图像数据格式
            // recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            // 设置视频分辨率
            // recorder.setVideoMetadata("resolution", width + "x" + height);
            // 设置视频比特率
            recorder.setVideoBitrate(VIDEO_BITRATE);
            // 设置视频格式
            recorder.setFormat(DEFAULT_VIDEO_FORMAT);
            // 设置视频质量
            // recorder.setVideoQuality(0);
            // 设置视频质量参数
            recorder.setVideoOption("crf", "23");

            recorder.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            for (File image : images) {
                BufferedImage read = ImageIO.read(image);

                for (int i = 0; i < fps / imageNumPerSeconds; i++) {
                    recorder.record(converter.getFrame(read));
                }
            }

        } catch (Exception e) {
            throw new AIGCException(e);
        } finally {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                log.error("[FfmpegVideoService] release recorder error. [{}]", e.getMessage(), e);
            }
        }
    }

    /**
     * 合并两个视频
     */
    public void mergeMp4(String mp4Path1, String mp4Path2, String outputMp4Path) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputMp4Path, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        try {
            // 设置视频编码层模式
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            // 设置视频为30帧每秒
            recorder.setFrameRate(DEFAULT_FPS);
            // 设置视频图像数据格式
            // recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            // 设置视频分辨率
            // recorder.setVideoMetadata("resolution", width + "x" + height);
            // 设置视频比特率
            recorder.setVideoBitrate(VIDEO_BITRATE);
            // 设置视频格式
            recorder.setFormat(DEFAULT_VIDEO_FORMAT);
            // 设置视频质量
            // recorder.setVideoQuality(0);
            // 设置视频质量参数
            recorder.setVideoOption("crf", "23");

            recorder.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            for (int i = 0; i < DEFAULT_FPS; i++) {
                recorder.record(converter.getFrame(ImageIO.read(new File(mp4Path1))));
            }

            for (int i = 0; i < DEFAULT_FPS; i++) {
                recorder.record(converter.getFrame(ImageIO.read(new File(mp4Path2))));
            }

        } catch (Exception e) {
            throw new AIGCException(e);
        } finally {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                log.error("[FfmpegVideoService] release recorder error. [{}]", e.getMessage(), e);
            }
        }
    }

    /**
     * 合并视频和音频
     */
    public void mergeMp4WithAudio(String mp4Path, String audioPath, String outputPath) {
        FrameRecorder recorder = null;
        FrameGrabber videoGrabber = null;
        FrameGrabber audioGrabber = null;
        try {
            videoGrabber = new FFmpegFrameGrabber(mp4Path);
            audioGrabber = new FFmpegFrameGrabber(audioPath);

            videoGrabber.start();
            audioGrabber.start();

            recorder = new FFmpegFrameRecorder(outputPath, videoGrabber.getImageWidth(), videoGrabber.getImageHeight(), audioGrabber.getAudioChannels());

            recorder.setFrameRate(videoGrabber.getFrameRate());
            recorder.setSampleRate(audioGrabber.getSampleRate());
            recorder.setVideoCodec(videoGrabber.getVideoCodec());
            // recorder.setAudioCodec(audioGrabber.getAudioCodec());
            recorder.setFormat(videoGrabber.getFormat());
            // recorder.setAudioBitrate(audioGrabber.getAudioBitrate());
            recorder.setVideoBitrate(videoGrabber.getVideoBitrate());
            // recorder.setPixelFormat(videoGrabber.getPixelFormat());
            // recorder.setAudioChannels(audioGrabber.getAudioChannels());
            // recorder.setAudioQuality(0);
            // recorder.setVideoQuality(0);
            // 设置视频质量参数
            recorder.setVideoOption("crf", "23");

            recorder.start();

            Frame audioFrame;
            Frame videoFrame;
            while ((audioFrame = audioGrabber.grabFrame()) != null && (videoFrame = videoGrabber.grabFrame()) != null) {
                recorder.record(videoFrame);
                recorder.record(audioFrame);
            }
        } catch (FrameGrabber.Exception e) {
            throw new AIGCException(e);
        } catch (FrameRecorder.Exception e) {
            throw new AIGCException(e);
        } finally {
            try {
                recorder.stop();
                recorder.release();

                videoGrabber.stop();
                videoGrabber.release();

                audioGrabber.stop();
                audioGrabber.release();
            } catch (Exception e) {
                log.error("[FfmpegVideoService] release recorder error. [{}]", e.getMessage(), e);
            }
        }
    }
}
