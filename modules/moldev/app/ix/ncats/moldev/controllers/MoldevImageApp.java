package ix.ncats.moldev.controllers;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;
import gov.nih.ncgc.imaging.MolDevImage;
import gov.nih.ncgc.imaging.MolDevUtils;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rajarshi Guha
 */
public class MoldevImageApp extends MoldevApp {

    private static byte[] getImageBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        switch (format) {
            case "jpeg":
                ImageIO.write(image, "jpeg", baos);
                break;
            case "tiff":
                TIFFEncodeParam params = new TIFFEncodeParam();
                params.setWriteTiled(false);
                ImageEncoder encoder = ImageCodec.createImageEncoder("TIFF", baos, params);
                encoder.encode(image);
                break;
            case "png":
                ImageIO.write(image, "png", baos);
                break;
            default:
                return null;
        }
        return baos.toByteArray();
    }

    public static Result getImage(String plateName, Integer row, Integer col, Long siteid, String waveId,
                                  String thumb, String format)
            throws SQLException, IOException {

        MolDevUtils mdu = new MolDevUtils(getRepositoryPath());

        BufferedImage image;
        boolean makeThumb = thumb != null && thumb.toLowerCase().equals("true");

        if (siteid == 0) { // retrieve images for all site ids
            List<MolDevImage> images = new ArrayList<>();
            List<Long> siteids = mdu.getSiteIds(plateName, row, col);
            for (Long id : siteids) {
                if (makeThumb) images.add(mdu.getMolDevImage(plateName, row, col, waveId, id));
                else images.add(mdu.getMolDevImage(plateName, row, col, waveId, id));
            }
            if (makeThumb)
                image = mdu.getThumbnailMontage(images);
            else image = mdu.getMontage(images);
        } else {
            if (thumb != null && thumb.toLowerCase().equals("true"))
                image = mdu.getMolDevImage(plateName, row, col, waveId, siteid).getThumbnail();
            else image = mdu.getMolDevImage(plateName, row, col, waveId, siteid).getImage();
        }
        if (image == null) return notFound("Couldn't find an image!");

        String returnType = "image/png";
        byte[] bytes = new byte[0];
        if (format.toLowerCase().equals("jpeg")) {
            bytes = getImageBytes(image, "jpeg");
            returnType = "image/jpeg";
        } else if (format.toLowerCase().equals("tiff")) {
            bytes = getImageBytes(image, "tiff");
            returnType = "image/tiff";
        } else if (format.toLowerCase().equals("png")) {
            bytes = getImageBytes(image, "png");
        }
        response().setHeader(CACHE_CONTROL, "max-age=31536000");
        return ok(bytes).as(returnType);
    }

}
