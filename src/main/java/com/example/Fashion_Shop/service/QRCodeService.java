package com.example.Fashion_Shop.service;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Service
public class QRCodeService {
    // Phương thức để tạo QR code và lưu vào file

    public String generateQRCode(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        // Đường dẫn tới thư mục static/qr-codes trong resources
        File outputDir = new File("src/main/resources/static/qr-codes");

        // Nếu thư mục không tồn tại, tạo mới
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        // Tạo tên tệp QR code
        String fileName = "QRCode_" + System.currentTimeMillis() + ".png";
        Path path = FileSystems.getDefault().getPath(outputDir.getAbsolutePath(), fileName);

        // Ghi QR code vào file
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return path.toString(); // Trả về đường dẫn file QR code
    }
    // Phương thức để tạo QR code và trả về dưới dạng mảng byte
    public byte[] generateQRCodeAsBytes(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    public String readQRCodeFromFile(String filePath) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(new File(filePath));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();  // Trả về nội dung của QR Code
    }

    // Đọc nội dung từ BufferedImage (cho phép đọc từ luồng)
    public String readQRCodeFromImage(BufferedImage bufferedImage) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }
}
