package enel.dev.budgets.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoManager {

    public static String saveImageToAppDirectory(Context context, Uri imageUri, String _fileName) {
        // Define el directorio de destino dentro de la app
        File appDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
        if (!appDir.exists()) {
            appDir.mkdirs(); // Crea el directorio si no existe
        }

        // Genera un nombre de archivo único
        String fileName = _fileName + ".jpg";
        File destinationFile = new File(appDir, fileName);

        try {
            // Obtén el flujo de entrada de la Uri
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            // Copia los datos desde el flujo de entrada al archivo de destino
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer);
            }

            // Cierra los flujos
            outputStream.close();
            inputStream.close();

            // Verifica que el archivo se haya creado y no esté vacío
            if (destinationFile.exists() && destinationFile.length() > 0) {
                Log.d("IMAGE_STORED", destinationFile.getAbsolutePath());
                return destinationFile.getAbsolutePath();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteImageFromAppDirectory(Context context, final String imageName) {
        try {
            File appDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
            String fileName = imageName + ".jpg";
            File destinationFile = new File(appDir, fileName);
            if (destinationFile.exists())
                return destinationFile.delete();
        } catch (Exception ignored) { }
        return false;
    }


    public static Bitmap getImage(String imagePath) {
        if (imagePath != null) {
            return BitmapFactory.decodeFile(imagePath);
        } return null;
    }

}
