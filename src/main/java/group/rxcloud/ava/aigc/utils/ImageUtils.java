package group.rxcloud.ava.aigc.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static void main(String[] args) {
        ImageUtils.compressJPG("./demo/awsrekognitionimage_input.jpg", "./demo/awsrekognitionimage_output.jpg");
    }

    /**
     * Compress JPG image
     */
    public static String compressJPG(String imagePath, String outputPath) {
        try {
            File input = new File(imagePath);
            BufferedImage image = ImageIO.read(input);

            File output = new File(outputPath);
            BufferedImage compress = compress(image, 0.5f);
            ImageIO.write(compress, "jpg", output);

            return outputPath;
        } catch (IOException e) {
            throw new UtilsException(e);
        }
    }

    private static BufferedImage compress(BufferedImage image, float quality) throws IOException {
        BufferedImage compressedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        compressedImage.createGraphics().drawImage(image, 0, 0, null);

        // Get a ImageWriter for jpeg format.
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

        // Set the compression quality
        javax.imageio.plugins.jpeg.JPEGImageWriteParam param = (javax.imageio.plugins.jpeg.JPEGImageWriteParam) writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        // Write the compressed image to the output stream
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new javax.imageio.IIOImage(compressedImage, null, null), param);

        // Clean up
        writer.dispose();

        // Get the compressed image as a BufferedImage object
        BufferedImage compressed = null;
        try {
            compressed = ImageIO.read(new java.io.ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return compressed;
    }
}
