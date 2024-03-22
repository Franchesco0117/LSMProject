package com.francisco.lsmproyect;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class objectDetectorClass {
    //interpreter es usado para cargar el modelo y predecir
    private Interpreter interpreter;

    // Almacenar todas las etiquetas(labelmap) en este arreglo
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXELS_SIZE = 3; // Para RGB
    private int IMAGE_MEAN = 0;
    private float IMAGE_STD = 255.0f;

    // gpuDelegate usado para inicializar la GPU en la App
    private GpuDelegate gpuDelegate;
    private int height = 0;
    private int width = 0;

    objectDetectorClass(AssetManager assetManager, String modelPath, String labelPath, int inputSize) throws IOException {
        INPUT_SIZE = inputSize;
        // options usado para definir la GPU, CPU o numero de threads
        Interpreter.Options options = new Interpreter.Options();
        //gpuDelegate = new GpuDelegate();
        //options.addDelegate(gpuDelegate);
        options.setNumThreads(4); // Poner la cantidad de hilos segun el celular (!!!)

        // Cargando modelo
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);

        // Cargar labelmap
        labelList = loadLabelList(assetManager, labelPath);
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
         // fileDescriptor usado para obtener la descripcion del archivo
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // Para capturar el label
        List<String> labelList = new ArrayList<>();

        // Crear un nuevo lector
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;

        // Bucle para recorrer cada linea y capturarlo en labelList
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }

        reader.close();

        return labelList;
    }

    public Mat recognizeImage(Mat matImage) {
        // Rotar la imagen original por 90 grados para tener modo retrato
        Mat rotatedMatImage = new Mat();

        Mat a = matImage.t();
        Core.flip(a, rotatedMatImage, 1);
        a.release();

        // Si no haces sete proceso, obtendras menos predicciones correctas, menos numero de objectos
        // Ahora convertir a Bitmap
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(rotatedMatImage.cols(), rotatedMatImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotatedMatImage, bitmap);

        // Definir altura y ancho
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        // Escalar el Bitmap a Input size del modelo
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // Convertir bitmap a Bytebuffer como modelo Input deberia de estar ahi
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // Definiendo el output
        // 10: top 10 objetos detectados
        // 4: Condenadas de la imagen
        // float[][][] result = new float[1][10][4]; no se usa por ahora je
        Object[] input = new Object[1];
        input[0] = byteBuffer;

        // TreeMap: Arreglo de tres (boxes, score, classes)
        Map<Integer, Object> outputMap = new TreeMap<>();

        // 10: top 10 objetos detectados
        // 4: Condenadas de la imagen
        float [][][] boxes = new float[1][10][4];

        // Captura puntos de 10 objectos
        float [][] scores = new float[1][10];

        // Captura la clase del objecto
        float [][] classes = new float[1][10];

        // Añadirlo al objectMap;
        outputMap.put(0, boxes);
        outputMap.put(1, classes);
        outputMap.put(2, scores);

        // Ahora predecir
        interpreter.runForMultipleInputsOutputs(input, outputMap);

        Object value = outputMap.get(0);
        Object objectClass = outputMap.get(1);
        Object score = outputMap.get(2);

        // Recorrer cada objecto - Output solo tiene 10 boxes
        for (int i = 0; i < 10; i++){
            float classValue = (float) Array.get(Array.get(objectClass, 0), i);
            float scoreValue = (float) Array.get(Array.get(score, 0), i);

            // Definir entrada para la puntuación
            if (scoreValue > 0.5) {
                Object box1 = Array.get(Array.get(value, 0), i);

                // Lo estamos multiplicando con el tamaño orignal y la altura del frame
                float top = (float) Array.get(box1, 0)*height;
                float left = (float) Array.get(box1, 1)*width;
                float bottom = (float) Array.get(box1, 2)*height;
                float right = (float) Array.get(box1, 3)*width;

                // Dibujar rectangulo en el frame original  - Punto inicial box   - Punto final box       - color de box     - grueso
                Imgproc.rectangle(rotatedMatImage, new Point(left, top), new Point(right, bottom), new Scalar(255, 155, 155), 2);

                // Escribir texto en el frame  - String del nombre de la clase obj   - Punto inicial                             - Color de texto       - Tamaño texto
                Imgproc.putText(rotatedMatImage, labelList.get((int) classValue), new Point(left, top), 3, 1,  new Scalar(255, 255, 255), 2);
            }
        }

        // Antes de return, rotar la imagen otra vez por -90 grados
        Mat b = rotatedMatImage.t();
        Core.flip(b, matImage, 0);
        b.release();

        return matImage;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        int quant = 1;
        int sizeImages = INPUT_SIZE;

        // Algunos modelos Input deberian de quant = 0; para algunos otros quant = 1;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1*sizeImages*sizeImages*3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4*1*sizeImages*sizeImages*3);
        }

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[sizeImages*sizeImages];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        for (int i = 0; i < sizeImages; ++i) {
            for (int j = 0; j < sizeImages; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16 ) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8 ) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val) & 0xFF))/255.0f);
                }
            }
        }

        return byteBuffer;
    }

}
